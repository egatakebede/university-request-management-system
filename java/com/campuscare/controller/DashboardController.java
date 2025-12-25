package com.campuscare.controller;

import com.campuscare.model.*;
import com.campuscare.util.DataService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.util.UUID;
import java.util.List;
import javafx.scene.control.TableCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;

public class DashboardController {
    @FXML private BorderPane dashboardRoot;
    @FXML private Label welcomeLabel;
    @FXML private TableView<ServiceRequest> requestTable;
    @FXML private TableColumn<ServiceRequest, String> idColumn;
    @FXML private TableColumn<ServiceRequest, String> titleColumn;
    @FXML private TableColumn<ServiceRequest, String> requesterColumn;
    @FXML private TableColumn<ServiceRequest, RequestCategory> categoryColumn;
    @FXML private TableColumn<ServiceRequest, RequestStatus> statusColumn;
    @FXML private TableColumn<ServiceRequest, Priority> priorityColumn;
    @FXML private TableColumn<ServiceRequest, String> createdColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<RequestStatus> statusFilter;
    @FXML private Button notificationsBtn;
    @FXML private Button usersBtn;
    @FXML private Button newRequestBtn;
    @FXML private Button analyticsBtn;
    @FXML private Button exportBtn;
    @FXML private Button editBtn;
    @FXML private Button cancelRequestBtn;
    @FXML private Button assignBtn;
    @FXML private Button acceptBtn;
    @FXML private Button rejectBtn;
    @FXML private Button updateStatusBtn;
    @FXML private Button deleteBtn;
    @FXML private HBox actionButtonsContainer;
    
    private User currentUser;
    private DataService dataService;
    
    public void setUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername() + " (" + user.getRole() + ")");
        configureRoleBasedUI();
        loadRequests();
        updateNotificationBadge();
    }
    
    private void configureRoleBasedUI() {
        UserRole role = currentUser.getRole();
        
        if (role == UserRole.ADMIN) {
            newRequestBtn.setVisible(false);
            newRequestBtn.setManaged(false);
            usersBtn.setVisible(true);
            usersBtn.setManaged(true);
            analyticsBtn.setVisible(true);
            analyticsBtn.setManaged(true);
            exportBtn.setVisible(true);
            exportBtn.setManaged(true);
            assignBtn.setVisible(false);
            assignBtn.setManaged(false);
            acceptBtn.setVisible(true);
            acceptBtn.setManaged(true);
            rejectBtn.setVisible(true);
            rejectBtn.setManaged(true);
            updateStatusBtn.setVisible(true);
            updateStatusBtn.setManaged(true);
            deleteBtn.setVisible(true);
            deleteBtn.setManaged(true);
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            cancelRequestBtn.setVisible(false);
            cancelRequestBtn.setManaged(false);
        } else if (role == UserRole.DEPARTMENT_STAFF) {
            assignBtn.setVisible(false);
            assignBtn.setManaged(false);
            acceptBtn.setVisible(true);
            acceptBtn.setManaged(true);
            rejectBtn.setVisible(true);
            rejectBtn.setManaged(true);
            updateStatusBtn.setVisible(true);
            updateStatusBtn.setManaged(true);
            exportBtn.setVisible(true);
            exportBtn.setManaged(true);
            editBtn.setVisible(false);
            editBtn.setManaged(false);
            cancelRequestBtn.setVisible(false);
            cancelRequestBtn.setManaged(false);
        } else {
            editBtn.setVisible(true);
            editBtn.setManaged(true);
            cancelRequestBtn.setVisible(true);
            cancelRequestBtn.setManaged(true);
        }
    }
    
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
    
    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("requestId"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        requesterColumn.setCellValueFactory(new PropertyValueFactory<>("requesterId"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        createdColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        
        statusColumn.setCellFactory(col -> new TableCell<ServiceRequest, RequestStatus>() {
            @Override
            protected void updateItem(RequestStatus status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status.toString());
                    switch (status) {
                        case DRAFT -> setStyle("-fx-text-fill: #6C757D; -fx-font-weight: bold;");
                        case PENDING -> setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                        case IN_PROGRESS -> setStyle("-fx-text-fill: #1E90FF; -fx-font-weight: bold;");
                        case WAITING_FOR_USER -> setStyle("-fx-text-fill: #FF6B6B; -fx-font-weight: bold;");
                        case COMPLETED -> setStyle("-fx-text-fill: #28A745; -fx-font-weight: bold;");
                        case REJECTED -> setStyle("-fx-text-fill: #DC3545; -fx-font-weight: bold;");
                        case CANCELLED -> setStyle("-fx-text-fill: #6C757D; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        priorityColumn.setCellFactory(col -> new TableCell<ServiceRequest, Priority>() {
            @Override
            protected void updateItem(Priority priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority.toString());
                    if (priority == Priority.URGENT) {
                        setStyle("-fx-text-fill: #DC3545; -fx-font-weight: bold;");
                    } else if (priority == Priority.HIGH) {
                        setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        statusFilter.setItems(FXCollections.observableArrayList(RequestStatus.values()));
        statusFilter.setOnAction(e -> filterRequests());
        
        searchField.textProperty().addListener((obs, old, newVal) -> filterRequests());
        
        // Hide action buttons initially and show only when a request is selected
        actionButtonsContainer.setVisible(false);
        actionButtonsContainer.setManaged(false);
        requestTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            actionButtonsContainer.setVisible(newVal != null);
            actionButtonsContainer.setManaged(newVal != null);
        });
        
        // Enable double-click to view details
        requestTable.setRowFactory(tv -> {
            TableRow<ServiceRequest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    showRequestDetails(row.getItem());
                }
            });
            return row;
        });
        
        // Clear selection when clicking on empty space
        requestTable.setOnMouseClicked(event -> {
            if (!(event.getTarget() instanceof Node)) return;
            Node node = (Node) event.getTarget();
            while (node != null && node != requestTable) {
                if (node instanceof TableRow && !((TableRow<?>) node).isEmpty()) {
                    return;
                }
                if (node.getStyleClass().contains("column-header-background") || node.getStyleClass().contains("column-header")) {
                    return;
                }
                node = node.getParent();
            }
            requestTable.getSelectionModel().clearSelection();
        });
        
        // Clear selection when clicking outside the table (on the background)
        dashboardRoot.setOnMouseClicked(event -> {
            Node target = (Node) event.getTarget();
            boolean insideTable = false;
            boolean insideButtons = false;
            while (target != null && target != dashboardRoot) {
                if (target == requestTable) { insideTable = true; break; }
                if (target == actionButtonsContainer) { insideButtons = true; break; }
                target = target.getParent();
            }
            if (!insideTable && !insideButtons) {
                requestTable.getSelectionModel().clearSelection();
            }
        });
    }
    
    private void loadRequests() {
        List<ServiceRequest> requests;
        if (currentUser.getRole() == UserRole.ADMIN) {
            requests = dataService.getAllRequests();
        } else if (currentUser.getRole() == UserRole.DEPARTMENT_STAFF) {
            requests = dataService.getRequestsByDepartment(currentUser.getDepartment());
        } else {
            requests = dataService.getRequestsByUser(currentUser.getUserId());
        }
        requestTable.setItems(FXCollections.observableArrayList(requests));
    }
    
    private void filterRequests() {
        List<ServiceRequest> requests;
        if (currentUser.getRole() == UserRole.ADMIN) {
            requests = dataService.getAllRequests();
        } else if (currentUser.getRole() == UserRole.DEPARTMENT_STAFF) {
            requests = dataService.getRequestsByDepartment(currentUser.getDepartment());
        } else {
            requests = dataService.getRequestsByUser(currentUser.getUserId());
        }
        
        String search = searchField.getText().toLowerCase();
        RequestStatus status = statusFilter.getValue();
        
        List<ServiceRequest> filtered = requests.stream()
            .filter(r -> search.isEmpty() || r.getTitle().toLowerCase().contains(search) || 
                        r.getRequestId().toLowerCase().contains(search))
            .filter(r -> status == null || r.getStatus() == status)
            .toList();
        
        requestTable.setItems(FXCollections.observableArrayList(filtered));
    }
    
    @FXML
    private void handleUsers() {
        List<User> users = dataService.getAllUsers();
        // Filter out current admin
        List<User> targetUsers = users.stream()
            .filter(u -> !u.getUserId().equals(currentUser.getUserId()))
            .toList();

        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("User List");
        dialog.setHeaderText("Select a user to chat with");

        TextField userSearchField = new TextField();
        userSearchField.setPromptText("Search by name, role, or department...");

        ListView<User> listView = new ListView<>();
        ObservableList<User> observableList = FXCollections.observableArrayList(targetUsers);
        FilteredList<User> filteredList = new FilteredList<>(observableList, p -> true);
        listView.setItems(filteredList);

        userSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(user -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return user.getUsername().toLowerCase().contains(lower) ||
                       user.getRole().toString().toLowerCase().contains(lower) ||
                       user.getDepartment().toLowerCase().contains(lower);
            });
        });

        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox root = new HBox(10);
                    root.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    // Mock Status Indicator
                    boolean isOnline = Math.abs(item.getUsername().hashCode()) % 3 == 0; 
                    javafx.scene.shape.Circle statusCircle = new javafx.scene.shape.Circle(5);
                    statusCircle.setFill(isOnline ? javafx.scene.paint.Color.LIMEGREEN : javafx.scene.paint.Color.GRAY);
                    Tooltip.install(statusCircle, new Tooltip(isOnline ? "Online" : "Offline"));
                    
                    VBox infoBox = new VBox(2);
                    Label nameLabel = new Label(item.getUsername() + " (" + item.getRole() + ")");
                    nameLabel.setStyle("-fx-font-weight: bold;");
                    Label deptLabel = new Label(item.getDepartment());
                    deptLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");
                    
                    infoBox.getChildren().addAll(nameLabel, deptLabel);
                    root.getChildren().addAll(statusCircle, infoBox);
                    
                    setGraphic(root);
                    setText(null);
                }
            }
        });

        VBox container = new VBox(10);
        container.getChildren().addAll(userSearchField, listView);
        
        dialog.getDialogPane().setContent(container);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                User selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openDirectChat(selected);
                    dialog.close();
                }
            }
        });

        dialog.showAndWait();
    }

    private void openDirectChat(User target) {
        // Find existing chat request or create new one
        ServiceRequest chatRequest = dataService.getAllRequests().stream()
            .filter(r -> r.getRequesterId().equals(target.getUserId()) && 
                         "Direct Chat".equals(r.getTitle()) &&
                         (r.getAssignedTo() != null && r.getAssignedTo().equals(currentUser.getUsername())))
            .findFirst()
            .orElse(null);

        if (chatRequest == null) {
            String id = "CHAT-" + UUID.randomUUID().toString().substring(0, 8);
            chatRequest = new ServiceRequest(
                id,
                "Direct Chat",
                RequestCategory.ADMINISTRATIVE_ASSISTANCE,
                RequestStatus.IN_PROGRESS,
                Priority.MEDIUM,
                "Direct chat between Admin and " + target.getUsername(),
                target.getUserId(),
                "Administration",
                java.time.LocalDateTime.now().toString()
            );
            chatRequest.setAssignedTo(currentUser.getUsername());
            dataService.addRequest(chatRequest);
            // Refresh table to show the new chat request if needed
            refresh();
        }
        
        openChatWindow(chatRequest);
    }

    @FXML
    private void handleNewRequest() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/NewRequest.fxml"));
            Scene scene = new Scene(loader.load());
            
            NewRequestController controller = loader.getController();
            controller.setUser(currentUser);
            controller.setDataService(dataService);
            controller.setDashboardController(this);
            controller.initializeDepartments();
            
            Stage stage = new Stage();
            stage.setTitle("Campath - New Request");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Requests");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(requestTable.getScene().getWindow());
        
        if (file != null) {
            try {
                dataService.exportToCSV(file.getAbsolutePath(), requestTable.getItems());
                showAlert("Export successful", "Requests exported to " + file.getName());
            } catch (Exception e) {
                showAlert("Export failed", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleAnalytics() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Analytics.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
            
            AnalyticsController controller = loader.getController();
            controller.setDataService(dataService);
            controller.setUser(currentUser);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleAssign() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        selected.setAssignedTo(currentUser.getUsername());
        if (selected.getStatus() == RequestStatus.PENDING) {
            selected.setStatus(RequestStatus.IN_PROGRESS);
        }
        dataService.updateRequest(selected);
        requestTable.refresh();
        showAlert("Success", "Request assigned to you");
    }
    
    @FXML
    private void handleEdit() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        if (selected.getStatus() != RequestStatus.DRAFT && selected.getStatus() != RequestStatus.PENDING) {
            showAlert("Cannot Edit", "Only DRAFT or PENDING requests can be edited");
            return;
        }
        
        showAlert("Edit Request", "Edit functionality - Update title, description, priority");
    }
    
    @FXML
    private void handleCancelRequest() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        if (selected.getStatus() == RequestStatus.COMPLETED || selected.getStatus() == RequestStatus.CANCELLED) {
            showAlert("Cannot Cancel", "Request is already completed or cancelled");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Request");
        confirm.setContentText("Are you sure you want to cancel request " + selected.getRequestId() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                selected.setStatus(RequestStatus.CANCELLED);
                dataService.updateRequest(selected);
                requestTable.refresh();
                showAlert("Success", "Request cancelled");
            }
        });
    }
    
    @FXML
    private void handleAccept() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        selected.setStatus(RequestStatus.IN_PROGRESS);
        selected.setAssignedTo(currentUser.getUsername());
        selected.addComment("Request accepted by " + currentUser.getUsername());
        dataService.updateRequest(selected);
        requestTable.refresh();
        showAlert("Success", "Request accepted and assigned to you");
    }
    
    @FXML
    private void handleReject() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Request");
        dialog.setHeaderText("Request: " + selected.getRequestId());
        dialog.setContentText("Reason for rejection:");
        
        dialog.showAndWait().ifPresent(reason -> {
            selected.setStatus(RequestStatus.REJECTED);
            selected.addComment("Rejected by " + currentUser.getUsername() + ": " + reason);
            dataService.updateRequest(selected);
            requestTable.refresh();
            showAlert("Success", "Request rejected");
        });
    }
    
    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Profile.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
            
            ProfileController controller = loader.getController();
            controller.setDataService(dataService);
            controller.setUser(currentUser);
            
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be redirected to the login page.");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Login.fxml"));
                    Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                    
                    Scene scene = new Scene(loader.load());
                    scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
                    
                    stage.setTitle("Campath - Login");
                    stage.setScene(scene);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    @FXML
    private void handleChat() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        openChatWindow(selected);
    }

    private void openChatWindow(ServiceRequest request) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Chat.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/com/campuscare/css/styles.css").toExternalForm());
            
            ChatController controller = loader.getController();
            controller.setup(request, currentUser, dataService);
            
            Stage stage = new Stage();
            stage.setTitle("Chat - " + request.getRequestId());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not open chat");
        }
    }
    
    @FXML
    private void handleUpdateStatus() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        ChoiceDialog<RequestStatus> dialog = new ChoiceDialog<>(selected.getStatus(), RequestStatus.values());
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Request: " + selected.getRequestId());
        dialog.setContentText("New status:");
        
        dialog.showAndWait().ifPresent(status -> {
            selected.setStatus(status);
            dataService.updateRequest(selected);
            dataService.sendNotification(currentUser.getUsername(), selected.getRequesterId(), "Request " + selected.getRequestId() + " status updated to " + status);
            requestTable.refresh();
            showAlert("Success", "Status updated to " + status);
        });
    }
    
    @FXML
    private void handleDelete() {
        ServiceRequest selected = requestTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a request");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setContentText("Delete request " + selected.getRequestId() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                dataService.deleteRequest(selected.getRequestId());
                loadRequests();
                showAlert("Success", "Request deleted");
            }
        });
    }
    
    private void showRequestDetails(ServiceRequest request) {
        StringBuilder details = new StringBuilder();
        details.append(String.format("Title: %s%n", request.getTitle()));
        details.append(String.format("Category: %s%n", request.getCategory()));
        details.append(String.format("Status: %s%n", request.getStatus()));
        details.append(String.format("Priority: %s%n", request.getPriority()));
        details.append(String.format("Requester: %s%n", request.getRequesterId()));
        details.append(String.format("Department: %s%n", request.getDepartmentId()));
        details.append(String.format("Assigned To: %s%n", (request.getAssignedTo() != null && !request.getAssignedTo().isEmpty()) ? request.getAssignedTo() : request.getDepartmentId()));
        details.append(String.format("Created: %s%n", formatTimestamp(request.getCreatedAt())));
        if (request.getCompletedAt() != null) {
            details.append(String.format("Completed: %s%n", formatTimestamp(request.getCompletedAt())));
            details.append(String.format("Resolution Time: %d hours%n", request.getResolutionTimeHours()));
        }
        details.append(String.format("Description: %s%n%n", request.getDescription()));
        if (request.getAttachmentPath() != null && !request.getAttachmentPath().isEmpty()) {
            details.append(String.format("Attachment: %s%n%n", request.getAttachmentPath()));
        }
        
        if (!request.getComments().isEmpty()) {
            details.append("Comments:\n");
            for (String comment : request.getComments()) {
                details.append("  • " + comment + "\n");
            }
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Request Details");
        alert.setHeaderText("Request ID: " + request.getRequestId());
        
        TextArea textArea = new TextArea(details.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(400);
        
        alert.getDialogPane().setContent(textArea);
        alert.showAndWait();
    }
    
    @FXML
    private void handleNotifications() {
        List<Notification> notifications = dataService.getUserNotifications(currentUser.getUserId());
        
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Notifications");
        dialog.setHeaderText("Your Notifications");
        
        ListView<Notification> listView = new ListView<>();
        listView.getItems().addAll(notifications);
        
        listView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Notification item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    VBox container = new VBox(5);
                    
                    HBox header = new HBox(10);
                    Label senderLabel = new Label(item.getSenderId() != null ? item.getSenderId() : "System");
                    senderLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                    
                    Label timeLabel = new Label(item.getTimestamp().substring(0, 16).replace("T", " "));
                    timeLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 11px;");
                    
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    
                    header.getChildren().addAll(senderLabel, spacer, timeLabel);
                    
                    Label messageLabel = new Label(item.getMessage());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-text-fill: #34495e;");
                    messageLabel.setMaxWidth(380);
                    
                    container.getChildren().addAll(header, messageLabel);
                    setGraphic(container);
                    
                    ContextMenu contextMenu = new ContextMenu();
                    MenuItem markUnreadItem = new MenuItem("Mark as Unread");
                    markUnreadItem.setOnAction(event -> {
                        dataService.markNotificationAsUnread(item.getId());
                        getListView().refresh();
                        updateNotificationBadge();
                    });
                    contextMenu.getItems().add(markUnreadItem);
                    setContextMenu(contextMenu);
                    
                    if (!item.isRead()) {
                        setStyle("-fx-background-color: #E8F4FF; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 1 0; -fx-padding: 10;");
                    }
                }
            }
        });
        
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Notification selected = listView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleNotificationClick(selected);
                    dialog.close();
                }
            }
        });
        
        if (notifications.isEmpty()) {
            listView.setPlaceholder(new Label("No notifications"));
        }
        
        for (Notification n : notifications) {
            if (!n.isRead()) {
                dataService.markNotificationAsRead(n.getId());
            }
        }
        
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().setPrefWidth(450);
        dialog.getDialogPane().setPrefHeight(500);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
        updateNotificationBadge();
    }
    
    private void handleNotificationClick(Notification n) {
        String message = n.getMessage();
        // Regex to find Request ID (REQ... or CHAT-...)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b((REQ|CHAT-)[A-Za-z0-9]+)\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        
        if (matcher.find()) {
            String requestId = matcher.group(1);
            ServiceRequest request = dataService.getAllRequests().stream()
                .filter(r -> r.getRequestId().equals(requestId))
                .findFirst()
                .orElse(null);
                
            if (request != null) {
                openChatWindow(request);
                return;
            }
        }
        
        // Fallback: Open direct chat with sender
        String senderUsername = n.getSenderId();
        if (senderUsername != null && !senderUsername.equals("System")) {
            User sender = dataService.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(senderUsername))
                .findFirst()
                .orElse(null);
                
            if (sender != null) {
                openDirectChat(sender);
            }
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public void refresh() {
        loadRequests();
        updateNotificationBadge();
    }
    
    private void updateNotificationBadge() {
        if (currentUser == null) return;
        List<Notification> notifications = dataService.getUserNotifications(currentUser.getUserId());
        long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();
        
        if (unreadCount > 0) {
            notificationsBtn.setText("🔔 Notifications (+" + unreadCount + ")");
            if (!notificationsBtn.getStyleClass().contains("notification-active")) {
                notificationsBtn.getStyleClass().add("notification-active");
            }
        } else {
            notificationsBtn.setText("🔔 Notifications");
            notificationsBtn.getStyleClass().remove("notification-active");
        }
    }
    
    private String formatTimestamp(String timestamp) {
        if (timestamp == null || timestamp.isEmpty()) return "";
        try {
            return java.time.LocalDateTime.parse(timestamp).format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return timestamp;
        }
    }
}
