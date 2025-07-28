import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JOptionPane;

public class DatabaseManager {
    private static final String DATA_DIR = "data";
    private static final String TRAINS_FILE = DATA_DIR + File.separator + "trains.txt";
    private static final String TICKETS_FILE = DATA_DIR + File.separator + "tickets.txt";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.txt";
    private static final String ADMIN_FILE = DATA_DIR + File.separator + "admin.txt";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static volatile DatabaseManager instance;
    private final Map<String, Train> trains;
    private final Map<String, Ticket> tickets;
    private final Map<String, String> users;
    private final Map<String, String> admins;

    private DatabaseManager() {
        this.trains = new ConcurrentHashMap<>();
        this.tickets = new ConcurrentHashMap<>();
        this.users = new ConcurrentHashMap<>();
        this.admins = new ConcurrentHashMap<>();
        initializeData();
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public synchronized boolean cancelTicket(String pnr) {
        // Input validation
        if (pnr == null || pnr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "Please enter a valid PNR number",
                "Invalid Input", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        pnr = pnr.trim();
    
        // Get the ticket
        Ticket ticket = tickets.get(pnr);
        if (ticket == null) {
            JOptionPane.showMessageDialog(null, 
                "Ticket with PNR " + pnr + " not found",
                "Ticket Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    
        try {
            // Cancel the ticket (this updates the status)
            ticket.cancelTicket();
            
            // Return seats to train availability
            Train train = ticket.getTrain();
            if (train != null) {
                if ("AC".equalsIgnoreCase(ticket.getSeatType())) {
                    train.setAvailableAcSeats(train.getAvailableAcSeats() + ticket.getNumberOfSeats());
                } else {
                    train.setAvailableNonAcSeats(train.getAvailableNonAcSeats() + ticket.getNumberOfSeats());
                }
            }
    
            // Force save changes to both files
            boolean ticketsSaved = forceSaveTickets();
            boolean trainsSaved = forceSaveTrains();
            
            if (!ticketsSaved || !trainsSaved) {
                throw new IOException("Failed to save changes to files");
            }
            
            JOptionPane.showMessageDialog(null,
                "Ticket " + pnr + " has been cancelled successfully.\n" +
                "Seats have been returned to availability.",
                "Cancellation Successful",
                JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IllegalStateException e) {
            JOptionPane.showMessageDialog(null,
                "Cannot cancel ticket: " + e.getMessage(),
                "Cancellation Failed",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "Error saving cancellation: " + e.getMessage(),
                "File Error",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    
    // Add these new methods to DatabaseManager
    private synchronized boolean forceSaveTickets() throws IOException {
        File file = new File(TICKETS_FILE);
        File tempFile = new File(TICKETS_FILE + ".tmp");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (Ticket ticket : tickets.values()) {
                writer.println(String.join("|",
                    ticket.getPnr(),
                    ticket.getUserId(),
                    ticket.getTrain().getTrainNumber(),
                    String.valueOf(ticket.getNumberOfSeats()),
                    ticket.getSeatType(),
                    ticket.getPassengerName(),
                    ticket.getPhoneNumber(),
                    ticket.getEmail(),
                    ticket.getJourneyDate().format(DATE_TIME_FORMATTER),
                    ticket.getBookingDateTime().format(DATE_TIME_FORMATTER),
                    ticket.getStatus(),
                    ticket.getPaymentMethod(),
                    ticket.getPaymentId(),
                    ticket.getTransactionReference() != null ? ticket.getTransactionReference() : "",
                    String.valueOf(ticket.getBaseFare()),
                    String.valueOf(ticket.getTax()),
                    String.valueOf(ticket.getTotalFare())
                ));
            }
        }
        
        // Atomic file replacement
        if (file.exists()) {
            file.delete();
        }
        return tempFile.renameTo(file);
    }
    
    private synchronized boolean forceSaveTrains() throws IOException {
        File file = new File(TRAINS_FILE);
        File tempFile = new File(TRAINS_FILE + ".tmp");
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
            for (Train train : trains.values()) {
                writer.println(String.format("%s|%s|%s|%s|%s|%s|%d|%.2f|%d|%d|%d",
                    train.getTrainNumber(),
                    train.getTrainName(),
                    train.getSource(),
                    train.getDestination(),
                    train.getDepartureTime().format(DATE_TIME_FORMATTER),
                    train.getArrivalTime().format(DATE_TIME_FORMATTER),
                    train.getTotalSeats(),
                    train.getBaseFare(),
                    train.getAvailableAcSeats(),
                    train.getAvailableNonAcSeats(),
                    train.getAcSeats()));
            }
        }
        
        // Atomic file replacement
        if (file.exists()) {
            file.delete();
        }
        return tempFile.renameTo(file);
    }

public void verifyTicketStatus(String pnr) {
    Ticket ticket = tickets.get(pnr);
    if (ticket == null) {
        System.out.println("Ticket " + pnr + " not found in memory");
    } else {
        System.out.println("Ticket " + pnr + " status: " + ticket.getStatus());
    }
    
    // Check file directly
    try (BufferedReader reader = new BufferedReader(new FileReader(TICKETS_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(pnr + "|")) {
                System.out.println("Ticket in file: " + line);
                break;
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading tickets file: " + e.getMessage());
    }
}

public void verifyTrainSeats(String trainNumber) {
    Train train = trains.get(trainNumber);
    if (train == null) {
        System.out.println("Train " + trainNumber + " not found");
        return;
    }
    
    System.out.println("Train " + trainNumber + " seat status:");
    System.out.println("AC Seats - Total: " + train.getAcSeats() + 
                      ", Available: " + train.getAvailableAcSeats());
    System.out.println("Non-AC Seats - Total: " + (train.getTotalSeats() - train.getAcSeats()) + 
                      ", Available: " + train.getAvailableNonAcSeats());
    
    // Check file directly
    try (BufferedReader reader = new BufferedReader(new FileReader(TRAINS_FILE))) {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith(trainNumber + "|")) {
                System.out.println("Train in file: " + line);
                break;
            }
        }
    } catch (IOException e) {
        System.err.println("Error reading trains file: " + e.getMessage());
    }
}
    private void initializeData() {
        // Create data directory if it doesn't exist
        new File(DATA_DIR).mkdirs();
        loadData();
        if (admins.isEmpty()) {
            admins.put("admin", "admin123");
            saveAdmins();
        }
    }

    // Train operations
    public synchronized boolean addTrain(Train train) {
        if (train == null || train.getTrainNumber() == null) {
            throw new IllegalArgumentException("Invalid train data");
        }
        trains.put(train.getTrainNumber(), train);
        saveTrains();
        return true;
    }

    public synchronized void deleteTrain(String trainNumber) {
        if (trainNumber == null || trainNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid train number");
        }
        trains.remove(trainNumber);
        saveTrains();
    }

    public Train getTrain(String trainNumber) {
        return trains.get(trainNumber);
    }

    public List<Train> getAllTrains() {
        return new ArrayList<>(trains.values());
    }

    // Ticket operations
    public synchronized void addTicket(Ticket ticket) {
        try {
            // Verify passenger name is preserved
            System.out.println("Saving ticket for: " + ticket.getPassengerName());
            
            tickets.put(ticket.getPnr(), ticket);
            
            // Debug output before saving
            System.out.println("Ticket data to save:");
            System.out.println("PNR: " + ticket.getPnr());
            System.out.println("Name: " + ticket.getPassengerName());
            System.out.println("Train: " + ticket.getTrain().getTrainNumber());
            // ... other important fields
            
            saveTickets(); // Save to file
        } catch (Exception e) {
            System.err.println("Error adding ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Ticket getTicket(String pnr) {
        return tickets.get(pnr);
    }

    public List<Ticket> getAllTickets() {
        // Reload tickets if empty to ensure we have latest data
        if (tickets.isEmpty()) {
            try {
                Map<String, Ticket> loadedTickets = loadTickets();
                tickets.putAll(loadedTickets);
            } catch (IOException e) {
                System.err.println("Error reloading tickets: " + e.getMessage());
            }
        }
        return new ArrayList<>(tickets.values());
    }

    public List<Ticket> getUserTickets(String userId) {
        List<Ticket> userTickets = new ArrayList<>();
        for (Ticket ticket : getAllTickets()) {
            if (ticket.getUserId().equals(userId)) {
                userTickets.add(ticket);
            }
        }
        return userTickets;
    }

    // User operations
    public synchronized void addUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        users.put(username, password);
        saveUsers();
    }

    public boolean validateUser(String username, String password) {
        if (username == null || password == null) return false;
        String storedPassword = users.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    public boolean validateAdmin(String username, String password) {
        if (username == null || password == null) return false;
        String storedPassword = admins.get(username);
        return storedPassword != null && storedPassword.equals(password);
    }

    // File operations
    private synchronized void loadData() {
        try {
            System.out.println("Loading data from files...");
            System.out.println("Trains file: " + new File(TRAINS_FILE).getAbsolutePath());
            System.out.println("Tickets file: " + new File(TICKETS_FILE).getAbsolutePath());
            
            trains.putAll(loadTrains());
            System.out.println("Loaded " + trains.size() + " trains");
            
            tickets.putAll(loadTickets());
            System.out.println("Loaded " + tickets.size() + " tickets");
            
            users.putAll(loadCredentials(USERS_FILE));
            admins.putAll(loadCredentials(ADMIN_FILE));
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Train> loadTrains() throws IOException {
        Map<String, Train> trainMap = new HashMap<>();
        File file = new File(TRAINS_FILE);
        if (!file.exists()) {
            System.out.println("Trains file not found, will be created on first save");
            return trainMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 9) {
                        Train train = new Train(
                            parts[0], parts[1], parts[2], parts[3],
                            LocalDateTime.parse(parts[4], DATE_TIME_FORMATTER),
                            LocalDateTime.parse(parts[5], DATE_TIME_FORMATTER),
                            Integer.parseInt(parts[6]),
                            Double.parseDouble(parts[7]),
                            Integer.parseInt(parts[8])
                        );
                        
                        if (parts.length >= 11) {
                            train.setAvailableAcSeats(Integer.parseInt(parts[9]));
                            train.setAvailableNonAcSeats(Integer.parseInt(parts[10]));
                        }
                        trainMap.put(train.getTrainNumber(), train);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing train: " + line);
                    e.printStackTrace();
                }
            }
        }
        return trainMap;
    }

    private Map<String, Ticket> loadTickets() throws IOException {
        Map<String, Ticket> ticketMap = new HashMap<>();
        File file = new File(TICKETS_FILE);
        if (!file.exists()) {
            System.out.println("Tickets file not found, will be created on first save");
            return ticketMap;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split("\\|");
                    if (parts.length < 14) {
                        System.err.println("Invalid ticket format at line " + lineNumber + ", expected 14+ fields, got " + parts.length);
                        continue;
                    }

                    // Parse required fields
                    String pnr = parts[0];
                    String userId = parts[1];
                    String trainNumber = parts[2];
                    int numberOfSeats = Integer.parseInt(parts[3]);
                    String seatType = parts[4];
                    String passengerName = parts[5];
                    String phoneNumber = parts[6];
                    String email = parts[7];
                    LocalDateTime journeyDate = LocalDateTime.parse(parts[8], DATE_TIME_FORMATTER);
                    LocalDateTime bookingDate = LocalDateTime.parse(parts[9], DATE_TIME_FORMATTER);
                    String status = parts[10];
                    String paymentMethod = parts[11];
                    String paymentId = parts[12];
                    String transactionRef = parts[13];

                    // Get referenced train
                    Train train = trains.get(trainNumber);
                    if (train == null) {
                        System.err.println("Train not found for ticket at line " + lineNumber + ": " + trainNumber);
                        continue;
                    }

                    // Create ticket
                    Ticket ticket = new Ticket(
                        userId, train, numberOfSeats, seatType,
                        passengerName, phoneNumber, email, journeyDate
                    );
                    ticket.setPnr(pnr);
                    ticket.setBookingDateTime(bookingDate);
                    ticket.setStatus(status);
                    ticket.setPaymentMethod(paymentMethod);
                    ticket.setPaymentId(paymentId);
                    ticket.setTransactionReference(transactionRef);

                    // Parse fare information if available
                    if (parts.length >= 17) {
                        ticket.setBaseFare(Double.parseDouble(parts[14]));
                        ticket.setTax(Double.parseDouble(parts[15]));
                        ticket.setTotalFare(Double.parseDouble(parts[16]));
                    }

                    ticketMap.put(pnr, ticket);
                } catch (Exception e) {
                    System.err.println("Error parsing ticket at line " + lineNumber + ": " + line);
                    e.printStackTrace();
                }
            }
        }
        return ticketMap;
    }

    private Map<String, String> loadCredentials(String filename) throws IOException {
        Map<String, String> credMap = new HashMap<>();
        File file = new File(filename);
        if (!file.exists()) return credMap;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    credMap.put(parts[0], parts[1]);
                }
            }
        }
        return credMap;
    }

    private void saveTrains() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TRAINS_FILE))) {
            for (Train train : trains.values()) {
                writer.println(String.format("%s|%s|%s|%s|%s|%s|%d|%.2f|%d|%d|%d",
                    train.getTrainNumber(),
                    train.getTrainName(),
                    train.getSource(),
                    train.getDestination(),
                    train.getDepartureTime().format(DATE_TIME_FORMATTER),
                    train.getArrivalTime().format(DATE_TIME_FORMATTER),
                    train.getTotalSeats(),
                    train.getBaseFare(),
                    train.getAvailableAcSeats(),
                    train.getAvailableNonAcSeats(),
                    train.getAcSeats()));
            }
        } catch (IOException e) {
            System.err.println("Error saving trains: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveTickets() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TICKETS_FILE))) {
            for (Ticket ticket : tickets.values()) {
                writer.println(String.join("|",
                    ticket.getPnr(),
                    ticket.getUserId(),
                    ticket.getTrain().getTrainNumber(),
                    String.valueOf(ticket.getNumberOfSeats()),
                    ticket.getSeatType(),
                    ticket.getPassengerName(),
                    ticket.getPhoneNumber(),
                    ticket.getEmail(),
                    ticket.getJourneyDate().format(DATE_TIME_FORMATTER),
                    ticket.getBookingDateTime().format(DATE_TIME_FORMATTER),
                    ticket.getStatus(),
                    ticket.getPaymentMethod(),
                    ticket.getPaymentId(),
                    ticket.getTransactionReference() != null ? ticket.getTransactionReference() : "",
                    String.valueOf(ticket.getBaseFare()),
                    String.valueOf(ticket.getTax()),
                    String.valueOf(ticket.getTotalFare())
                ));
            }
        } catch (IOException e) {
            System.err.println("Error saving tickets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveUsers() {
        saveCredentials(users, USERS_FILE);
    }

    private void saveAdmins() {
        saveCredentials(admins, ADMIN_FILE);
    }

    private void saveCredentials(Map<String, String> credentials, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<String, String> entry : credentials.entrySet()) {
                writer.println(entry.getKey() + ":" + entry.getValue());
            }
        } catch (IOException e) {
            System.err.println("Error saving credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public synchronized void generateReport(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Railway Booking System Report");
            writer.println("Generated at: " + LocalDateTime.now());
            writer.println("\nTrains:");
            for (Train train : trains.values()) {
                writer.println(train.getTrainNumber() + " - " + train.getTrainName());
            }
            writer.println("\nTickets:");
            for (Ticket ticket : tickets.values()) {
                writer.println(ticket.getPnr() + " - " + ticket.getPassengerName());
            }
        } catch (IOException e) {
            System.err.println("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
}