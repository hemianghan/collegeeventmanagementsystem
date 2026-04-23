# 🎓 College Event Management System

A comprehensive web-based College Event Management System built with Spring Boot and modern web technologies.

## ✨ Features

### 👨‍🎓 Student Panel
- **Event Registration**: Browse and register for college events
- **My Events**: View registered events with cancel functionality  
- **Past Events**: Track completed event registrations
- **Profile Management**: View certificates earned, change password, update profile
- **Notifications**: Receive announcements from admin
- **Feedback Board**: View event feedback and experiences
- **Registration Deadlines**: See "3 days left" or "last day" warnings

### 👨‍💼 Admin Panel
- **Event Management**: Create, edit, and manage events with deadline tracking
- **Student Registration**: View 10 registrations per event
- **Notification System**: Send announcements to all students
- **Certificate Generation**: Generate certificates for event participants
- **Admin Management**: Add/remove admin users
- **Dashboard Analytics**: Overview of system statistics

### 🔧 Technical Features
- **Real-time Updates**: Live notification system
- **Data Persistence**: H2 database with permanent storage
- **Responsive Design**: Works on desktop, tablet, and mobile
- **Deadline Tracking**: Automatic deadline warnings and validation
- **Secure Authentication**: Session-based user management
- **File Upload**: Event image management
- **Certificate System**: Track and display earned certificates

## 🚀 Tech Stack

- **Backend**: Java Spring Boot, Spring Data JPA, H2 Database
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Authentication**: Session-based authentication
- **Database**: H2 (file-based for persistence)
- **Build Tool**: Gradle
- **Server**: Embedded Tomcat

## 📦 Quick Start

### Prerequisites
- Java 17 or higher
- Gradle (or use included wrapper)

### Installation & Setup

1. **Clone the repository**
```bash
git clone https://github.com/hemishaanghan/college-event-management-system.git
cd college-event-management-system
```

2. **Run the application**
```bash
# Using Gradle wrapper (recommended)
./gradlew :server:bootRun

# Or if you have Gradle installed
gradle :server:bootRun
```

3. **Access the application**
- Open your browser and go to: `http://localhost:8080`
- The application will automatically create sample data on first run

### 👥 Test Accounts

**Students:**
- Email: `student@college.edu` | Password: `password`
- Email: `hemishaanghan1@gmail.com` | Password: `password`

**Admin:**
- Email: `admin@college.edu` | Password: `admin`

## 📱 Usage

### For Students:
1. Login with student credentials
2. Browse available events in the dashboard
3. Register for events (check deadline warnings)
4. View "My Events" to see registrations and cancel if needed
5. Check "Profile" to see certificates earned and update account
6. View "Feedback" to see event experiences

### For Admins:
1. Login with admin credentials  
2. Create and manage events with registration deadlines
3. View student registrations (10 per event)
4. Send notifications to all students
5. Generate certificates for event participants
6. Manage admin users

## 🗂️ Project Structure

```
college-event-management-system/
├── server/                          # Spring Boot backend
│   ├── src/main/java/              # Java source code
│   │   └── com/college/event/      # Main package
│   │       ├── controller/         # REST controllers
│   │       ├── model/             # JPA entities
│   │       ├── repository/        # Data repositories
│   │       ├── service/           # Business logic
│   │       └── dto/               # Data transfer objects
│   ├── src/main/resources/        # Resources
│   │   ├── static/               # Frontend files (HTML, CSS, JS)
│   │   └── application.properties # Configuration
│   └── build.gradle              # Backend dependencies
├── app/                          # Android app (if applicable)
├── data/                         # H2 database files
└── README.md                     # This file
```

## 🎯 Key Features Implemented

- ✅ **Student Registration System** (12 events per student)
- ✅ **Admin Panel** (10 registrations per event view)
- ✅ **Registration Deadline Tracking** ("3 days left" warnings)
- ✅ **Notification System** (Admin to all students)
- ✅ **Past Events Registration** (Historical data)
- ✅ **Cancel Registration** (Student functionality)
- ✅ **Profile Management** (Certificate count, password change)
- ✅ **Feedback System** (Simple feedback display)
- ✅ **Data Persistence** (H2 database storage)
- ✅ **Sample Data** (13 realistic events with photos)

## 🔧 Configuration

The application uses H2 database with file-based storage for data persistence. Configuration can be found in:
- `server/src/main/resources/application.properties`

Database location: `./data/eventdb`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- Built with Spring Boot framework
- Uses H2 database for data persistence
- Responsive design with modern web technologies
- Sample data includes realistic event scenarios

## 📞 Support

If you encounter any issues or have questions, please open an issue on GitHub.

---

**Made with ❤️ for college event management**