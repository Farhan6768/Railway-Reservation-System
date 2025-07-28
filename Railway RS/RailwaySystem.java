import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class RailwaySystem {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for native appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                
                // Custom UI improvements
                UIManager.put("Button.focus", new Color(0, 0, 0, 0)); // Remove focus border
                new MainMenu();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to default if system L&F fails
                new MainMenu();
            }
        });
    }
}

class MainMenu extends JFrame {
    public MainMenu() {
        // Configure main window
        setTitle("Railway Reservation System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon("icons/train_icon.png").getImage());

        // Create main panel with background
        JPanel mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw background image if available
                try {
                    ImageIcon bgImage = new ImageIcon("./images/bgimage.jpg");
                    g.drawImage(bgImage.getImage(), 0, 0, getWidth(), getHeight(), this);
                } catch (Exception e) {
                    // Fallback to gradient background
                    Graphics2D g2d = (Graphics2D) g;
                    Color color1 = new Color(0, 51, 102);
                    Color color2 = new Color(0, 102, 153);
                    GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        // Create header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        JLabel headerLabel = new JLabel("RAILWAY RESERVATION SYSTEM");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 20, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(40, 100, 40, 100));

        // Create styled buttons
        JButton adminButton = createMenuButton("ADMIN PORTAL", "icons/admin.png");
        JButton userButton = createMenuButton("USER PORTAL", "icons/user.png");
        JButton exitButton = createMenuButton("EXIT SYSTEM", "icons/exit.png");

        // Add action listeners
        adminButton.addActionListener(e -> {
            dispose();
            new AdminLogin();
        });
        
        userButton.addActionListener(e -> {
            dispose();
            new UserMenu();
        });
        
        exitButton.addActionListener(e -> System.exit(0));

        // Add buttons to panel
        buttonPanel.add(adminButton);
        buttonPanel.add(userButton);
        buttonPanel.add(exitButton);

        // Create footer panel
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("© 2023 Railway Reservation System");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        
        mainPanel.add(buttonPanel, BorderLayout.CENTER);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private JButton createMenuButton(String text, String iconPath) {
        JButton button = new JButton(text, new ImageIcon(iconPath));
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setForeground(new Color(0, 51, 102));
        button.setBackground(Color.WHITE);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setBorder(new CompoundBorder(
            new LineBorder(new Color(255, 255, 255, 150), 2),
            new EmptyBorder(15, 15, 15, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        
        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
                button.setBorder(new CompoundBorder(
                    new LineBorder(new Color(255, 255, 255, 200), 2),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setBorder(new CompoundBorder(
                    new LineBorder(new Color(255, 255, 255, 150), 2),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        });
        
        return button;
    }

    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }

    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, 
            message, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}