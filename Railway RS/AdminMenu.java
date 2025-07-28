import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.MessageFormat;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class AdminMenu extends JFrame {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Color PRIMARY_COLOR = new Color(0, 51, 102);
    private static final Color SECONDARY_COLOR = new Color(220, 240, 255);

    public AdminMenu() {
        initializeUI();
        setupWindowListener();
    }

    private void initializeUI() {
        setTitle("Railway System - Admin Dashboard");
        setSize(900, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(new ImageIcon("icons/train1.jpg").getImage());

        // Main panel with background image
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            private Image backgroundImage;
            {
                try {
                    backgroundImage = new ImageIcon("./images/train1.jpg").getImage();
                } catch (Exception e) {
                    backgroundImage = null;
                    setBackground(SECONDARY_COLOR);
                }
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g.setColor(new Color(255, 255, 255, 200));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header panel
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                    0, 0, PRIMARY_COLOR, 
                    getWidth(), 0, new Color(70, 130, 180));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JLabel headerLabel = new JLabel("ADMINISTRATOR DASHBOARD");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 20, 20));
        buttonPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        buttonPanel.setOpaque(false);

        // Create and add buttons
        addAdminButton(buttonPanel, "Add Train", "icons/addtrain.jpg", e -> showAddTrainDialog());
        addAdminButton(buttonPanel, "Modify Train", "icons/edit_train.png", e -> showModifyTrainDialog());
        addAdminButton(buttonPanel, "Delete Train", "icons/delete_train.png", e -> showDeleteTrainDialog());
        addAdminButton(buttonPanel, "View Bookings", "icons/view_bookings.png", e -> showAllBookings());
        addAdminButton(buttonPanel, "Generate Report", "icons/report.png", e -> generateReport());
        addAdminButton(buttonPanel, "Logout", "icons/logout.png", e -> confirmLogout());

        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        add(mainPanel);
    }

    private void addAdminButton(JPanel panel, String text, String iconPath, ActionListener action) {
        JButton button = new JButton(text, new ImageIcon(iconPath));
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBackground(Color.WHITE);
        button.setBorder(new CompoundBorder(
            new LineBorder(PRIMARY_COLOR, 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        panel.add(button);
    }

    private void setupWindowListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmLogout();
            }
        });
    }

    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private void generateReport() {
        String filename = "report_" + 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
            ".txt";
        
        DatabaseManager.getInstance().generateReport(filename);
        
        int choice = JOptionPane.showOptionDialog(this,
            "Report generated successfully!\n" +
            "File: " + filename + "\n\n" +
            "Would you like to open the report now?",
            "Report Generated",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new Object[]{"Open Report", "Close"},
            "Open Report");
            
        if (choice == JOptionPane.YES_OPTION) {
            try {
                Desktop.getDesktop().open(new File(filename));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Could not open report: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddTrainDialog() {
        JDialog dialog = new JDialog(this, "Add New Train", true);
        dialog.setSize(650, 750);
        dialog.setLocationRelativeTo(this);
        dialog.setIconImage(new ImageIcon("icons/add_train.png").getImage());
    
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 248, 255));
    
        JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 15));
        formPanel.setOpaque(false);
        
        JTextField numberField = createFormFieldWithPlaceholder("e.g., 12345");
        JTextField nameField = createFormFieldWithPlaceholder("e.g., Rajdhani Express");
        JTextField sourceField = createFormFieldWithPlaceholder("e.g., Delhi");
        JTextField destField = createFormFieldWithPlaceholder("e.g., Mumbai");
        JTextField departureField = createFormFieldWithPlaceholder("yyyy-MM-dd HH:mm");
        JTextField arrivalField = createFormFieldWithPlaceholder("yyyy-MM-dd HH:mm");
        JTextField seatsField = createFormFieldWithPlaceholder("e.g., 300");
        JTextField acSeatsField = createFormFieldWithPlaceholder("e.g., 100");
        JTextField fareField = createFormFieldWithPlaceholder("e.g., 500.00");
    
        addFormRow(formPanel, "Train Number*:", numberField);
        addFormRow(formPanel, "Train Name*:", nameField);
        addFormRow(formPanel, "Source Station*:", sourceField);
        addFormRow(formPanel, "Destination Station*:", destField);
        addFormRow(formPanel, "Departure Time*:", departureField);
        addFormRow(formPanel, "Arrival Time*:", arrivalField);
        addFormRow(formPanel, "Total Seats*:", seatsField);
        addFormRow(formPanel, "AC Seats*:", acSeatsField);
        addFormRow(formPanel, "Base Fare (₹)*:", fareField);
    
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton addButton = createGradientButton("Add Train", new Color(46, 125, 50), new Color(56, 142, 60));
        JButton cancelButton = createGradientButton("Cancel", new Color(198, 40, 40), new Color(183, 28, 28));
    
        addButton.addActionListener(e -> {
            try {
                if (numberField.getText().isEmpty() || numberField.getText().equals("e.g., 12345") ||
                    nameField.getText().isEmpty() || nameField.getText().equals("e.g., Rajdhani Express") ||
                    sourceField.getText().isEmpty() || sourceField.getText().equals("e.g., Delhi") ||
                    destField.getText().isEmpty() || destField.getText().equals("e.g., Mumbai") ||
                    departureField.getText().isEmpty() || departureField.getText().equals("yyyy-MM-dd HH:mm") ||
                    arrivalField.getText().isEmpty() || arrivalField.getText().equals("yyyy-MM-dd HH:mm") ||
                    seatsField.getText().isEmpty() || seatsField.getText().equals("e.g., 300") ||
                    acSeatsField.getText().isEmpty() || acSeatsField.getText().equals("e.g., 100") ||
                    fareField.getText().isEmpty() || fareField.getText().equals("e.g., 500.00")) {
                    throw new IllegalArgumentException("All fields marked with * are required");
                }
    
                LocalDateTime departure = LocalDateTime.parse(departureField.getText(), DATE_TIME_FORMATTER);
                LocalDateTime arrival = LocalDateTime.parse(arrivalField.getText(), DATE_TIME_FORMATTER);
                
                if (arrival.isBefore(departure)) {
                    throw new IllegalArgumentException("Arrival time must be after departure time");
                }
    
                int seats = Integer.parseInt(seatsField.getText());
                int acSeats = Integer.parseInt(acSeatsField.getText());
                
                if (acSeats > seats) {
                    throw new IllegalArgumentException("AC seats cannot exceed total seats");
                }
    
                double fare = Double.parseDouble(fareField.getText());
                if (fare <= 0) {
                    throw new IllegalArgumentException("Fare must be positive");
                }
    
                Train train = new Train(
                    numberField.getText(),
                    nameField.getText(),
                    sourceField.getText(),
                    destField.getText(),
                    departure,
                    arrival,
                    seats,
                    fare,
                    acSeats
                );
    
                if (DatabaseManager.getInstance().addTrain(train)) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Train added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                } else {
                    throw new Exception("Failed to add train. It may already exist.");
                }
            } catch (DateTimeParseException ex) {
                showErrorDialog(dialog, "Invalid date/time format. Please use yyyy-MM-dd HH:mm");
            } catch (NumberFormatException ex) {
                showErrorDialog(dialog, "Invalid number format for seats or fare");
            } catch (IllegalArgumentException ex) {
                showErrorDialog(dialog, ex.getMessage());
            } catch (Exception ex) {
                showErrorDialog(dialog, "Error: " + ex.getMessage());
            }
        });
    
        cancelButton.addActionListener(e -> dialog.dispose());
    
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
    
        panel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        dialog.add(panel);
        dialog.setVisible(true);
    }
    
    private JTextField createFormFieldWithPlaceholder(String placeholder) {
        JTextField field = new JTextField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, new Color(0, 51, 102)),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.setForeground(Color.GRAY);
        field.setText(placeholder);
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        
        return field;
    }
    
    private JButton createGradientButton(String text, Color topColor, Color bottomColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gp = new GradientPaint(
                    0, 0, topColor, 
                    0, getHeight(), bottomColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                super.paintComponent(g);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(new Color(245, 245, 245));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(Color.WHITE);
            }
        });
        
        return button;
    }

    private void addFormRow(JPanel panel, String label, JComponent field) {
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        jLabel.setForeground(PRIMARY_COLOR);
        panel.add(jLabel);
        panel.add(field);
    }

    private void showErrorDialog(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    private void showModifyTrainDialog() {
        String trainNumber = JOptionPane.showInputDialog(this, 
            "Enter Train Number to modify:");
        
        if (trainNumber != null && !trainNumber.trim().isEmpty()) {
            Train train = DatabaseManager.getInstance().getTrain(trainNumber);
            if (train != null) {
                JDialog dialog = new JDialog(this, "Modify Train", true);
                dialog.setSize(500, 600);
                dialog.setLocationRelativeTo(this);
                dialog.setIconImage(new ImageIcon("icons/edit_train.png").getImage());

                JPanel panel = new JPanel(new BorderLayout());
                panel.setBorder(new EmptyBorder(15, 20, 15, 20));

                JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));

                JTextField nameField = createStyledTextField(train.getTrainName());
                JTextField sourceField = createStyledTextField(train.getSource());
                JTextField destField = createStyledTextField(train.getDestination());
                JTextField departureField = createStyledTextField(train.getDepartureTime().format(DATE_TIME_FORMATTER));
                JTextField arrivalField = createStyledTextField(train.getArrivalTime().format(DATE_TIME_FORMATTER));
                JTextField seatsField = createStyledTextField(String.valueOf(train.getTotalSeats()));
                JTextField acSeatsField = createStyledTextField(String.valueOf(train.getAcSeats()));
                JTextField fareField = createStyledTextField(String.valueOf(train.getBaseFare()));

                addFormRow(formPanel, "Train Number:", new JLabel(train.getTrainNumber()));
                addFormRow(formPanel, "Train Name:", nameField);
                addFormRow(formPanel, "Source:", sourceField);
                addFormRow(formPanel, "Destination:", destField);
                addFormRow(formPanel, "Departure (yyyy-MM-dd HH:mm):", departureField);
                addFormRow(formPanel, "Arrival (yyyy-MM-dd HH:mm):", arrivalField);
                addFormRow(formPanel, "Total Seats:", seatsField);
                addFormRow(formPanel, "AC Seats:", acSeatsField);
                addFormRow(formPanel, "Base Fare (₹):", fareField);

                JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                JButton updateButton = createActionButton("Update", new Color(0, 102, 51));
                JButton cancelButton = createActionButton("Cancel", new Color(153, 0, 0));

                updateButton.addActionListener(e -> {
                    try {
                        LocalDateTime departure = LocalDateTime.parse(departureField.getText(), DATE_TIME_FORMATTER);
                        LocalDateTime arrival = LocalDateTime.parse(arrivalField.getText(), DATE_TIME_FORMATTER);
                        if (arrival.isBefore(departure)) {
                            throw new IllegalArgumentException("Arrival time must be after departure time");
                        }

                        int seats = Integer.parseInt(seatsField.getText());
                        int acSeats = Integer.parseInt(acSeatsField.getText());
                        if (acSeats > seats) {
                            throw new IllegalArgumentException("AC seats cannot exceed total seats");
                        }

                        train.setTrainName(nameField.getText());
                        train.setSource(sourceField.getText());
                        train.setDestination(destField.getText());
                        train.setDepartureTime(departure);
                        train.setArrivalTime(arrival);
                        train.setTotalSeats(seats);
                        train.setAcSeats(acSeats);
                        train.setBaseFare(Double.parseDouble(fareField.getText()));

                        DatabaseManager.getInstance().addTrain(train);
                        JOptionPane.showMessageDialog(dialog, 
                            "Train updated successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog, 
                            ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                });

                cancelButton.addActionListener(e -> dialog.dispose());

                buttonPanel.add(updateButton);
                buttonPanel.add(cancelButton);

                panel.add(new JScrollPane(formPanel), BorderLayout.CENTER);
                panel.add(buttonPanel, BorderLayout.SOUTH);
                dialog.add(panel);
                dialog.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Train not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDeleteTrainDialog() {
        String trainNumber = JOptionPane.showInputDialog(this, 
            "Enter Train Number to delete:");
        
        if (trainNumber != null && !trainNumber.trim().isEmpty()) {
            Train train = DatabaseManager.getInstance().getTrain(trainNumber);
            if (train != null) {
                long ticketCount = DatabaseManager.getInstance().getAllTickets().stream()
                    .filter(t -> t.getTrain().getTrainNumber().equals(trainNumber))
                    .count();

                if (ticketCount > 0) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                        "Warning: This train has " + ticketCount + " bookings!\n" +
                        "Deleting it will cancel all associated tickets.\n" +
                        "Are you sure you want to proceed?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    if (confirm != JOptionPane.YES_OPTION) return;
                }

                int finalConfirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete train " + trainNumber + "?",
                    "Final Confirmation",
                    JOptionPane.YES_NO_OPTION);
                
                if (finalConfirm == JOptionPane.YES_OPTION) {
                    DatabaseManager.getInstance().deleteTrain(trainNumber);
                    JOptionPane.showMessageDialog(this, 
                        "Train deleted successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Train not found!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAllBookings() {
        JDialog dialog = new JDialog(this, "All Bookings", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);
        
        // Create main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);
    
        // Create text area for displaying bookings
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        textArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Load and format the bookings data
        List<Ticket> tickets = DatabaseManager.getInstance().getAllTickets();
        if (tickets.isEmpty()) {
            textArea.setText("No bookings found.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (Ticket ticket : tickets) {
                sb.append("PNR: ").append(ticket.getPnr()).append("\n");
                sb.append("Passenger: ").append(ticket.getPassengerName()).append("\n");
                sb.append("Train: ").append(ticket.getTrain().getTrainName())
                  .append(" (").append(ticket.getTrain().getTrainNumber()).append(")\n");
                sb.append("Route: ").append(ticket.getTrain().getSource())
                  .append(" to ").append(ticket.getTrain().getDestination()).append("\n");
                sb.append("Date: ").append(ticket.getFormattedJourneyDate()).append("\n");
                sb.append("Class: ").append(ticket.getSeatType()).append(", Seats: ")
                  .append(ticket.getNumberOfSeats()).append("\n");
                sb.append("Status: ").append(ticket.getStatus()).append("\n");
                sb.append("Fare: ₹").append(String.format("%.2f", ticket.getTotalFare())).append("\n");
                sb.append("----------------------------------------\n");
            }
            textArea.setText(sb.toString());
        }
    
        // Add to scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Add to dialog
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        dialog.add(mainPanel);
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

    private JTextField createStyledTextField(String text) {
        JTextField field = createStyledTextField();
        field.setText(text);
        return field;
    }

    private JButton createActionButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
        return button;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                new AdminMenu().setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}