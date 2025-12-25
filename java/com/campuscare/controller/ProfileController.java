package com.campuscare.controller;

import com.campuscare.model.User;
import com.campuscare.util.DataService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {
    @FXML private ImageView avatarImage;
    @FXML private Label idLabel;
    @FXML private Label usernameLabel;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label roleLabel;
    @FXML private Label departmentLabel;
    @FXML private Label joinDateLabel;
    @FXML private Label lastLoginLabel;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox showPasswordCheck;
    @FXML private TextField newPasswordVisible;
    @FXML private TextField confirmPasswordVisible;
    @FXML private Label messageLabel;
    @FXML private ProgressBar passwordStrength;
    @FXML private Label strengthLabel;
    
    private User currentUser;
    private DataService dataService;
    
    public void setUser(User user) {
        this.currentUser = user;
        loadProfile();
    }
    
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
    
    private void loadProfile() {
        idLabel.setText(currentUser.getUserId());
        usernameLabel.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());
        roleLabel.setText(getRoleDisplayName(currentUser.getRole().toString()));
        departmentLabel.setText(currentUser.getDepartment());
        joinDateLabel.setText("Member since 2024");
        lastLoginLabel.setText("Last login: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
        
        // Load default avatar
        loadDefaultAvatar();
        
        // Setup password visibility toggle
        setupPasswordToggle();
    }
    
    private String getRoleDisplayName(String role) {
        return switch (role) {
            case "ADMIN" -> "👑 Administrator";
            case "DEPARTMENT_STAFF" -> "🏢 Department Staff";
            case "LECTURER" -> "👨‍🏫 Lecturer";
            case "STUDENT" -> "🎓 Student";
            case "STAFF" -> "👤 Staff";
            default -> role;
        };
    }
    
    private void loadDefaultAvatar() {
        String avatarPath = "/com/campuscare/images/default-avatar.png";
        try {
            Image avatar = new Image(getClass().getResourceAsStream(avatarPath));
            avatarImage.setImage(avatar);
        } catch (Exception e) {
            // Create a simple colored circle as fallback
            avatarImage.setStyle("-fx-background-color: #4A90E2; -fx-background-radius: 50;");
        }
    }
    
    private void setupPasswordToggle() {
        newPasswordVisible.setVisible(false);
        confirmPasswordVisible.setVisible(false);
        
        showPasswordCheck.setOnAction(e -> {
            boolean show = showPasswordCheck.isSelected();
            newPasswordField.setVisible(!show);
            confirmPasswordField.setVisible(!show);
            newPasswordVisible.setVisible(show);
            confirmPasswordVisible.setVisible(show);
            
            if (show) {
                newPasswordVisible.setText(newPasswordField.getText());
                confirmPasswordVisible.setText(confirmPasswordField.getText());
            } else {
                newPasswordField.setText(newPasswordVisible.getText());
                confirmPasswordField.setText(confirmPasswordVisible.getText());
            }
        });
        
        // Password strength indicator
        newPasswordField.textProperty().addListener((obs, old, newVal) -> updatePasswordStrength(newVal));
        newPasswordVisible.textProperty().addListener((obs, old, newVal) -> updatePasswordStrength(newVal));
    }
    
    private void updatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            passwordStrength.setProgress(0);
            strengthLabel.setText("");
            return;
        }
        
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()].*")) score++;
        
        double strength = score / 5.0;
        passwordStrength.setProgress(strength);
        
        String[] labels = {"Very Weak", "Weak", "Fair", "Good", "Strong"};
        String[] colors = {"#ff4444", "#ff8800", "#ffbb33", "#00C851", "#007E33"};
        
        int index = Math.min(score - 1, 4);
        if (index >= 0) {
            strengthLabel.setText(labels[index]);
            strengthLabel.setStyle("-fx-text-fill: " + colors[index] + ";");
            passwordStrength.setStyle("-fx-accent: " + colors[index] + ";");
        }
    }
    
    @FXML
    private void handleUpdateProfile() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        
        if (email.isEmpty()) {
            showMessage("Email cannot be empty", "error");
            return;
        }
        
        if (!isValidEmail(email)) {
            showMessage("Please enter a valid email address", "error");
            return;
        }
        
        if (!phone.isEmpty() && !isValidPhone(phone)) {
            showMessage("Please enter a valid phone number", "error");
            return;
        }
        
        // Update user data
        currentUser.setEmail(email);
        
        showMessage("✅ Profile updated successfully!", "success");
    }
    
    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    private boolean isValidPhone(String phone) {
        return phone.matches("^[+]?[0-9\\s\\-\\(\\)]{10,15}$");
    }
    
    @FXML
    private void handleChangePassword() {
        String current = currentPasswordField.getText();
        String newPass = showPasswordCheck.isSelected() ? newPasswordVisible.getText() : newPasswordField.getText();
        String confirm = showPasswordCheck.isSelected() ? confirmPasswordVisible.getText() : confirmPasswordField.getText();
        
        if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            showMessage("All password fields are required", "error");
            return;
        }
        
        if (!current.equals(currentUser.getPassword())) {
            showMessage("Current password is incorrect", "error");
            return;
        }
        
        if (newPass.length() < 8) {
            showMessage("New password must be at least 8 characters", "error");
            return;
        }
        
        if (!newPass.equals(confirm)) {
            showMessage("New passwords do not match", "error");
            return;
        }
        
        // Check password strength
        if (passwordStrength.getProgress() < 0.6) {
            showMessage("Password is too weak. Please use a stronger password.", "warning");
            return;
        }
        
        currentUser.setPassword(newPass);
        clearPasswordFields();
        
        showMessage("✅ Password changed successfully!", "success");
    }
    
    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        newPasswordVisible.clear();
        confirmPasswordVisible.clear();
        passwordStrength.setProgress(0);
        strengthLabel.setText("");
    }
    
    private void showMessage(String text, String type) {
        messageLabel.setText(text);
        String color = switch (type) {
            case "success" -> "#28a745";
            case "warning" -> "#ffc107";
            case "error" -> "#dc3545";
            default -> "#6c757d";
        };
        messageLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }
    
    @FXML
    private void handleChangeAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File file = fileChooser.showOpenDialog(avatarImage.getScene().getWindow());
        if (file != null) {
            try {
                Image newAvatar = new Image(file.toURI().toString());
                avatarImage.setImage(newAvatar);
                showMessage("✅ Profile picture updated!", "success");
            } catch (Exception e) {
                showMessage("Failed to load image", "error");
            }
        }
    }
    

    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Dashboard.fxml"));
            Stage stage = (Stage) idLabel.getScene().getWindow();
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
            
            DashboardController controller = loader.getController();
            controller.setDataService(dataService);
            controller.setUser(currentUser);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
