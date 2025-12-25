package com.campuscare.controller;

import com.campuscare.model.User;
import com.campuscare.util.DataService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private TextField passwordVisible;
    @FXML private PasswordField passwordField;
    @FXML private Button togglePasswordBtn;
    @FXML private Label errorLabel;
    
    private DataService dataService = new DataService();
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 5;
    
    @FXML
    private void handleLogin() {
        clearError();
        
        String username = usernameField.getText().trim();
        String password = passwordField.isVisible() ? passwordField.getText() : passwordVisible.getText();
        
        // Validation
        if (username.isEmpty()) {
            showError("ID is required");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Password is required");
            return;
        }
        
        if (password.length() < 6) {
            showError("Password must be at least 6 characters");
            return;
        }
        
        // Check login attempts
        if (loginAttempts >= MAX_ATTEMPTS) {
            showError("Account locked. Too many failed attempts. Contact admin.");
            return;
        }
        
        // Authenticate
        User user = dataService.authenticate(username, password);
        if (user != null) {
            loginAttempts = 0;
            openDashboard(user);
        } else {
            loginAttempts++;
            int remaining = MAX_ATTEMPTS - loginAttempts;
            if (remaining > 0) {
                showError("Invalid ID or password. " + remaining + " attempts remaining.");
            } else {
                showError("Account locked. Too many failed attempts.");
            }
        }
    }
    
    @FXML
    private void togglePassword() {
        if (passwordField.isVisible()) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("🔒");
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            togglePasswordBtn.setText("👁");
        }
    }
    
    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forgot Password");
        alert.setHeaderText("Password Recovery");
        alert.setContentText("Please contact system administrator to reset your password.\n\nEmail: admin@campus.edu\nPhone: +251-11-XXX-XXXX");
        alert.showAndWait();
    }
    
    private void openDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Dashboard.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
            
            DashboardController controller = loader.getController();
            controller.setDataService(dataService);
            controller.setUser(user);
            
            stage.setTitle("Campath - Dashboard");
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
