import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserMenu extends JFrame {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private String currentUserId;

    public UserMenu() {
        // First show login/register dialog
        showLoginDialog();
        
        // Configure main window
        setTitle("Railway Reservation System - User Portal");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setIconImage(new ImageIcon("./images/train1.jpg").getImage());

        // Create main panel with background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 245, 250));

        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 51, 102));
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel headerLabel = new JLabel("USER DASHBOARD - WELCOME " + currentUserId);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 20, 20));
        buttonPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        buttonPanel.setOpaque(false);

        // Create styled buttons with icons
        JButton bookTicketButton = createMenuButton("Book Ticket", "icons/book_ticket.png");
        JButton cancelTicketButton = createMenuButton("Cancel Ticket", "icons/cancel_ticket.png");
        JButton viewBookingButton = createMenuButton("My Bookings", "icons/view_bookings.png");
        JButton viewScheduleButton = createMenuButton("Train Schedule", "icons/schedule.png");
        JButton checkAvailabilityButton = createMenuButton("Seat Availability", "icons/availability.png");
        JButton printTicketButton = createMenuButton("Print Ticket", "icons/print.png");
        JButton backButton = createMenuButton("Logout", "icons/logout.png");

        // Add action listeners
        bookTicketButton.addActionListener(e -> showBookTicketDialog());
        cancelTicketButton.addActionListener(e -> showCancelTicketDialog());
        viewBookingButton.addActionListener(e -> showViewBookingDialog());
        viewScheduleButton.addActionListener(e -> showTrainSchedule());
        checkAvailabilityButton.addActionListener(e -> checkSeatAvailability());
        printTicketButton.addActionListener(e -> printTicket());
        backButton.addActionListener(e -> {
            currentUserId = null;
            dispose();
            new UserMenu(); // Return to login screen
        });

        // Add buttons to panel
        buttonPanel.add(bookTicketButton);
        buttonPanel.add(cancelTicketButton);
        buttonPanel.add(viewBookingButton);
        buttonPanel.add(viewScheduleButton);
        buttonPanel.add(checkAvailabilityButton);
        buttonPanel.add(printTicketButton);
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
        setVisible(true);
    }

    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text, new ImageIcon(iconPath));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBackground(Color.WHITE);
        button.setBorder(new CompoundBorder(
            new LineBorder(new Color(0, 51, 102), 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void showLoginDialog() {
        JDialog dialog = new JDialog((Frame)null, "User Login/Register", true);
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(null);
        dialog.setIconImage(new ImageIcon("icons/login_icon.png").getImage());

        // Create main panel with background
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setOpaque(false);

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        // Add styled components
        addFormRow(formPanel, "Username:", usernameField);
        addFormRow(formPanel, "Password:", passwordField);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton loginButton = createActionButton("Login", new Color(0, 102, 51));
        JButton registerButton = createActionButton("Register", new Color(0, 102, 204));

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (DatabaseManager.getInstance().validateUser(username, password)) {
                currentUserId = username;
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid username or password",
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.length() < 3 || password.length() < 4) {
                JOptionPane.showMessageDialog(dialog,
                    "Username must be at least 3 characters\nPassword must be at least 4 characters",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            DatabaseManager.getInstance().addUser(username, password);
            currentUserId = username;
            JOptionPane.showMessageDialog(dialog,
                "Registration successful!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
                currentUserId = username;
                JOptionPane.showMessageDialog(dialog,
                    "Registration successful!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);

        // If user closed dialog without logging in
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });

        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, String label, JComponent field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(jLabel);
        
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(150, 150, 150)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        panel.add(field);
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }

    private void showBookTicketDialog() {
        List<Train> trains = DatabaseManager.getInstance().getAllTrains();
        if (trains.isEmpty()) {
            showErrorMessage("No trains available for booking");
            return;
        }

        JDialog dialog = new JDialog(this, "Book Ticket", true);
        dialog.setSize(700, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setIconImage(new ImageIcon("icons/book_ticket.png").getImage());

        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Create form panel
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        // Create form components
        JTextField nameField = createStyledTextField();
        JTextField phoneField = createStyledTextField();
        JTextField emailField = createStyledTextField();
        JTextField seatsField = createStyledTextField();
        
        JComboBox<Train> trainComboBox = new JComboBox<>(trains.toArray(new Train[0]));
        trainComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Train) {
                    Train train = (Train) value;
                    setText(String.format("%s (%s) %s to %s", 
                        train.getTrainName(), train.getTrainNumber(),
                        train.getSource(), train.getDestination()));
                }
                return this;
            }
        });
        
        JComboBox<String> seatTypeComboBox = new JComboBox<>(new String[]{"AC", "Non-AC"});
        JComboBox<String> paymentMethodComboBox = new JComboBox<>(
            new String[]{"UPI", "Credit Card", "Debit Card", "Net Banking"});

        // Add form rows
        addFormRow(formPanel, "Passenger Name:", nameField);
        addFormRow(formPanel, "Phone Number:", phoneField);
        addFormRow(formPanel, "Email:", emailField);
        addFormRow(formPanel, "Select Train:", trainComboBox);
        addFormRow(formPanel, "Seat Type:", seatTypeComboBox);
        addFormRow(formPanel, "Number of Seats:", seatsField);
        addFormRow(formPanel, "Payment Method:", paymentMethodComboBox);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton bookButton = createActionButton("Book Ticket", new Color(0, 102, 51));
        JButton cancelButton = createActionButton("Cancel", new Color(153, 0, 0));

        bookButton.addActionListener(e -> {
            try {
                // Validate and process booking
                Train selectedTrain = (Train) trainComboBox.getSelectedItem();
                String seatType = (String) seatTypeComboBox.getSelectedItem();
                int seats = Integer.parseInt(seatsField.getText());
                String paymentMethod = (String) paymentMethodComboBox.getSelectedItem();
                String passengerName = nameField.getText().trim();
                String phoneNumber = phoneField.getText().trim();
                String email = emailField.getText().trim();
                LocalDateTime journeyDate = LocalDateTime.now().plusDays(1);

                if (passengerName.isEmpty() || phoneNumber.isEmpty()) {
                    throw new IllegalArgumentException("Name and phone number are required");
                }

                if (selectedTrain.bookSeats(seats, seatType)) {
                    Ticket ticket = new Ticket(currentUserId, selectedTrain, seats, seatType,
                                            passengerName, phoneNumber, email, journeyDate);
                    
                    // Show payment confirmation
                    int confirm = JOptionPane.showConfirmDialog(dialog,
                        createPaymentConfirmationPanel(ticket),
                        "Confirm Payment",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.PLAIN_MESSAGE);
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        String paymentId = "PAY" + System.currentTimeMillis();
                        String txnRef = "TXN" + System.currentTimeMillis();
                        ticket.setPaymentDetails(paymentMethod, paymentId);
                        DatabaseManager.getInstance().addTicket(ticket);
                        
                        // Show ticket details
                        showTicketDetails(ticket, "Booking Successful");
                        dialog.dispose();
                    } else {
                        selectedTrain.cancelSeats(seats, seatType);
                    }
                } else {
                    showErrorMessage("Not enough " + seatType + " seats available");
                }
            } catch (Exception ex) {
                showErrorMessage(ex.getMessage());
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private JPanel createPaymentConfirmationPanel(Ticket ticket) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailsArea.setText(String.format(
            "Payment Details:\n\n" +
            "Passenger: %s\n" +
            "Train: %s (%s)\n" +
            "Seat Type: %s\n" +
            "Number of Seats: %d\n" +
            "Base Fare: ₹%.2f\n" +
            "Tax (18%%): ₹%.2f\n" +
            "Total Fare: ₹%.2f\n\n" +
            "Proceed with payment?",
            ticket.getPassengerName(),
            ticket.getTrain().getTrainName(),
            ticket.getTrain().getTrainNumber(),
            ticket.getSeatType(),
            ticket.getNumberOfSeats(),
            ticket.getBaseFare(),
            ticket.getTax(),
            ticket.getTotalFare()
        ));

        panel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        return panel;
    }

    private void showTicketDetails(Ticket ticket, String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(600, 700);
        dialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea(ticket.generateTicketPrintout());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JButton closeButton = createActionButton("Close", new Color(0, 51, 102));
        closeButton.addActionListener(e -> dialog.dispose());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(closeButton, BorderLayout.SOUTH);
        
        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
            new LineBorder(new Color(150, 150, 150)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return field;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }

    private void showCancelTicketDialog() {
        String pnr = JOptionPane.showInputDialog(this, 
            "Enter PNR Number to cancel:",
            "Cancel Ticket",
            JOptionPane.QUESTION_MESSAGE);
    
        if (pnr == null || pnr.trim().isEmpty()) {
            return;
        }
        pnr = pnr.trim();
    
        Ticket ticket = DatabaseManager.getInstance().getTicket(pnr);
        if (ticket == null) {
            JOptionPane.showMessageDialog(this,
                "Ticket with PNR " + pnr + " not found",
                "Not Found",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        if (!ticket.getUserId().equals(currentUserId)) {
            JOptionPane.showMessageDialog(this,
                "You can only cancel your own tickets",
                "Unauthorized",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Cancel this ticket?</b><br><br>" +
            "PNR: " + ticket.getPnr() + "<br>" +
            "Passenger: " + ticket.getPassengerName() + "<br>" +
            "Train: " + ticket.getTrain().getTrainNumber() + "<br>" +
            "Date: " + ticket.getFormattedJourneyDate() + "<br>" +
            "Status: " + ticket.getStatus() + "<br>" +
            "Fare: ₹" + String.format("%.2f", ticket.getTotalFare()) + "</html>",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
    
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = DatabaseManager.getInstance().cancelTicket(pnr);
            
            if (success) {
                JOptionPane.showMessageDialog(this,
                    "Ticket cancelled successfully",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh the display
                // refreshMyBookings(); // Call your existing refresh method
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to cancel ticket",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void showViewBookingDialog() {
        String pnr = JOptionPane.showInputDialog(this, "Enter PNR number:");
        if (pnr != null) {
            Ticket ticket = DatabaseManager.getInstance().getTicket(pnr);
            if (ticket != null && ticket.getUserId().equals(currentUserId)) {
                JOptionPane.showMessageDialog(this,
                    ticket.generateTicketPrintout(),
                    "Ticket Details",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid PNR or unauthorized access",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTrainSchedule() {
        JDialog dialog = new JDialog(this, "Train Schedule", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        for (Train train : DatabaseManager.getInstance().getAllTrains()) {
            textArea.append(String.format("Train: %s (%s)\n", train.getTrainName(), train.getTrainNumber()));
            textArea.append(String.format("Route: %s to %s\n", train.getSource(), train.getDestination()));
            textArea.append(String.format("Departure: %s\n", train.getDepartureTime().format(DATE_TIME_FORMATTER)));
            textArea.append(String.format("Arrival: %s\n", train.getArrivalTime().format(DATE_TIME_FORMATTER)));
            textArea.append(String.format("Available Seats: %d\n", train.getAvailableSeats()));
            textArea.append(String.format("Base Fare: ₹%.2f\n", train.getBaseFare()));
            textArea.append("----------------------------------------\n");
        }

        dialog.add(scrollPane);
        dialog.setVisible(true);
    }

    private void checkSeatAvailability() {
        List<Train> trains = DatabaseManager.getInstance().getAllTrains();
        if (trains.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No trains available",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, "Seat Availability", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout());
        JComboBox<Train> trainComboBox = new JComboBox<>(trains.toArray(new Train[0]));
        JLabel availabilityLabel = new JLabel();

        trainComboBox.addActionListener(e -> {
            Train selectedTrain = (Train) trainComboBox.getSelectedItem();
            availabilityLabel.setText(String.format("Available Seats: %d", selectedTrain.getAvailableSeats()));
        });

        panel.add(trainComboBox, BorderLayout.NORTH);
        panel.add(availabilityLabel, BorderLayout.CENTER);
        panel.add(new JButton("Close") {{ addActionListener(e -> dialog.dispose()); }}, 
            BorderLayout.SOUTH);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private void printTicket() {
        String pnr = JOptionPane.showInputDialog(this, "Enter PNR number to print ticket:");
        if (pnr != null) {
            Ticket ticket = DatabaseManager.getInstance().getTicket(pnr);
            if (ticket != null && ticket.getUserId().equals(currentUserId)) {
                JDialog dialog = new JDialog(this, "Print Ticket", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(this);

                JTextArea textArea = new JTextArea(ticket.generateTicketPrintout());
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);

                JButton printButton = new JButton("Print");
                printButton.addActionListener(e -> {
                    JOptionPane.showMessageDialog(dialog,
                        "Ticket sent to printer",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                });

                dialog.setLayout(new BorderLayout());
                dialog.add(scrollPane, BorderLayout.CENTER);
                dialog.add(printButton, BorderLayout.SOUTH);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Invalid PNR or unauthorized access",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}