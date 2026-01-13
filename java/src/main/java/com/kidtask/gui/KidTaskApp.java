package com.kidtask.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.kidtask.models.Task;
import com.kidtask.models.User;
import com.kidtask.models.Wish;
import com.kidtask.services.FileHandler;

public class KidTaskApp extends JFrame {
    private final Color COLOR_BG = new Color(245, 240, 255);
    private final Color PURPLE = new Color(156, 39, 176);
    private final Color ORANGE = new Color(255, 183, 77);
    private final Color GREEN = new Color(77, 208, 225);

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);
    private User currentUser;
    private List<Task> tasks;
    private List<Wish> wishes;
    private List<User> allUsers;

    public KidTaskApp() {
        tasks = FileHandler.loadTasks();
        wishes = FileHandler.loadWishes();
        allUsers = FileHandler.loadUsers();

        if (allUsers.isEmpty()) {
            allUsers.add(new User("Çocuk", User.CHILD));
            allUsers.add(new User("Ebeveyn", User.PARENT));
            allUsers.add(new User("Öğretmen", User.TEACHER));
            FileHandler.saveUsers(allUsers);
        }

        setTitle("KidTask");
        setSize(1200, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainContainer.add(createLoginPanel(), "LOGIN");
        add(mainContainer);
    }

    private JPanel createLoginPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(PURPLE);
        RoundedPanel card = new RoundedPanel(40, Color.WHITE);
        card.setPreferredSize(new Dimension(350, 500));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(50, 40, 50, 40));

        JLabel title = new JLabel("KidTask 🐵");
        title.setFont(new Font("SansSerif", Font.BOLD, 32));
        title.setAlignmentX(0.5f);
        card.add(title); card.add(Box.createVerticalStrut(50));

        for (User u : allUsers) {
            JButton b = new JButton(u.getName() + " Girişi");
            b.setMaximumSize(new Dimension(280, 45));
            b.setAlignmentX(0.5f);
            b.addActionListener(e -> { currentUser = u; setupDashboard(); });
            card.add(b); card.add(Box.createVerticalStrut(10));
        }
        p.add(card);
        return p;
    }

    private void setupDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(COLOR_BG);

        // --- Üst Menü Barı ---
        JPanel nav = new JPanel(new GridLayout(1, 4));
        nav.setBackground(Color.WHITE);
        nav.setPreferredSize(new Dimension(1200, 80));
        String[] menu = {"Ana Sayfa", "Görevler", "Dilekler", "İlerleme"};
        for (String m : menu) {
            JButton btn = new JButton(m);
            btn.setBorderPainted(false); btn.setContentAreaFilled(false);
            btn.setFont(new Font("SansSerif", Font.BOLD, 14));
            btn.addActionListener(e -> cardLayout.show(mainContainer, m));
            nav.add(btn);
        }

        // --- Sayfalar ---
        mainContainer.add(createHomePanel(), "Ana Sayfa");
        mainContainer.add(createTaskPanel(), "Görevler");
        mainContainer.add(createWishPanel(), "Dilekler");
        mainContainer.add(createProgressPanel(), "İlerleme");

        dashboard.add(nav, BorderLayout.NORTH);
        dashboard.add(new JPanel(), BorderLayout.CENTER);
        
        mainContainer.add(dashboard, "MAIN_UI");
        cardLayout.show(mainContainer, "MAIN_UI");
        cardLayout.show(mainContainer, "Ana Sayfa");
    }

    private JPanel createHomePanel() {
        JPanel p = createBasePanel();
        JPanel stats = new JPanel(new GridLayout(1, 3, 20, 0));
        stats.setOpaque(false);
        stats.add(new StatCard("Motivasyon", "0", ORANGE));
        stats.add(new StatCard("Seviyem", String.valueOf(currentUser.getLevel()), PURPLE));
        stats.add(new StatCard("Bekleyen", String.valueOf(tasks.size()), GREEN));
        p.add(stats); p.add(Box.createVerticalStrut(30));
        
        RoundedPanel animBar = new RoundedPanel(30, Color.WHITE);
        animBar.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 20));
        animBar.add(new JLabel("🐵 Milo")); animBar.add(new JLabel("🍕 Pizza")); animBar.add(new JLabel("🦁 Leo"));
        p.add(animBar);
        return p;
    }

    private JPanel createTaskPanel() {
        JPanel p = createBasePanel();
        RoundedPanel card = new RoundedPanel(30, Color.WHITE);
        card.setLayout(new BorderLayout()); card.setBorder(new EmptyBorder(20,20,20,20));
        DefaultListModel<Task> model = new DefaultListModel<>();
        tasks.forEach(model::addElement);
        JList<Task> jlist = new JList<>(model);
        card.add(new JLabel("<html><h2>Görevlerim 📝</h2></html>"), BorderLayout.NORTH);
        card.add(new JScrollPane(jlist), BorderLayout.CENTER);

        JButton action = new JButton(currentUser.isChild() ? "Tamamla" : "Onayla");
        action.addActionListener(e -> {
            Task s = jlist.getSelectedValue();
            if (s != null) {
                if (currentUser.isChild()) s.setCompleted(true, currentUser.getName());
                else { 
                    s.setApproved(true); 
                    // Puanı gerçek çocuğun nesnesine ekle
                    allUsers.stream().filter(User::isChild).findFirst().ifPresent(u -> u.addPoints(s.getPoints()));
                }
                saveAll(); setupDashboard();
            }
        });
        card.add(action, BorderLayout.SOUTH);
        p.add(card);
        return p;
    }

    private JPanel createWishPanel() {
        JPanel p = createBasePanel();
        RoundedPanel card = new RoundedPanel(30, Color.WHITE);
        card.setLayout(new BorderLayout()); card.setBorder(new EmptyBorder(20,20,20,20));
        DefaultListModel<Wish> model = new DefaultListModel<>();
        wishes.stream().filter(w -> !currentUser.isChild() || w.getRequiredLevel() <= currentUser.getLevel()).forEach(model::addElement);
        card.add(new JLabel("<html><h2>Dileklerim 🎁</h2></html>"), BorderLayout.NORTH);
        card.add(new JScrollPane(new JList<>(model)), BorderLayout.CENTER);
        
        if(currentUser.isChild()) {
            JButton add = new JButton("+ Yeni Dilek Ekle");
            add.addActionListener(e -> {
                String d = JOptionPane.showInputDialog("Dileğin nedir?");
                if(d != null) { wishes.add(new Wish(d, 1)); saveAll(); setupDashboard(); }
            });
            card.add(add, BorderLayout.SOUTH);
        }
        p.add(card);
        return p;
    }

    private JPanel createProgressPanel() {
        JPanel p = createBasePanel();
        RoundedPanel card = new RoundedPanel(30, Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(30,30,30,30));
        card.add(new JLabel("<html><h2>⭐ İlerleme Durumum</h2></html>"));
        JProgressBar bar = new JProgressBar(0, 100); bar.setValue(currentUser.getPoints() % 100);
        card.add(bar); card.add(Box.createVerticalStrut(20));
        card.add(new JLabel("Başarılarım 🏆"));
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10)); badges.setOpaque(false);
        badges.add(new BadgeCard("İlk Görev", Color.YELLOW)); badges.add(new BadgeCard("Seviye 5", Color.CYAN));
        card.add(badges);
        p.add(card);
        return p;
    }

    private JPanel createBasePanel() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(COLOR_BG); p.setBorder(new EmptyBorder(30, 60, 30, 60));
        return p;
    }

    private void saveAll() {
        FileHandler.saveTasks(tasks); FileHandler.saveWishes(wishes); FileHandler.saveUsers(allUsers);
    }

    class RoundedPanel extends JPanel {
        private int r;
        public RoundedPanel(int r, Color c) { this.r = r; setOpaque(false); setBackground(c); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), r, r));
        }
    }

    class StatCard extends RoundedPanel {
        public StatCard(String t, String v, Color c) {
            super(30, c); setLayout(new BorderLayout()); setBorder(new EmptyBorder(15,20,15,20));
            JLabel title = new JLabel(t); title.setForeground(Color.WHITE);
            JLabel val = new JLabel(v); val.setFont(new Font("Sans", Font.BOLD, 30)); val.setForeground(Color.WHITE);
            add(title, BorderLayout.NORTH); add(val, BorderLayout.CENTER);
        }
    }

    class BadgeCard extends RoundedPanel {
        public BadgeCard(String text, Color color) { super(20, color.brighter()); add(new JLabel(text)); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KidTaskApp().setVisible(true));
    }
}