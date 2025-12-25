package com.campuscare.util;

import com.campuscare.model.User;
import com.campuscare.model.UserRole;
import com.campuscare.model.ServiceRequest;
import com.campuscare.model.Notification;
import com.campuscare.model.RequestCategory;
import com.campuscare.model.RequestStatus;
import com.campuscare.model.Priority;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

public class DataService {
    private static final String USERS_FILE = "data/users.json";
    private static final String REQUESTS_FILE = "data/requests.json";
    private static final String NOTIFICATIONS_FILE = "data/notifications.json";
    
    private List<User> users;
    private List<ServiceRequest> requests;
    private List<Notification> notifications;
    
    public DataService() {
        loadData();
        // Debug: Print loaded users
        System.out.println("Loaded users: " + users.size());
        for (User u : users) {
            System.out.println("User: " + u.getUsername() + " / " + u.getPassword());
        }
    }
    
    private void loadData() {
        users = loadUsers();
        requests = loadRequests();
        notifications = loadNotifications();
        
        if (users.isEmpty()) {
            initializeDefaultUsers();
        }
    }
    
    private List<User> loadUsers() {
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) return new ArrayList<>();
            
            StringBuilder json = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                while (scanner.hasNextLine()) {
                    json.append(scanner.nextLine());
                }
            }
            return parseUsersFromJson(json.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<ServiceRequest> loadRequests() {
        try {
            File file = new File(REQUESTS_FILE);
            if (!file.exists()) return new ArrayList<>();
            
            StringBuilder json = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                while (scanner.hasNextLine()) {
                    json.append(scanner.nextLine());
                }
            }
            return parseRequestsFromJson(json.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    private List<Notification> loadNotifications() {
        try {
            File file = new File(NOTIFICATIONS_FILE);
            if (!file.exists()) return new ArrayList<>();
            
            StringBuilder json = new StringBuilder();
            try (java.util.Scanner scanner = new java.util.Scanner(file)) {
                while (scanner.hasNextLine()) {
                    json.append(scanner.nextLine());
                }
            }
            return parseNotificationsFromJson(json.toString());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public void saveUsers() {
        new File("data").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            writer.println(usersToJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveRequests() {
        new File("data").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(REQUESTS_FILE))) {
            writer.println(requestsToJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void saveNotifications() {
        new File("data").mkdirs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(NOTIFICATIONS_FILE))) {
            writer.println(notificationsToJson());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeDefaultUsers() {
        users.clear();
        users.add(new User("ADMIN001", "admin", "admin123", "admin@campus.edu", UserRole.ADMIN, "Administration"));
        users.add(new User("1892/16", "1892/16", "pass123", "student1892@campus.edu", UserRole.STUDENT, "Computer Science"));
        users.add(new User("1893/16", "1893/16", "pass123", "student1893@campus.edu", UserRole.STUDENT, "Engineering"));
        users.add(new User("LECT001", "lecturer1", "pass123", "lecturer1@campus.edu", UserRole.LECTURER, "Engineering"));
        users.add(new User("STAFF001", "staff1", "pass123", "staff1@campus.edu", UserRole.STAFF, "Finance"));
        users.add(new User("DEPT-IT001", "itstaff", "pass123", "itstaff@campus.edu", UserRole.DEPARTMENT_STAFF, "IT Support"));
        users.add(new User("DEPT-FAC001", "facilitystaff", "pass123", "facilitystaff@campus.edu", UserRole.DEPARTMENT_STAFF, "Facilities Maintenance"));
        users.add(new User("DEPT-ACAD001", "acadstaff", "pass123", "acadstaff@campus.edu", UserRole.DEPARTMENT_STAFF, "Academic Advising"));
        users.add(new User("DEPT-ADMIN001", "adminstaff", "pass123", "adminstaff@campus.edu", UserRole.DEPARTMENT_STAFF, "Administrative Services"));
        saveUsers();
        System.out.println("Initialized " + users.size() + " default users");
    }
    
    public java.util.List<String> getDepartments() {
        return java.util.Arrays.asList(
            "IT Support",
            "Facilities Maintenance", 
            "Academic Advising",
            "Administrative Services",
            "Library Services",
            "Student Affairs"
        );
    }
    
    public User authenticate(String username, String password) {
        return users.stream()
            .filter(u -> u.getUsername().equals(username) && u.getPassword().equals(password))
            .findFirst()
            .orElse(null);
    }
    
    public void addRequest(ServiceRequest request) {
        requests.add(request);
        saveRequests();
    }
    
    public List<ServiceRequest> getAllRequests() {
        return new ArrayList<>(requests);
    }
    
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
    
    public List<ServiceRequest> getRequestsByUser(String userId) {
        return requests.stream()
            .filter(r -> r.getRequesterId().equals(userId))
            .collect(Collectors.toList());
    }
    
    public List<ServiceRequest> getRequestsByDepartment(String department) {
        System.out.println("Filtering requests for department: " + department);
        List<ServiceRequest> filtered = requests.stream()
            .filter(r -> {
                boolean matches = r.getDepartmentId() != null && r.getDepartmentId().equals(department);
                if (matches) {
                    System.out.println("Found request " + r.getRequestId() + " for department " + r.getDepartmentId());
                }
                return matches;
            })
            .collect(Collectors.toList());
        System.out.println("Found " + filtered.size() + " requests for department " + department);
        return filtered;
    }
    
    public void updateRequest(ServiceRequest request) {
        for (int i = 0; i < requests.size(); i++) {
            if (requests.get(i).getRequestId().equals(request.getRequestId())) {
                requests.set(i, request);
                saveRequests();
                return;
            }
        }
    }
    
    public void deleteRequest(String requestId) {
        requests.removeIf(r -> r.getRequestId().equals(requestId));
        saveRequests();
    }
    
    public void exportToCSV(String filename, List<ServiceRequest> requestsToExport) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Request ID,Title,Category,Status,Priority,Requester,Created At");
            for (ServiceRequest req : requestsToExport) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s%n",
                    req.getRequestId(), req.getTitle(), req.getCategory(),
                    req.getStatus(), req.getPriority(), req.getRequesterId(),
                    req.getCreatedAt());
            }
        }
    }
    
    // --- Notification Methods ---
    
    public void sendNotification(String senderId, String recipientId, String message) {
        notifications.add(new Notification(senderId, recipientId, message));
        saveNotifications();
    }
    
    public void notifyDepartment(String senderId, String department, String message) {
        List<User> staff = users.stream()
            .filter(u -> u.getDepartment().equals(department) && u.getRole() == UserRole.DEPARTMENT_STAFF)
            .collect(Collectors.toList());
            
        for (User u : staff) {
            sendNotification(senderId, u.getUserId(), message);
        }
    }
    
    public List<Notification> getUserNotifications(String userId) {
        return notifications.stream()
            .filter(n -> n.getRecipientId().equals(userId))
            .sorted((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    public void markNotificationAsRead(String notificationId) {
        notifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(true));
        saveNotifications();
    }
    
    public void markNotificationAsUnread(String notificationId) {
        notifications.stream()
            .filter(n -> n.getId().equals(notificationId))
            .findFirst()
            .ifPresent(n -> n.setRead(false));
        saveNotifications();
    }
    
    private String usersToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            json.append("  {\n");
            json.append("    \"userId\": \"").append(u.getUserId()).append("\",\n");
            json.append("    \"username\": \"").append(u.getUsername()).append("\",\n");
            json.append("    \"password\": \"").append(u.getPassword()).append("\",\n");
            json.append("    \"email\": \"").append(u.getEmail()).append("\",\n");
            json.append("    \"role\": \"").append(u.getRole()).append("\",\n");
            json.append("    \"department\": \"").append(u.getDepartment()).append("\",\n");
            json.append("    \"phone\": \"").append(u.getPhone() != null ? u.getPhone() : "").append("\",\n");
            json.append("    \"avatarPath\": \"").append(u.getAvatarPath() != null ? u.getAvatarPath().replace("\\", "\\\\") : "").append("\"\n");
            json.append("  }").append(i < users.size() - 1 ? "," : "").append("\n");
        }
        json.append("]");
        return json.toString();
    }
    
    private String requestsToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < requests.size(); i++) {
            ServiceRequest r = requests.get(i);
            json.append("  {\n");
            json.append("    \"requestId\": \"").append(r.getRequestId()).append("\",\n");
            json.append("    \"title\": \"").append(r.getTitle()).append("\",\n");
            json.append("    \"category\": \"").append(r.getCategory()).append("\",\n");
            json.append("    \"status\": \"").append(r.getStatus()).append("\",\n");
            json.append("    \"priority\": \"").append(r.getPriority()).append("\",\n");
            json.append("    \"description\": \"").append(r.getDescription() != null ? r.getDescription() : "").append("\",\n");
            json.append("    \"requesterId\": \"").append(r.getRequesterId()).append("\",\n");
            json.append("    \"departmentId\": \"").append(r.getDepartmentId() != null ? r.getDepartmentId() : "").append("\",\n");
            json.append("    \"createdAt\": \"").append(r.getCreatedAt()).append("\",\n");
            json.append("    \"assignedTo\": \"").append(r.getAssignedTo() != null ? r.getAssignedTo() : "").append("\",\n");
            json.append("    \"completedAt\": \"").append(r.getCompletedAt() != null ? r.getCompletedAt() : "").append("\"\n");
            json.append("  }").append(i < requests.size() - 1 ? "," : "").append("\n");
        }
        json.append("]");
        return json.toString();
    }
    
    private String notificationsToJson() {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < notifications.size(); i++) {
            Notification n = notifications.get(i);
            json.append("  {\n");
            json.append("    \"id\": \"").append(n.getId()).append("\",\n");
            json.append("    \"senderId\": \"").append(n.getSenderId()).append("\",\n");
            json.append("    \"recipientId\": \"").append(n.getRecipientId()).append("\",\n");
            json.append("    \"message\": \"").append(n.getMessage()).append("\",\n");
            json.append("    \"isRead\": \"").append(n.isRead()).append("\",\n");
            json.append("    \"timestamp\": \"").append(n.getTimestamp()).append("\"\n");
            json.append("  }").append(i < notifications.size() - 1 ? "," : "").append("\n");
        }
        json.append("]");
        return json.toString();
    }
    
    private List<User> parseUsersFromJson(String json) {
        List<User> userList = new ArrayList<>();
        // Simple JSON parsing for users
        String[] userBlocks = json.split("\\},");
        for (String block : userBlocks) {
            if (block.contains("userId")) {
                String userId = extractValue(block, "userId");
                String username = extractValue(block, "username");
                String password = extractValue(block, "password");
                String email = extractValue(block, "email");
                String roleStr = extractValue(block, "role");
                String department = extractValue(block, "department");
                String phone = extractValue(block, "phone");
                String avatarPath = extractValue(block, "avatarPath");
                
                UserRole role = UserRole.valueOf(roleStr);
                User u = new User(userId, username, password, email, role, department);
                u.setPhone(phone);
                u.setAvatarPath(avatarPath);
                userList.add(u);
            }
        }
        return userList;
    }
    
    private List<ServiceRequest> parseRequestsFromJson(String json) {
        List<ServiceRequest> requestList = new ArrayList<>();
        // Simple JSON parsing for requests
        String[] requestBlocks = json.split("\\},");
        for (String block : requestBlocks) {
            if (block.contains("requestId")) {
                String requestId = extractValue(block, "requestId");
                String title = extractValue(block, "title");
                String categoryStr = extractValue(block, "category");
                String statusStr = extractValue(block, "status");
                String priorityStr = extractValue(block, "priority");
                String description = extractValue(block, "description");
                String requesterId = extractValue(block, "requesterId");
                String departmentId = extractValue(block, "departmentId");
                String createdAt = extractValue(block, "createdAt");
                
                RequestCategory category = RequestCategory.valueOf(categoryStr);
                RequestStatus status = RequestStatus.valueOf(statusStr);
                Priority priority = Priority.valueOf(priorityStr);
                
                requestList.add(new ServiceRequest(requestId, title, category, status, priority, description, requesterId, departmentId, createdAt));
            }
        }
        return requestList;
    }
    
    private List<Notification> parseNotificationsFromJson(String json) {
        List<Notification> list = new ArrayList<>();
        String[] blocks = json.split("\\},");
        for (String block : blocks) {
            if (block.contains("recipientId")) {
                String id = extractValue(block, "id");
                String senderId = extractValue(block, "senderId");
                String recipientId = extractValue(block, "recipientId");
                String message = extractValue(block, "message");
                String isReadStr = extractValue(block, "isRead");
                String timestamp = extractValue(block, "timestamp");
                
                boolean isRead = Boolean.parseBoolean(isReadStr);
                list.add(new Notification(id, senderId, recipientId, message, isRead, timestamp));
            }
        }
        return list;
    }
    
    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\": \"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return "";
        return json.substring(start, end);
    }
}
