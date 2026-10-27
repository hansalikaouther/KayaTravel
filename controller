package controller;

import model.*;
import view.*;
import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {
    private List<User> users;
    private List<Event> events;
    private List<Notification> notifications;
    private List<Comment> comments;
    private User currentUser;
    private MainFrame mainFrame;
    private JFrame loginFrame;

    public MainController() {
        users = DataManager.loadUsers();
        events = DataManager.loadEvents();
        notifications = DataManager.loadNotifications();
        comments = DataManager.loadComments();
        showLoginDialog();
    }

    private void showLoginDialog() {
        LoginPanel loginPanel = new LoginPanel(this);
        loginFrame = new JFrame("Kaya Travel - Connexion");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 350);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.add(loginPanel);
        loginFrame.setVisible(true);
    }

    public void login(String email, String password, JFrame loginFrame) {
        // Validation des champs vides
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            ModernDialog.showWarning(loginFrame, "Veuillez remplir tous les champs", "Champs requis");
            return;
        }
        
        String emailLower = email.trim().toLowerCase();
        for (User user : users) {
            if (user.getEmail().toLowerCase().equals(emailLower) && user.getPassword().equals(password)) {
                currentUser = user;
                if (loginFrame != null) {
                    loginFrame.dispose();
                }
                openMainFrame();
                return;
            }
        }
        ModernDialog.showError(loginFrame, "Email ou mot de passe incorrect!", "Erreur de connexion");
    }

    public boolean signup(String name, String email, String password, String confirmPassword, String role) {
        return signup(name, email, password, confirmPassword, role, null);
    }

    public boolean signup(String name, String email, String password, String confirmPassword, String role, Component parentComponent) {
        // Validate email format
        String emailLower = email.toLowerCase();
        if (!emailLower.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            ModernDialog.showError(parentComponent, "Format d'email invalide!<br>(exemple: user@gmail.com)", "Erreur");
            return false;
        }
        
        // Verifier d'abord si l'email existe deja
        for (User user : users) {
            if (user.getEmail().toLowerCase().equals(emailLower)) {
                ModernDialog.showError(parentComponent, "Cet email est deja utilise!", "Erreur");
                return false;
            }
        }
        
        // Validate password strength
        if (password.length() < 8 || 
            !password.matches(".*[A-Z].*") || 
            !password.matches(".*[a-z].*") || 
            !password.matches(".*\\d.*")) {
            ModernDialog.showError(parentComponent, "Le mot de passe doit contenir:<br>" +
                    "- Au moins 8 caracteres<br>" +
                    "- Au moins 1 lettre MAJUSCULE<br>" +
                    "- Au moins 1 lettre minuscule<br>" +
                    "- Au moins 1 chiffre (0-9)", "Mot de passe invalide");
            return false;
        }
        
        if (!password.equals(confirmPassword)) {
            ModernDialog.showError(parentComponent, "Les mots de passe ne correspondent pas!", "Erreur");
            return false;
        }
        
        User newUser = new User(name, emailLower, role, password);
        users.add(newUser);
        DataManager.saveUsers(users);
        
        // Connexion automatique apres inscription
        currentUser = newUser;
        ModernDialog.showSuccess(parentComponent, "Compte cree avec succes!<br>Bienvenue " + name + "!", "Inscription reussie");
        
        // Ouvrir l'interface principale
        openMainFrame();
        return true;
    }

    private void openMainFrame() {
        mainFrame = new MainFrame(this);
        mainFrame.setVisible(true);
        
        // Verifier les notifications non lues
        int unreadCount = getUnreadNotificationCount();
        if (unreadCount > 0) {
            String message = unreadCount == 1 
                ? "Vous avez 1 nouvelle notification"
                : "Vous avez " + unreadCount + " nouvelles notifications";
            NotificationPopup.show(message);
        }
    }

    public void addEvent(Event event) {
        events.add(event);
        DataManager.saveEvents(events);
        String notifMessage = "Nouvel evenement ajoute: " + event.getTitle() + " par " + event.getCreatedBy();
        addNotification(notifMessage, null);
    }

    public void updateEvent(Event event) {
        DataManager.saveEvents(events);
        String notifMessage = "Evenement modifie: " + event.getTitle() + " → " + event.getDepartDate();
        addNotification(notifMessage, null);
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() -> {
                mainFrame.refresh();
                mainFrame.repaint();
                mainFrame.revalidate();
            });
        }
    }

    public void deleteEvent(Event event) {
        String eventTitle = event.getTitle();
        events.remove(event);
        DataManager.saveEvents(events);
        String notifMessage = "Evenement supprime: " + eventTitle;
        addNotification(notifMessage, null);
    }

    public boolean canEditEvent(Event event) {
        if (currentUser == null) return false;
        if ("admin".equalsIgnoreCase(currentUser.getRole())) return true;
        return event.getCreatedBy().equals(currentUser.getName());
    }

    public boolean canDeleteEvent(Event event) {
        if (currentUser == null) return false;
        if ("admin".equalsIgnoreCase(currentUser.getRole())) return true;
        return event.getCreatedBy().equals(currentUser.getName());
    }

    public boolean isAdmin() {
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    public boolean verifyCurrentPassword(String password) {
        if (currentUser == null) return false;
        return currentUser.getPassword().equals(password);
    }

    public List<Event> getEvents() { return events; }
    public User getCurrentUser() { return currentUser; }
    public List<Notification> getNotifications() { return notifications; }

    public List<Notification> getNotificationsForCurrentUser() {
        if (currentUser == null) return new ArrayList<>();
        String email = currentUser.getEmail();
        return notifications.stream()
                .filter(n -> n.getRecipientEmail() == null || email.equalsIgnoreCase(n.getRecipientEmail()))
                .collect(Collectors.toList());
    }

    public int getUnreadNotificationCount() {
        if (currentUser == null) return 0;
        String email = currentUser.getEmail();
        return (int) notifications.stream()
                .filter(n -> n.getRecipientEmail() == null || email.equalsIgnoreCase(n.getRecipientEmail()))
                .filter(n -> !n.isSeen())
                .count();
    }

    public void markNotificationsAsRead() {
        if (currentUser == null) return;
        String email = currentUser.getEmail();
        boolean changed = false;
        for (Notification n : notifications) {
            if ((n.getRecipientEmail() == null || email.equalsIgnoreCase(n.getRecipientEmail())) && !n.isSeen()) {
                n.setSeen(true);
                changed = true;
            }
        }
        if (changed) {
            DataManager.saveNotifications(notifications);
            if (mainFrame != null) {
                mainFrame.refresh();
            }
        }
    }

    public void addNotification(String message, String recipientEmail) {
        notifications.add(new Notification(message, recipientEmail));
        DataManager.saveNotifications(notifications);
        if (mainFrame != null) {
            mainFrame.refresh();
        }
    }

    public void clearNotificationsForCurrentUser() {
        if (currentUser == null) return;
        String email = currentUser.getEmail();
        notifications.removeIf(n -> n.getRecipientEmail() == null || email.equalsIgnoreCase(n.getRecipientEmail()));
        DataManager.saveNotifications(notifications);
        if (mainFrame != null) {
            mainFrame.refresh();
        }
    }

    public void logout() {
        if (mainFrame != null) {
            mainFrame.dispose();
            mainFrame = null;
        }
        currentUser = null;
        showLoginDialog();
    }

    public List<User> getUsers() {
        return users;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void addComment(String message) {
        if (currentUser == null) return;
        Comment comment = new Comment(currentUser.getEmail(), currentUser.getName(), message);
        comments.add(comment);
        DataManager.saveComments(comments);
        addNotification("Nouveau commentaire de " + currentUser.getName(), null);
    }

    public boolean canDeleteComment(Comment comment) {
        if (currentUser == null || comment == null) return false;
        if (isAdmin()) return true;
        return currentUser.getEmail().equalsIgnoreCase(comment.getAuthorEmail());
    }

    public void deleteComment(Comment comment) {
        if (comment == null || !canDeleteComment(comment)) return;
        comments.remove(comment);
        DataManager.saveComments(comments);
        addNotification("Commentaire supprime par " + currentUser.getName(), null);
        if (mainFrame != null) {
            mainFrame.refresh();
        }
    }

    public boolean updateProfile(String name, String email, String password) {
        return updateProfile(name, email, password, null);
    }

    public boolean updateProfile(String name, String email, String password, Component parentComponent) {
        if (currentUser == null) return false;
        String emailLower = email.toLowerCase();
        if (!emailLower.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            ModernDialog.showError(parentComponent, "Format d'email invalide", "Erreur");
            return false;
        }
        if (password != null && !password.isEmpty() && !password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,}$")) {
            ModernDialog.showError(parentComponent, "Mot de passe trop faible", "Erreur");
            return false;
        }
        for (User user : users) {
            if (!user.equals(currentUser) && user.getEmail().equalsIgnoreCase(emailLower)) {
                ModernDialog.showError(parentComponent, "Email deja utilise", "Erreur");
                return false;
            }
        }
        currentUser.setName(name);
        currentUser.setEmail(emailLower);
        if (password != null && !password.isEmpty()) {
            currentUser.setPassword(password);
        }
        DataManager.saveUsers(users);
        addNotification("Profil mis a jour pour " + currentUser.getName(), currentUser.getEmail());
        if (mainFrame != null) {
            mainFrame.refresh();
        }
        return true;
    }

    public void deleteAccount() {
        deleteAccount(null);
    }

    public void deleteAccount(Component parentComponent) {
        if (currentUser == null) return;
        
        String userEmail = currentUser.getEmail();
        
        // Supprimer l'utilisateur de la liste
        users.remove(currentUser);
        DataManager.saveUsers(users);
        
        // Supprimer les evenements crees par cet utilisateur
        events.removeIf(e -> e.getCreatedBy().equals(userEmail));
        DataManager.saveEvents(events);
        
        // Supprimer les notifications de cet utilisateur
        notifications.removeIf(n -> userEmail.equals(n.getRecipientEmail()));
        DataManager.saveNotifications(notifications);
        
        ModernDialog.showSuccess(parentComponent, 
            "Votre compte a ete supprime avec succes", 
            "Compte supprime");
        
        // Deconnecter l'utilisateur
        logout();
    }
}
