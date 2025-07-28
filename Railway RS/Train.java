import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Train implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Basic train information
    private final String trainNumber;  // Immutable identifier
    private String trainName;
    private String source;
    private String destination;
    
    // Schedule information
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    
    // Seat configuration
    private int totalSeats;
    private int acSeats;
    private int availableAcSeats;
    private int availableNonAcSeats;
    
    // Fare information
    private double baseFare;
    private double acFareMultiplier = 1.5;
    private static final double TAX_RATE = 0.18;
    
    // Additional properties
    private String trainType; // Rajdhani, Shatabdi, Express, etc.
    private boolean hasPantry;
    private int averageSpeed; // km/h

    public Train(String trainNumber, String trainName, String source, String destination,
                LocalDateTime departureTime, LocalDateTime arrivalTime, 
                int totalSeats, double baseFare, int acSeats) {
        validateConstructorArgs(trainNumber, trainName, source, destination, 
                              departureTime, arrivalTime, totalSeats, baseFare, acSeats);
        
        this.trainNumber = trainNumber;
        this.trainName = trainName;
        this.source = source;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.totalSeats = totalSeats;
        this.baseFare = baseFare;
        this.acSeats = acSeats;
        this.availableAcSeats = acSeats;
        this.availableNonAcSeats = totalSeats - acSeats;
    }

    private void validateConstructorArgs(String trainNumber, String trainName, String source, 
                                       String destination, LocalDateTime departureTime, 
                                       LocalDateTime arrivalTime, int totalSeats, 
                                       double baseFare, int acSeats) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Train number cannot be empty");
        }
        if (acSeats > totalSeats) {
            throw new IllegalArgumentException("AC seats cannot exceed total seats");
        }
        if (baseFare <= 0) {
            throw new IllegalArgumentException("Base fare must be positive");
        }
        if (arrivalTime.isBefore(departureTime)) {
            throw new IllegalArgumentException("Arrival time must be after departure time");
        }
    }

    // Enhanced seat management
    public synchronized boolean bookSeats(int numberOfSeats, String seatType) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }

        if ("AC".equalsIgnoreCase(seatType)) {
            if (availableAcSeats >= numberOfSeats) {
                availableAcSeats -= numberOfSeats;
                return true;
            }
        } else {
            if (availableNonAcSeats >= numberOfSeats) {
                availableNonAcSeats -= numberOfSeats;
                return true;
            }
        }
        return false;
    }

    public synchronized void cancelSeats(int numberOfSeats, String seatType) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }

        if ("AC".equalsIgnoreCase(seatType)) {
            availableAcSeats = Math.min(acSeats, availableAcSeats + numberOfSeats);
        } else {
            availableNonAcSeats = Math.min(totalSeats - acSeats, availableNonAcSeats + numberOfSeats);
        }
    }

    // Enhanced fare calculation
    public double calculateFare(int numberOfSeats, String seatType, LocalDateTime bookingDate) {
        if (numberOfSeats <= 0) {
            throw new IllegalArgumentException("Number of seats must be positive");
        }

        double farePerSeat = "AC".equalsIgnoreCase(seatType) ? getAcFare() : baseFare;
        double subtotal = farePerSeat * numberOfSeats;
        
        // Apply dynamic pricing based on demand
        double demandMultiplier = calculateDemandMultiplier();
        subtotal *= demandMultiplier;
        
        // Apply advance booking discount
        double discount = calculateAdvanceBookingDiscount(bookingDate);
        subtotal *= (1 - discount);
        
        double tax = subtotal * TAX_RATE;
        return subtotal + tax;
    }

    private double calculateDemandMultiplier() {
        double occupancyRate = 1 - ((double)getAvailableSeats() / totalSeats);
        return 1.0 + (occupancyRate * 0.5); // 1.0x to 1.5x based on demand
    }

    private double calculateAdvanceBookingDiscount(LocalDateTime bookingDate) {
        long daysInAdvance = Duration.between(bookingDate, departureTime).toDays();
        if (daysInAdvance > 60) return 0.15;
        if (daysInAdvance > 30) return 0.10;
        if (daysInAdvance > 15) return 0.05;
        return 0;
    }

    // New functionality
    public Duration getJourneyDuration() {
        return Duration.between(departureTime, arrivalTime);
    }

    public boolean hasExecutiveClass() {
        return trainType != null && (trainType.equals("Rajdhani") || trainType.equals("Shatabdi"));
    }

    public String getFormattedSchedule() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm");
        return String.format("%s to %s\nDep: %s\nArr: %s",
                source, destination,
                departureTime.format(formatter),
                arrivalTime.format(formatter));
    }

    // Getters and Setters
    public String getTrainNumber() { return trainNumber; }
    public String getTrainName() { return trainName; }
    public void setTrainName(String trainName) { 
        if (trainName == null || trainName.trim().isEmpty()) {
            throw new IllegalArgumentException("Train name cannot be empty");
        }
        this.trainName = trainName; 
    }

    public String getSource() { return source; }
    public void setSource(String source) { 
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Source cannot be empty");
        }
        this.source = source; 
    }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { 
        if (destination == null || destination.trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty");
        }
        this.destination = destination; 
    }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { 
        if (departureTime == null || departureTime.isAfter(arrivalTime)) {
            throw new IllegalArgumentException("Invalid departure time");
        }
        this.departureTime = departureTime; 
    }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { 
        if (arrivalTime == null || arrivalTime.isBefore(departureTime)) {
            throw new IllegalArgumentException("Invalid arrival time");
        }
        this.arrivalTime = arrivalTime; 
    }

    public int getTotalSeats() { return totalSeats; }
    public void setTotalSeats(int totalSeats) {
        if (totalSeats <= 0) {
            throw new IllegalArgumentException("Total seats must be positive");
        }
        if (acSeats > totalSeats) {
            throw new IllegalArgumentException("AC seats cannot exceed total seats");
        }
        this.totalSeats = totalSeats;
        this.availableNonAcSeats = totalSeats - acSeats - (this.totalSeats - availableAcSeats - availableNonAcSeats);
    }

    public int getAcSeats() { return acSeats; }
    public void setAcSeats(int acSeats) {
        if (acSeats < 0 || acSeats > totalSeats) {
            throw new IllegalArgumentException("Invalid AC seats count");
        }
        int difference = acSeats - this.acSeats;
        this.acSeats = acSeats;
        this.availableAcSeats += difference;
        this.availableNonAcSeats -= difference;
    }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { 
        if (baseFare <= 0) {
            throw new IllegalArgumentException("Base fare must be positive");
        }
        this.baseFare = baseFare; 
    }

    public double getAcFare() { return baseFare * acFareMultiplier; }
    public void setAcFareMultiplier(double multiplier) { 
        if (multiplier < 1.0) {
            throw new IllegalArgumentException("AC multiplier must be >= 1.0");
        }
        this.acFareMultiplier = multiplier; 
    }

    public void setAvailableAcSeats(int availableAcSeats) {
        this.availableAcSeats = availableAcSeats;
    }

    public int getAvailableAcSeats() {
        return availableAcSeats;
    }
    public void setAvailableNonAcSeats(int availableNonAcSeats) {
        this.availableNonAcSeats = availableNonAcSeats;
    }

    public int getAvailableSeats() { return availableAcSeats + availableNonAcSeats; }
    public int getAvailableNonAcSeats() { return availableNonAcSeats; }

    public String getTrainType() { return trainType; }
    public void setTrainType(String trainType) { this.trainType = trainType; }

    public boolean hasPantry() { return hasPantry; }
    public void setHasPantry(boolean hasPantry) { this.hasPantry = hasPantry; }

    public int getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(int averageSpeed) { 
        if (averageSpeed <= 0) {
            throw new IllegalArgumentException("Average speed must be positive");
        }
        this.averageSpeed = averageSpeed; 
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s to %s)\n" +
               "Departure: %s\n" +
               "Arrival: %s\n" +
               "Duration: %dh %dm\n" +
               "Seats: %d total (%d AC, %d Non-AC)\n" +
               "Available: %d AC, %d Non-AC\n" +
               "Fares: ₹%.2f (Non-AC), ₹%.2f (AC)",
               trainNumber, trainName, source, destination,
               departureTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
               arrivalTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")),
               getJourneyDuration().toHours(), getJourneyDuration().toMinutesPart(),
               totalSeats, acSeats, totalSeats - acSeats,
               availableAcSeats, availableNonAcSeats,
               baseFare, getAcFare());
    }
}