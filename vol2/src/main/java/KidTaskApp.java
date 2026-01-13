import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// --- 1. DATA MODELS ---
class Task {
    String title, description, dueDate, status, creatorRole;
    int points;

    public Task(String title, String description, String dueDate, int points, String status, String creatorRole) {
        this.title = title; this.description = description; this.dueDate = dueDate;
        this.points = points; this.status = status; this.creatorRole = creatorRole;
    }

    public String toFileString() {
        return title + "|" + description + "|" + dueDate + "|" + points + "|" + status + "|" + creatorRole;
    }
}

class Wish {
    String name;
    int pointsRequired;
    boolean isApproved;

    public Wish(String name, int pointsRequired, boolean isApproved) {
        this.name = name; this.pointsRequired = pointsRequired; this.isApproved = isApproved;
    }

    public String toFileString() {
        return name + "|" + pointsRequired + "|" + isApproved;
    }
}

// --- CUSTOM UI COMPONENTS ---

// Panel with a gradient background
class GradientPanel extends JPanel {
    private Color color1, color2;

    public GradientPanel(Color color1, Color color2) {
        this.color1 = color1;
        this.color2 = color2;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, color1, w, h, color2);
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, w, h);
    }
}

// Panel with rounded corners
class RoundedPanel extends JPanel {
    private Color backgroundColor;
    private int cornerRadius = 20;

    public RoundedPanel(Color bgColor) {
        this.backgroundColor = bgColor;
        setOpaque(false);
    }
    
    public RoundedPanel(Color bgColor, int radius) {
        this.backgroundColor = bgColor;
        this.cornerRadius = radius;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(backgroundColor);
        g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
    }
}

// A styled button with gradient and rounded corners
class StyledButton extends JButton {
    private Color color1, color2;

    public StyledButton(String text, Color c1, Color c2) {
        super(text);
        this.color1 = c1;
        this.color2 = c2;
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
        setForeground(Color.WHITE);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        GradientPaint gp = new GradientPaint(0, 0, color1, w, 0, color2);
        g2d.setPaint(gp);
        g2d.fill(new RoundRectangle2D.Float(0, 0, w, h, 15, 15));
        g2d.dispose();
        super.paintComponent(g);
    }
}

// Navigation Menu Item
class NavItem extends JLabel {
    private boolean isActive = false;
    public NavItem(String text, String iconEmoji) {
        super(iconEmoji + " " + text);
        setFont(new Font("SansSerif", Font.BOLD, 14));
        setForeground(Color.GRAY);
        setBorder(new EmptyBorder(10, 15, 10, 15));
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public void setActive(boolean active) {
        this.isActive = active;
        setForeground(active ? new Color(255, 105, 180) : Color.GRAY); // Pink for active
    }
}


// --- MAIN APPLICATION ---
public class KidTaskApp extends JFrame {
    private String userRole = "Child";
    private int totalPoints = 0, currentLevel = 1;
    private int completedTasks = 0, approvedWishes = 0, dailyStreak = 0;
    private String lastLoginDate = "";

    private List<Task> tasks = new ArrayList<>();
    private List<Wish> wishes = new ArrayList<>();

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // Navigation Items
    private NavItem navHome, navTasks, navWishes, navProgress;
    private JLabel userRoleLabel;


    public KidTaskApp() {
        setupDataFolder();
        loadData();
        checkStreak();
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        showLogin();
    }

    private void setupDataFolder() {
        File folder = new File("data");
        if (!folder.exists()) folder.mkdir();
    }
    
    private void checkStreak() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(new Date());
        if (!today.equals(lastLoginDate)) {
            if (!lastLoginDate.isEmpty()) {
                // Simple streak logic: if last login was yesterday, increment. Else reset.
                // For a real app, you'd need date arithmetic. Here we just increment for a new day.
                dailyStreak++;
            } else {
                dailyStreak = 1;
            }
            lastLoginDate = today;
        }
    }


    private void showLogin() {
        // Using Turkish as in the screenshots
        String[] options = {"Öğrenci", "Ebeveyn", "Öğretmen"};
        int choice = JOptionPane.showOptionDialog(null, "KidTask'a Hoş Geldiniz\nLütfen rolünüzü seçin:", 
                "Giriş Yap", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == -1) System.exit(0);
        userRole = options[choice];
        initUI();
    }

    private void initUI() {
        setTitle("KidTask");
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main background gradient
        GradientPanel backgroundPanel = new GradientPanel(new Color(255, 182, 193), new Color(147, 112, 219));
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Logo & Role
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        logoPanel.setOpaque(false);
        JLabel logoLabel = new JLabel("🐵 KidTask");
        logoLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        logoLabel.setForeground(Color.WHITE);
        userRoleLabel = new JLabel(userRole);
        userRoleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        userRoleLabel.setForeground(new Color(240, 240, 240));
        logoPanel.add(logoLabel);
        logoPanel.add(userRoleLabel);

        // Navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        navPanel.setOpaque(false);
        navHome = new NavItem("Ana Sayfa", "🏠");
        navTasks = new NavItem("Görevler", "📝");
        navWishes = new NavItem("Dilekler", "🎁");
        navProgress = new NavItem("İlerleme", "⭐");
        
        navHome.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { showView("HOME"); } });
        navTasks.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { showView("TASKS"); } });
        navWishes.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { showView("WISHES"); } });
        navProgress.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { showView("PROGRESS"); } });
        
        navPanel.add(navHome);
        navPanel.add(navTasks);
        navPanel.add(navWishes);
        navPanel.add(navProgress);

        // Logout Button
        StyledButton logoutBtn = new StyledButton("Çıkış Yap", new Color(255, 105, 180), new Color(255, 69, 0));
        logoutBtn.addActionListener(e -> { saveData(); System.exit(0); });

        headerPanel.add(logoPanel, BorderLayout.WEST);
        headerPanel.add(navPanel, BorderLayout.CENTER);
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        backgroundPanel.add(headerPanel, BorderLayout.NORTH);

        // --- MAIN CONTENT CONTAINER ---
        RoundedPanel contentContainer = new RoundedPanel(Color.WHITE, 30);
        contentContainer.setLayout(new BorderLayout());
        contentContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Use CardLayout for switching views
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setOpaque(false);

        mainContentPanel.add(createHomeView(), "HOME");
        mainContentPanel.add(createTasksView(), "TASKS");
        mainContentPanel.add(createWishesView(), "WISHES");
        mainContentPanel.add(createProgressView(), "PROGRESS");
        
        contentContainer.add(mainContentPanel, BorderLayout.CENTER);
        
        // Add padding around the container
        JPanel paddingPanel = new JPanel(new BorderLayout());
        paddingPanel.setOpaque(false);
        paddingPanel.setBorder(new EmptyBorder(20, 40, 40, 40));
        paddingPanel.add(contentContainer, BorderLayout.CENTER);

        backgroundPanel.add(paddingPanel, BorderLayout.CENTER);

        showView("HOME"); // Show home view by default
        setVisible(true);
    }
    
    private void showView(String viewName) {
        cardLayout.show(mainContentPanel, viewName);
        navHome.setActive(viewName.equals("HOME"));
        navTasks.setActive(viewName.equals("TASKS"));
        navWishes.setActive(viewName.equals("WISHES"));
        navProgress.setActive(viewName.equals("PROGRESS"));
        if (viewName.equals("HOME") || viewName.equals("PROGRESS")) {
            // Refresh these views when shown
            mainContentPanel.add(createHomeView(), "HOME");
            mainContentPanel.add(createProgressView(), "PROGRESS");
        }
    }


    // --- VIEW CREATION METHODS ---

    private JPanel createHomeView() {
        JPanel homePanel = new JPanel(new BorderLayout(0, 20));
        homePanel.setOpaque(false);

        // Dashboard Cards
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setOpaque(false);

        cardsPanel.add(createDashboardCard("Bugünün Motivasyonu", "🏆", totalPoints + "", "Harika gidiyorsun!", new Color(255, 223, 0), new Color(255, 165, 0)));
        cardsPanel.add(createDashboardCard("Seviyem", "🎯", currentLevel + "", "Devam et!", new Color(173, 216, 230), new Color(135, 206, 250)));
        
        long pendingCount = tasks.stream().filter(t -> t.status.equals("Pending")).count();
        cardsPanel.add(createDashboardCard("Bekleyen Görevler", "✅", pendingCount + "", "Hadi başlayalım!", new Color(144, 238, 144), new Color(60, 179, 113)));

        // Motivation Section
        RoundedPanel motivationPanel = new RoundedPanel(new Color(245, 245, 245), 20);
        motivationPanel.setLayout(new BorderLayout());
        motivationPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel motivationTitle = new JLabel("🎉 Bugünün Motivasyonu");
        motivationTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        motivationTitle.setForeground(new Color(100, 100, 100));
        
        JPanel iconsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        iconsPanel.setOpaque(false);
        iconsPanel.add(createMotivationIcon("🐵", "Maymun Milo"));
        iconsPanel.add(createMotivationIcon("🍕", "Pizza Ödülü"));
        iconsPanel.add(createMotivationIcon("🦁", "Aslan Leo"));
        iconsPanel.add(createMotivationIcon("🍦", "Dondurma"));
        iconsPanel.add(createMotivationIcon("🐼", "Panda Pam"));
        
        motivationPanel.add(motivationTitle, BorderLayout.NORTH);
        motivationPanel.add(iconsPanel, BorderLayout.CENTER);

        homePanel.add(cardsPanel, BorderLayout.NORTH);
        homePanel.add(motivationPanel, BorderLayout.CENTER);

        return homePanel;
    }

    private JPanel createDashboardCard(String title, String icon, String value, String subtitle, Color c1, Color c2) {
        GradientPanel card = new GradientPanel(c1, c2);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(icon + " " + title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 36));
        valueLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(240, 240, 240));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subtitleLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createMotivationIcon(String icon, String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 40));
        JLabel textLabel = new JLabel(text, SwingConstants.CENTER);
        textLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textLabel.setForeground(Color.GRAY);
        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(textLabel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTasksView() {
        JPanel tasksPanel = new JPanel(new BorderLayout(0, 20));
        tasksPanel.setOpaque(false);
        
        // Header with "Add Task" button
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("📝 Görevlerim");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(100, 100, 100));
        
        StyledButton addTaskBtn = new StyledButton("+ Yeni Görev Ekle", new Color(50, 205, 50), new Color(34, 139, 34));
        addTaskBtn.addActionListener(e -> showAddTaskDialog());
        
        header.add(title, BorderLayout.WEST);
        if (!userRole.equals("Öğrenci")) {
            header.add(addTaskBtn, BorderLayout.EAST);
        }

        // Task List or Empty State
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        
        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("Henüz görev yok. Hadi bir tane ekleyelim! 🚀", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            emptyLabel.setForeground(Color.GRAY);
            content.add(emptyLabel, BorderLayout.CENTER);
        } else {
            String[] columns = {"Görev", "Bitiş Tarihi", "Puan", "Durum", "Atayan"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (Task t : tasks) {
                 String statusDisplay = t.status.equals("Pending") ? "Bekliyor" : (t.status.equals("Completed") ? "Tamamlandı (Onay Bekliyor)" : "Onaylandı");
                model.addRow(new Object[]{t.title, t.dueDate, t.points, statusDisplay, t.creatorRole});
            }
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            content.add(new JScrollPane(table), BorderLayout.CENTER);

            // Action Button
            StyledButton actionBtn = new StyledButton(userRole.equals("Öğrenci") ? "Görevi Tamamla" : "Seçileni Onayla", new Color(100, 149, 237), new Color(65, 105, 225));
            actionBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row != -1) handleTaskAction(row);
            });
            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            btnPanel.setOpaque(false);
            btnPanel.setBorder(new EmptyBorder(10,0,0,0));
            btnPanel.add(actionBtn);
            content.add(btnPanel, BorderLayout.SOUTH);
        }

        tasksPanel.add(header, BorderLayout.NORTH);
        tasksPanel.add(content, BorderLayout.CENTER);
        return tasksPanel;
    }
    
    // Custom Dialog for Adding Task
    private void showAddTaskDialog() {
        JDialog dialog = new JDialog(this, "Yeni Görev Ekle 🎯", true);
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        RoundedPanel panel = new RoundedPanel(Color.WHITE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 15));
        formPanel.setOpaque(false);
        
        JTextField titleField = createStyledTextField("Görev Başlığı");
        JTextArea descArea = createStyledTextArea("Açıklama");
        JTextField dateField = createStyledTextField("Bitiş Tarihi (gg.aa.yyyy)");
        JTextField pointsField = createStyledTextField("Puan");
        pointsField.setText("10"); // Default value

        formPanel.add(createFormField("Görev Başlığı", titleField));
        formPanel.add(createFormField("Açıklama", new JScrollPane(descArea)));
        
        JPanel bottomFields = new JPanel(new GridLayout(1, 2, 20, 0));
        bottomFields.setOpaque(false);
        bottomFields.add(createFormField("Bitiş Tarihi", dateField));
        bottomFields.add(createFormField("Puan", pointsField));
        formPanel.add(bottomFields);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        StyledButton addBtn = new StyledButton("Ekle", new Color(255, 105, 180), new Color(255, 20, 147));
        StyledButton cancelBtn = new StyledButton("İptal", Color.LIGHT_GRAY, Color.GRAY);

        addBtn.addActionListener(e -> {
            try {
                tasks.add(new Task(titleField.getText(), descArea.getText(), dateField.getText(), Integer.parseInt(pointsField.getText()), "Pending", userRole));
                dialog.dispose();
                mainContentPanel.add(createTasksView(), "TASKS"); // Refresh view
                cardLayout.show(mainContentPanel, "TASKS");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Lütfen puan için geçerli bir sayı girin.");
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addBtn);
        buttonPanel.add(cancelBtn);

        panel.add(formPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setContentPane(panel);
        dialog.setVisible(true);
    }
    
    private JPanel createFormField(String labelText, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(Color.GRAY);
        panel.add(label, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10), new EmptyBorder(5, 10, 5, 10)));
        return field;
    }
    
    private JTextArea createStyledTextArea(String placeholder) {
        JTextArea area = new JTextArea();
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setRows(3);
        area.setLineWrap(true);
        area.setBorder(new EmptyBorder(5, 10, 5, 10));
        return area;
    }
    
    // Simple Rounded Border
    private static class RoundedBorder implements javax.swing.border.Border {
        private int radius;
        RoundedBorder(int radius) { this.radius = radius; }
        public Insets getBorderInsets(Component c) { return new Insets(this.radius+1, this.radius+1, this.radius+2, this.radius); }
        public boolean isBorderOpaque() { return true; }
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            g.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }


    private JPanel createWishesView() {
        JPanel wishesPanel = new JPanel(new BorderLayout(0, 20));
        wishesPanel.setOpaque(false);
        
        // Header
        JLabel title = new JLabel("🎁 Dileklerim");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(100, 100, 100));
        wishesPanel.add(title, BorderLayout.NORTH);

        // Content or Empty State
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        if (wishes.isEmpty()) {
            JLabel emptyLabel = new JLabel("Henüz dilek yok. Hayal et ve ekle! 🌈", SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            emptyLabel.setForeground(Color.GRAY);
            content.add(emptyLabel, BorderLayout.CENTER);
        } else {
            String[] columns = {"Dilek", "Gerekli Puan", "Durum"};
            DefaultTableModel model = new DefaultTableModel(columns, 0);
            for (Wish w : wishes) {
                 if (w.pointsRequired <= (currentLevel * 100) + 100) {
                    model.addRow(new Object[]{w.name, w.pointsRequired, w.isApproved ? "Onaylandı" : "Bekliyor"});
                }
            }
            JTable table = new JTable(model);
            table.setRowHeight(30);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            content.add(new JScrollPane(table), BorderLayout.CENTER);
        }
        
        // Add Wish Button for Child
        if (userRole.equals("Öğrenci")) {
             StyledButton addWishBtn = new StyledButton("+ Yeni Dilek Ekle", new Color(255, 105, 180), new Color(255, 20, 147));
             addWishBtn.addActionListener(e -> handleAddWish());
             JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
             btnPanel.setOpaque(false);
             btnPanel.setBorder(new EmptyBorder(10,0,0,0));
             btnPanel.add(addWishBtn);
             content.add(btnPanel, BorderLayout.SOUTH);
        }

        wishesPanel.add(content, BorderLayout.CENTER);
        return wishesPanel;
    }

    private JPanel createProgressView() {
        JPanel progressPanel = new JPanel(new BorderLayout(0, 20));
        progressPanel.setOpaque(false);
        
        // Header
        JLabel title = new JLabel("⭐ İlerleme Durumum");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(100, 100, 100));
        progressPanel.add(title, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new GridLayout(3, 1, 0, 20));
        content.setOpaque(false);

        // Level Progress Section
        JPanel levelPanel = new JPanel(new BorderLayout(0, 10));
        levelPanel.setOpaque(false);
        JLabel levelTitle = new JLabel("Seviye İlerleme");
        levelTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        levelTitle.setForeground(Color.GRAY);
        
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(totalPoints % 100);
        bar.setStringPainted(true);
        bar.setForeground(new Color(255, 105, 180));
        bar.setBackground(new Color(230, 230, 250));
        bar.setPreferredSize(new Dimension(100, 30));
        
        JPanel labels = new JPanel(new BorderLayout());
        labels.setOpaque(false);
        labels.add(new JLabel("Seviye " + currentLevel), BorderLayout.WEST);
        labels.add(new JLabel(totalPoints + " Puan"), BorderLayout.EAST);
        
        levelPanel.add(levelTitle, BorderLayout.NORTH);
        levelPanel.add(bar, BorderLayout.CENTER);
        levelPanel.add(labels, BorderLayout.SOUTH);
        content.add(levelPanel);

        // Achievements Section
        JPanel achievementsPanel = new JPanel(new BorderLayout(0, 10));
        achievementsPanel.setOpaque(false);
        JLabel achTitle = new JLabel("Başarılarım 🏆");
        achTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        achTitle.setForeground(Color.GRAY);
        
        JPanel achCards = new JPanel(new GridLayout(1, 4, 10, 0));
        achCards.setOpaque(false);
        achCards.add(createAchievementCard("İlk Görev", "🌟", new Color(255, 250, 205)));
        achCards.add(createAchievementCard("10 Görev", "🏆", new Color(240, 255, 240)));
        achCards.add(createAchievementCard("Seviye 5", "💎", new Color(255, 240, 245)));
        achCards.add(createAchievementCard("Süper Yıldız", "👑", new Color(230, 230, 250)));
        
        achievementsPanel.add(achTitle, BorderLayout.NORTH);
        achievementsPanel.add(achCards, BorderLayout.CENTER);
        content.add(achievementsPanel);

        // Fun Stats Section
        JPanel statsPanel = new JPanel(new BorderLayout(0, 10));
        statsPanel.setOpaque(false);
        JLabel statsTitle = new JLabel("Eğlenceli İstatistikler 📊");
        statsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        statsTitle.setForeground(Color.GRAY);
        
        JPanel statCards = new JPanel(new GridLayout(1, 3, 10, 0));
        statCards.setOpaque(false);
        statCards.add(createStatCard("Tamamlanan Görev", "✅", completedTasks + "", new Color(173, 216, 230)));
        statCards.add(createStatCard("Onaylanan Dilek", "🎁", approvedWishes + "", new Color(152, 251, 152)));
        statCards.add(createStatCard("Günlük Seri", "🔥", dailyStreak + "", new Color(255, 218, 185)));

        statsPanel.add(statsTitle, BorderLayout.NORTH);
        statsPanel.add(statCards, BorderLayout.CENTER);
        content.add(statsPanel);

        progressPanel.add(content, BorderLayout.CENTER);
        return progressPanel;
    }
    
    private JPanel createAchievementCard(String title, String icon, Color bgColor) {
        RoundedPanel card = new RoundedPanel(bgColor, 15);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 10, 15, 10));
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        card.add(iconLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createStatCard(String title, String icon, String value, Color bgColor) {
        RoundedPanel card = new RoundedPanel(bgColor, 15);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 24));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(Color.DARK_GRAY);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(iconLabel, BorderLayout.WEST);
        
        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }


    // --- LOGIC METHODS ---

    private void handleAddWish() {
        try {
            String n = JOptionPane.showInputDialog(this, "Dileğinin adı ne?", "Yeni Dilek", JOptionPane.QUESTION_MESSAGE);
            String p = JOptionPane.showInputDialog(this, "Kaç puan değerinde?", "Yeni Dilek", JOptionPane.QUESTION_MESSAGE);
            if (n != null && p != null) {
                wishes.add(new Wish(n, Integer.parseInt(p), false));
                mainContentPanel.add(createWishesView(), "WISHES"); // Refresh view
                cardLayout.show(mainContentPanel, "WISHES");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir puan girin.");
        }
    }

    private void handleTaskAction(int row) {
        Task t = tasks.get(row);
        if (userRole.equals("Öğrenci")) {
            if (t.status.equals("Pending")) {
                t.status = "Completed";
                JOptionPane.showMessageDialog(this, "Harika! Görev tamamlandı, onay bekliyor.");
            }
        } else {
            if (t.status.equals("Completed")) {
                t.status = "Approved";
                totalPoints += t.points;
                currentLevel = (totalPoints / 100) + 1;
                completedTasks++; // Increment stat
                JOptionPane.showMessageDialog(this, "Görev onaylandı! " + t.points + " puan eklendi.");
            }
        }
        // Refresh views to show updated data
        mainContentPanel.add(createTasksView(), "TASKS");
        cardLayout.show(mainContentPanel, "TASKS");
    }

    private void saveData() {
        try {
            PrintWriter tOut = new PrintWriter(new FileWriter("data/tasks.txt"));
            for (Task t : tasks) tOut.println(t.toFileString());
            tOut.close();
            
            PrintWriter wOut = new PrintWriter(new FileWriter("data/wishes.txt"));
            // Save new stats: points|level|completedTasks|approvedWishes|dailyStreak|lastLoginDate
            wOut.println("DATA|" + totalPoints + "|" + currentLevel + "|" + completedTasks + "|" + approvedWishes + "|" + dailyStreak + "|" + lastLoginDate);
            for (Wish w : wishes) wOut.println(w.toFileString());
            wOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        try (BufferedReader br = new BufferedReader(new FileReader("data/tasks.txt"))) {
            String l; while ((l = br.readLine()) != null) {
                String[] p = l.split("\\|");
                tasks.add(new Task(p[0], p[1], p[2], Integer.parseInt(p[3]), p[4], p[5]));
            }
        } catch (Exception e) {}

        try (BufferedReader br = new BufferedReader(new FileReader("data/wishes.txt"))) {
            String l; while ((l = br.readLine()) != null) {
                String[] p = l.split("\\|");
                if (p[0].equals("DATA")) {
                    totalPoints = Integer.parseInt(p[1]);
                    currentLevel = Integer.parseInt(p[2]);
                    // Load new stats if available
                    if (p.length > 3) completedTasks = Integer.parseInt(p[3]);
                    if (p.length > 4) approvedWishes = Integer.parseInt(p[4]);
                    if (p.length > 5) dailyStreak = Integer.parseInt(p[5]);
                    if (p.length > 6) lastLoginDate = p[6];
                } else {
                    wishes.add(new Wish(p[0], Integer.parseInt(p[1]), Boolean.parseBoolean(p[2])));
                }
            }
        } catch (Exception e) {}
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(KidTaskApp::new);
    }
}