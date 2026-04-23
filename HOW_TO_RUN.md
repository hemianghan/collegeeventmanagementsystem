# 🎓 College Event Management System - How to Run

## ✅ Prerequisites

1. **Java 17** installed and configured
2. **Web Browser** (Chrome, Firefox, Edge)

---

## 🚀 Step-by-Step Instructions

### **Step 1: Fix Java Installation (If needed)**

If you get "JAVA_HOME not found" error, follow these steps:

1. Open **Windows Search** (press Windows key)
2. Type: **"environment variables"**
3. Click **"Edit the system environment variables"**
4. Click **"Environment Variables"** button
5. Under **System variables**, click **"New"**
6. Add:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Eclipse Adoptium\jdk-17.0.18.10-hotspot`
7. Find **"Path"** variable, click **"Edit"**
8. Click **"New"** and add: `%JAVA_HOME%\bin`
9. Click **OK** on all windows
10. **Restart VS Code**

---

### **Step 2: Start the Backend Server**

Open terminal in VS Code (Ctrl + `) and run:

```bash
cd server
..\gradlew bootRun
```

**Wait until you see:**
```
Started EventApplication in X seconds
```

✅ Server is now running on **http://localhost:8080**

---

### **Step 3: Open the Website in Browser**

Open your web browser and go to:

```
http://localhost:8080/login.html
```

---

## 🎯 How to Use

### **Login Page**

1. **Email**: Enter your email (e.g., `student@college.edu`)
2. **Password**: Enter your password
3. **Select Role**: Click either **Student** or **Admin**
4. Click **Login** button

### **Default Admin Account**

- **Email**: `hemishaanghan1@gmail.com`
- **Password**: `admin123`
- **Role**: Admin

### **For Students**

- Any new email will be automatically registered
- Just enter email, password, and select "Student"

---

## 📱 Available Pages

| URL | Description |
|-----|-------------|
| `http://localhost:8080/login.html` | Login Page (Start here) |
| `http://localhost:8080/index.html` | Main Dashboard |
| `http://localhost:8080/api/events` | API - View all events (JSON) |
| `http://localhost:8080/h2-console` | Database Console |

---

## 🛠️ Troubleshooting

### Problem: "Port 8080 already in use"

**Solution**: Another program is using port 8080. Stop it or change the port in `application.properties`

### Problem: "Cannot connect to server"

**Solution**: Make sure the backend server is running (Step 2)

### Problem: "Login failed"

**Solution**: 
- Check if server is running
- Try the default admin account
- Check server logs in terminal

---

## 🎨 Features

### **Student Features:**
- ✅ View all events
- ✅ Register for events
- ✅ View my registrations
- ✅ Rate and review events
- ✅ View certificates

### **Admin Features:**
- ✅ Create new events
- ✅ Edit/Delete events
- ✅ View all registrations
- ✅ Manage student registrations
- ✅ View statistics

---

## 📊 Database Access

To view the database:

1. Go to: `http://localhost:8080/h2-console`
2. **JDBC URL**: `jdbc:h2:file:./data/eventdb`
3. **Username**: `sa`
4. **Password**: (leave empty)
5. Click **Connect**

---

## 🔄 To Stop the Server

Press **Ctrl + C** in the terminal where the server is running

---

## 📝 Quick Commands Reference

```bash
# Start server
cd server
..\gradlew bootRun

# Check Java version
java -version

# View project structure
dir
```

---

## 🎉 You're All Set!

Your College Event Management System is now running!

**Login URL**: http://localhost:8080/login.html

Enjoy! 🚀
