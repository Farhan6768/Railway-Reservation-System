import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.swing.JOptionPane;

public class Ticket implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int ticketCounter = 1000;
    
    // Ticket Information
    private String pnr;
    private final String userId;
    private LocalDateTime bookingDateTime;
    private LocalDateTime journeyDate;
    private String status; // CONFIRMED, CANCELLED, PENDING
    
    // Passenger Information
    private final String passengerName;
    private final String phoneNumber;
    private final String email;
    
    // Train Information
    private final Train train;
    private final int numberOfSeats;
    private String seatType; // AC or Non-AC
    private final List<String> seatNumbers;
    private final String coachNumber;
    private final String bookingClass; // SL, 3A, 2A, 1A
    
    // Fare Information
    private double baseFare;
    private double tax;
    private double totalFare;
    
    // Payment Information
    private String paymentMethod;
    private String paymentId;
    private String transactionReference;

    public Ticket(String userId, Train train, int numberOfSeats, String seatType,
                 String passengerName, String phoneNumber, String email, 
                 LocalDateTime journeyDate) {
        this.pnr = generatePNR();
        this.userId = userId;
        this.train = train;
        this.numberOfSeats = numberOfSeats;
        this.seatType = seatType;
        this.passengerName = passengerName;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.journeyDate = journeyDate;
        this.bookingDateTime = LocalDateTime.now();
        this.status = "CONFIRMED";
        this.seatNumbers = generateSeatNumbers();
        this.coachNumber = generateCoachNumber();
        this.bookingClass = determineBookingClass();
        calculateFares();
    }

    public void cancelTicket() throws IllegalStateException {
        if ("CANCELLED".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Ticket is already cancelled");
        }
        
        if ("REFUNDED".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Refunded tickets cannot be cancelled");
        }
        
        if ("USED".equalsIgnoreCase(this.status)) {
            throw new IllegalStateException("Used tickets cannot be cancelled");
        }
        
        // Allow cancellation for other statuses but show warning
        if (!"CONFIRMED".equalsIgnoreCase(this.status)) {
            System.out.println("Warning: Cancelling ticket in " + this.status + " status");
        }
        
        this.status = "CANCELLED";
        System.out.println("Ticket " + this.pnr + " status changed to CANCELLED");
    }

    // Setters
    public void setPnr(String pnr) {
        this.pnr = pnr;
    }

    public void setBookingDateTime(LocalDateTime bookingDateTime) {
        this.bookingDateTime = bookingDateTime;
    }

    public void setStatus(String status) {
        if (status != null && 
            (status.equals("CONFIRMED") || 
             status.equals("PENDING") || 
             status.equals("CANCELLED"))) {
            this.status = status;
        } else {
            throw new IllegalArgumentException("Invalid ticket status");
        }
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public void setJourneyDate(LocalDateTime journeyDate) {
        this.journeyDate = journeyDate;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }

    public void setBaseFare(double baseFare) {
        this.baseFare = baseFare;
    }

    public void setTotalFare(double totalFare) {
        this.totalFare = totalFare;
    }

    // PNR generation
    private String generatePNR() {
        String timestamp = Long.toString(System.currentTimeMillis() % 100000);
        Random rand = new Random();
        return "PNR" + timestamp + 
               Character.toString(rand.nextInt(26) + 'A') +
               Character.toString(rand.nextInt(26) + 'A');
    }

    // Seat number generation
    private List<String> generateSeatNumbers() {
        List<String> seats = new ArrayList<>();
        Random rand = new Random();
        int coachSeats = seatType.equals("AC") ? 30 : 72;
        
        for (int i = 0; i < numberOfSeats; i++) {
            String seat;
            do {
                seat = String.format("%02d", rand.nextInt(coachSeats) + 1);
            } while (seats.contains(seat));
            seats.add(seat);
        }
        return seats;
    }

    // Coach number generation
    private String generateCoachNumber() {
        Random rand = new Random();
        if (seatType.equals("AC")) {
            return "A" + (rand.nextInt(8) + 1);
        } else {
            String[] prefixes = {"S", "B", "C"};
            return prefixes[rand.nextInt(prefixes.length)] + (rand.nextInt(12) + 1);
        }
    }

    // Booking class determination
    private String determineBookingClass() {
        if (seatType.equals("AC")) {
            if (train.hasExecutiveClass()) {
                return numberOfSeats <= 2 ? "1A" : "2A";
            }
            return numberOfSeats <= 2 ? "2A" : "3A";
        } else {
            return "SL";
        }
    }

    // Fare calculation
    private void calculateFares() {
        double farePerSeat = seatType.equals("AC") ? 
                           train.getAcFare() : train.getBaseFare();
        
        double demandMultiplier = calculateDemandMultiplier();
        double classMultiplier = getClassMultiplier();
        double advanceBookingDiscount = getAdvanceBookingDiscount();
        
        this.baseFare = farePerSeat * numberOfSeats * demandMultiplier * classMultiplier;
        this.tax = this.baseFare * 0.18;
        this.totalFare = (this.baseFare + this.tax) * (1 - advanceBookingDiscount);
    }

    private double calculateDemandMultiplier() {
        double occupancyRate = 1 - ((double)train.getAvailableSeats() / train.getTotalSeats());
        return 0.9 + (occupancyRate * 0.9);
    }

    private double getClassMultiplier() {
        switch (bookingClass) {
            case "1A": return 1.8;
            case "2A": return 1.4;
            case "3A": return 1.2;
            default: return 1.0;
        }
    }

    private double getAdvanceBookingDiscount() {
        long daysInAdvance = java.time.Duration.between(LocalDateTime.now(), journeyDate).toDays();
        if (daysInAdvance > 60) return 0.15;
        if (daysInAdvance > 30) return 0.10;
        if (daysInAdvance > 15) return 0.05;
        return 0;
    }

    // Payment handling
    public void setPaymentDetails(String paymentMethod, String transactionReference) {
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
        this.paymentId = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        if (paymentMethod.equalsIgnoreCase("UPI")) {
            this.status = "CONFIRMED";
        } else if (paymentMethod.equalsIgnoreCase("Credit Card")) {
            this.status = "PENDING";
        }
    }

    // Getters
    public String getPnr() { return pnr; }
    public String getUserId() { return userId; }
    public Train getTrain() { return train; }
    public int getNumberOfSeats() { return numberOfSeats; }
    public String getSeatType() { return seatType; }
    public double getBaseFare() { return baseFare; }
    public double getTax() { return tax; }
    public double getTotalFare() { return totalFare; }
    public LocalDateTime getBookingDateTime() { return bookingDateTime; }
    public LocalDateTime getJourneyDate() { return journeyDate; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentId() { return paymentId; }
    public String getStatus() { return status; }
    public String getPassengerName() { return passengerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public List<String> getSeatNumbers() { return seatNumbers; }
    public String getCoachNumber() { return coachNumber; }
    public String getBookingClass() { return bookingClass; }
    public String getTransactionReference() { return transactionReference; }

    // Utility methods
    public String getSeatNumbersAsString() {
        return String.join(", ", seatNumbers);
    }

    public String getFormattedJourneyDate() {
        return journeyDate.format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy"));
    }

    public String getFormattedBookingTime() {
        return bookingDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
    }

    public boolean isCancellable() {
        return status.equals("CONFIRMED") && 
               LocalDateTime.now().isBefore(journeyDate);
    }

    public String getTrainDetails() {
        return String.format("%s (%s) %s to %s", 
            train.getTrainName(), 
            train.getTrainNumber(),
            train.getSource(),
            train.getDestination());
    }

    public String getJourneyTimings() {
        return String.format("Dep: %s | Arr: %s",
            train.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")),
            train.getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
    }

    public String generateTicketPrintout() {
        return "Ticket Details:\n" +
               "PNR: " + getPnr() + "\n" +
               "Passenger Name: " + getPassengerName() + "\n" +
               "Train Number: " + getTrain().getTrainNumber() + "\n" +
               "Source: " + getTrain().getSource() + "\n" +
               "Destination: " + getTrain().getDestination() + "\n" +
               "Journey Date: " + getFormattedJourneyDate() + "\n" +
               "Seats: " + getNumberOfSeats() + "\n" +
               "Fare: ₹" + getTotalFare();
    }
}