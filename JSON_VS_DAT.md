# JSON vs .DAT Files Comparison

## ✅ Benefits of JSON over .DAT:

### **Human Readable**
```json
// users.json
[
  {
    "userId": "1892/16",
    "username": "1892/16", 
    "password": "pass123",
    "email": "student1892@campus.edu",
    "role": "STUDENT",
    "department": "Computer Science"
  }
]
```

### **Easy Debugging**
- Can open and read JSON files in any text editor
- Easy to spot data issues
- Can manually edit if needed

### **Cross-Platform**
- Works with any programming language
- Can be read by web apps, mobile apps
- Standard format

### **Version Control Friendly**
- Git can show meaningful diffs
- Can track changes line by line
- Better for collaboration

## ❌ Drawbacks of .DAT (Binary):

### **Not Human Readable**
```
// users.dat (binary)
aced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a65787000000007770400000007737200...
```

### **Hard to Debug**
- Cannot read file contents
- Difficult to troubleshoot issues
- Need special tools to inspect

### **Java-Only**
- Only works with Java ObjectStreams
- Cannot be used by other languages
- Platform dependent

## 📊 File Size Comparison:

### **JSON Format:**
```json
// requests.json (readable)
[
  {
    "requestId": "REQ12345",
    "title": "Fix projector",
    "category": "IT_SUPPORT",
    "status": "PENDING",
    "priority": "HIGH",
    "description": "Projector not working in Room 301",
    "requesterId": "1892/16",
    "departmentId": "IT Support",
    "createdAt": "2024-01-15T09:00:00"
  }
]
```

### **.DAT Format:**
```
Binary data: aced0005737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a6578700000000177040000000173720...
```

## 🔧 Implementation Changes:

### **File Extensions:**
- `data/users.dat` → `data/users.json`
- `data/requests.dat` → `data/requests.json`

### **Save Method:**
```java
// OLD (.dat)
ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
oos.writeObject(users);

// NEW (JSON)
PrintWriter writer = new PrintWriter(new FileWriter(file));
writer.println(usersToJson());
```

### **Load Method:**
```java
// OLD (.dat)
ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
return (List<User>) ois.readObject();

// NEW (JSON)
Scanner scanner = new Scanner(file);
String json = scanner.nextLine();
return parseUsersFromJson(json);
```

## 📁 Sample JSON Files:

### **users.json:**
```json
[
  {
    "userId": "ADMIN001",
    "username": "admin",
    "password": "admin123",
    "email": "admin@campus.edu",
    "role": "ADMIN",
    "department": "Administration"
  },
  {
    "userId": "1892/16",
    "username": "1892/16",
    "password": "pass123",
    "email": "student1892@campus.edu",
    "role": "STUDENT",
    "department": "Computer Science"
  }
]
```

### **requests.json:**
```json
[
  {
    "requestId": "REQ12345",
    "title": "Fix projector",
    "category": "IT_SUPPORT",
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "description": "Projector not working in Room 301",
    "requesterId": "1892/16",
    "departmentId": "IT Support",
    "createdAt": "2024-01-15T09:00:00",
    "assignedTo": "itstaff",
    "completedAt": ""
  }
]
```

## 🚀 Advantages for Development:

### **Easy Testing**
- Can create test data by editing JSON files
- Can verify data changes visually
- Can backup/restore data easily

### **Better Debugging**
- See exactly what data is stored
- Identify data corruption issues
- Track data changes over time

### **Future Extensibility**
- Easy to add new fields
- Can be consumed by web APIs
- Compatible with databases (MongoDB, etc.)

## ⚡ Performance:

### **JSON:**
- Slightly slower parsing
- Larger file size (text vs binary)
- More memory usage during parsing

### **.DAT:**
- Faster serialization/deserialization
- Smaller file size
- Less memory usage

**For this university app:** JSON benefits outweigh performance costs.

## 🎯 Recommendation:

**Use JSON** because:
1. **Better for learning** - Students can see data structure
2. **Easier debugging** - Can inspect files directly
3. **More professional** - Industry standard format
4. **Future-proof** - Can integrate with web/mobile later
5. **Version control friendly** - Better for Git

The app now uses JSON files instead of binary .dat files!