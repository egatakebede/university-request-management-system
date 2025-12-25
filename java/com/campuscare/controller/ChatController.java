package com.campuscare.controller;

import com.campuscare.model.ServiceRequest;
import com.campuscare.model.User;
import com.campuscare.util.DataService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.media.AudioClip;

public class ChatController {
    @FXML private Label headerLabel;
    @FXML private ListView<String> chatListView;
    @FXML private TextField messageField;
    @FXML private Label typingLabel;
    
    private ServiceRequest request;
    private User currentUser;
    private DataService dataService;
    
    public void setup(ServiceRequest request, User user, DataService service) {
        this.request = request;
        this.currentUser = user;
        this.dataService = service;
        headerLabel.setText("Chat - " + request.getRequestId());
        
        setupListView();
        markMessagesAsRead();
        loadMessages();
    }
    
    private void setupListView() {
        chatListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    // Format stored is: "MMM dd, yyyy HH:mm: username: message"
                    // We check if the message contains the current username to determine alignment
                    
                    String content = item;
                    boolean isRead = false;
                    if (content.endsWith(" ||READ||")) {
                        isRead = true;
                        content = content.substring(0, content.length() - 9);
                    }

                    // Split by ": " -> [0]=Timestamp, [1]=Username, [2]=Message
                    String[] parts = content.split(": ", 3);
                    boolean isMe = false;
                    String timestamp = "";
                    String displayContent = content;
                    
                    if (parts.length >= 3) {
                        timestamp = parts[0];
                        String sender = parts[1];
                        String message = parts[2];
                        isMe = sender.equals(currentUser.getUsername());
                        
                        if (isMe) {
                            displayContent = message;
                        } else {
                            displayContent = sender + ":\n" + message;
                        }
                    }
                    Label label = new Label(displayContent);
                    label.setWrapText(true);
                    label.setMaxWidth(280);
                    label.getStyleClass().add(isMe ? "chat-bubble-self" : "chat-bubble-other");
                    
                    Label timeLabel = new Label(timestamp);
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888; -fx-padding: 2 5 0 5;");
                    timeLabel.setVisible(false);
                    timeLabel.setManaged(false);
                    
                    // Ticks for sent messages
                    Label tickLabel = new Label();
                    if (isMe) {
                        tickLabel.setText(isRead ? "✓✓" : "✓");
                        tickLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (isRead ? "#0099FF" : "#888888") + "; -fx-padding: 0 0 0 5;");
                    }
                    
                    label.setOnMouseClicked(e -> {
                        if (e.getClickCount() == 2) {
                            boolean visible = !timeLabel.isVisible();
                            timeLabel.setVisible(visible);
                            timeLabel.setManaged(visible);
                        }
                    });
                    
                    HBox metaBox = new HBox(timeLabel, tickLabel);
                    metaBox.setAlignment(Pos.CENTER_RIGHT);
                    
                    VBox bubbleBox = new VBox(label, metaBox);
                    bubbleBox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    
                    HBox hbox = new HBox(bubbleBox);
                    hbox.setAlignment(isMe ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
                    hbox.setPadding(new javafx.geometry.Insets(5));
                    
                    setGraphic(hbox);
                    setText(null);
                    setStyle("-fx-background-color: transparent;");
                }
            }
        });
    }
    
    private void loadMessages() {
        chatListView.getItems().setAll(request.getComments());
        Platform.runLater(() -> chatListView.scrollTo(chatListView.getItems().size() - 1));
    }
    
    private void markMessagesAsRead() {
        boolean modified = false;
        java.util.List<String> comments = request.getComments();
        for (int i = 0; i < comments.size(); i++) {
            String comment = comments.get(i);
            String[] parts = comment.split(": ", 3);
            if (parts.length >= 3) {
                String sender = parts[1];
                if (!sender.equals(currentUser.getUsername()) && !comment.endsWith(" ||READ||")) {
                    comments.set(i, comment + " ||READ||");
                    modified = true;
                }
            }
        }
        if (modified) {
            dataService.updateRequest(request);
            playSound();
        }
    }
    
    @FXML
    private void handleSend() {
        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;
        
        request.addComment(currentUser.getUsername() + ": " + msg);
        dataService.updateRequest(request);
        
        // Send notifications
        if (currentUser.getUserId().equals(request.getRequesterId())) {
             dataService.notifyDepartment(currentUser.getUsername(), request.getDepartmentId(), "New message on " + request.getRequestId() + ": " + msg);
        } else {
             dataService.sendNotification(currentUser.getUsername(), request.getRequesterId(), "New message on " + request.getRequestId() + ": " + msg);
        }
        
        messageField.clear();
        loadMessages();
        playSound();
    }
    
    public void showTypingIndicator(String username) {
        typingLabel.setText(username + " is typing...");
        typingLabel.setVisible(true);
        typingLabel.setManaged(true);
    }
    
    public void hideTypingIndicator() {
        typingLabel.setVisible(false);
        typingLabel.setManaged(false);
    }
    
    @FXML
    private void handleClear() {
        if (request.getComments().isEmpty()) return;
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Clear Chat");
        alert.setHeaderText("Delete all messages?");
        alert.setContentText("This action cannot be undone.");
        
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            request.clearComments();
            dataService.updateRequest(request);
            loadMessages();
        }
    }
    
    private void playSound() {
        try {
            java.net.URL resource = getClass().getResource("/com/campuscare/sounds/notification.wav");
            if (resource != null) {
                new AudioClip(resource.toString()).play();
            }
        } catch (Exception e) {
            // Ignore if sound file not found or error playing
        }
    }
}