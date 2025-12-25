package com.campuscare.controller;

import com.campuscare.model.*;
import com.campuscare.util.DataService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.*;
import java.util.stream.Collectors;
import javafx.scene.chart.*;

public class AnalyticsController {
    @FXML private Label totalRequestsLabel;
    @FXML private Label pendingLabel;
    @FXML private Label inProgressLabel;
    @FXML private Label completedLabel;
    @FXML private Label avgResolutionLabel;
    @FXML private Label completionRateLabel;
    @FXML private PieChart categoryChart;
    @FXML private BarChart<String, Number> priorityChart;
    
    private DataService dataService;
    private User currentUser;
    
    public void setDataService(DataService dataService) {
        this.dataService = dataService;
    }
    
    public void setUser(User user) {
        this.currentUser = user;
        loadAnalytics();
    }
    
    @FXML
    private void initialize() {
        // Charts are initialized in loadAnalytics
    }
    
    private void loadAnalytics() {
        List<ServiceRequest> requests = dataService.getAllRequests();
        
        totalRequestsLabel.setText(String.valueOf(requests.size()));
        pendingLabel.setText(String.valueOf(requests.stream().filter(r -> r.getStatus() == RequestStatus.PENDING).count()));
        inProgressLabel.setText(String.valueOf(requests.stream().filter(r -> r.getStatus() == RequestStatus.IN_PROGRESS).count()));
        completedLabel.setText(String.valueOf(requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count()));
        
        long completed = requests.stream().filter(r -> r.getStatus() == RequestStatus.COMPLETED).count();
        double completionRate = requests.isEmpty() ? 0 : (completed * 100.0 / requests.size());
        completionRateLabel.setText(String.format("%.1f%%", completionRate));
        
        double avgHours = requests.stream()
            .filter(r -> r.getResolutionTimeHours() > 0)
            .mapToLong(ServiceRequest::getResolutionTimeHours)
            .average()
            .orElse(0);
        avgResolutionLabel.setText(String.format("%.1fh", avgHours));
        
        Map<String, Long> categoryStats = requests.stream()
            .filter(r -> r.getCategory() != null)
            .collect(Collectors.groupingBy(r -> r.getCategory().toString(), Collectors.counting()));
            
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        categoryStats.forEach((k, v) -> pieData.add(new PieChart.Data(k, v)));
        categoryChart.setData(pieData);
        
        Map<String, Long> priorityStats = requests.stream()
            .filter(r -> r.getPriority() != null)
            .collect(Collectors.groupingBy(r -> r.getPriority().toString(), Collectors.counting()));
            
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Requests");
        // Sort priorities logically if possible, or just add them
        priorityStats.forEach((k, v) -> series.getData().add(new XYChart.Data<>(k, v)));
        
        priorityChart.getData().clear();
        priorityChart.getData().add(series);
    }
    
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/campuscare/view/Dashboard.fxml"));
            Stage stage = (Stage) totalRequestsLabel.getScene().getWindow();
            
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
