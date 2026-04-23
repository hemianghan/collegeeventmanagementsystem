package com.college.event;

import com.college.event.model.Certificate;
import com.college.event.model.Event;
import com.college.event.model.Memory;
import com.college.event.model.Notification;
import com.college.event.model.Registration;
import com.college.event.model.User;
import com.college.event.repository.CertificateRepository;
import com.college.event.repository.EventRepository;
import com.college.event.repository.MemoryRepository;
import com.college.event.repository.NotificationRepository;
import com.college.event.repository.RegistrationRepository;
import com.college.event.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class EventApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventApplication.class, args);
    }

    @Bean
    public CommandLineRunner dataLoader(EventRepository eventRepo, UserRepository userRepo, CertificateRepository certRepo, NotificationRepository notificationRepo, RegistrationRepository registrationRepo, MemoryRepository memoryRepo) {
        return args -> {
            // 1. Create a clean Admin account
            String adminEmail = "hemishaanghan1@gmail.com";
            User admin = null;
            if (userRepo.findByEmail(adminEmail).isEmpty()) {
                admin = new User();
                admin.setName("Admin User");
                admin.setEmail(adminEmail);
                admin.setPassword("admin123");
                admin.setRole("Admin");
                admin = userRepo.save(admin);
                System.out.println("Clean Admin Created: " + adminEmail);
            } else {
                admin = userRepo.findByEmail(adminEmail).get();
            }

            // 2. Add 40 events (20 current + 20 past) with 5 events per category
            if (eventRepo.count() == 0) {
                
                // CULTURAL EVENTS (10 total: 5 past + 5 current)
                // Past Cultural Events
                eventRepo.save(new Event("Classical Dance Night", "Main Auditorium", "Traditional dance performances", "Cultural", "2026-03-15", ""));
                eventRepo.save(new Event("Poetry Slam", "Literature Hall", "Student poetry competition", "Cultural", "2026-03-20", ""));
                eventRepo.save(new Event("Art Exhibition", "Art Gallery", "Student artwork showcase", "Cultural", "2026-04-01", ""));
                eventRepo.save(new Event("Drama Festival", "Theater Hall", "Inter-college drama competition", "Cultural", "2026-04-10", ""));
                eventRepo.save(new Event("Music Concert", "Open Air Theater", "Classical music evening", "Cultural", "2026-04-15", ""));
                
                // Current Cultural Events
                eventRepo.save(new Event("Cultural Fest 2026", "Main Campus", "Annual cultural celebration", "Cultural", "2026-05-15", ""));
                eventRepo.save(new Event("Folk Dance Competition", "Auditorium", "Regional folk dance contest", "Cultural", "2026-05-20", ""));
                eventRepo.save(new Event("Painting Workshop", "Art Studio", "Learn watercolor techniques", "Cultural", "2026-06-01", ""));
                eventRepo.save(new Event("Literary Meet", "Library Hall", "Book reading and discussion", "Cultural", "2026-06-10", ""));
                eventRepo.save(new Event("Fashion Show", "Main Stage", "Student fashion showcase", "Cultural", "2026-06-25", ""));

                // TECHNICAL EVENTS (10 total: 5 past + 5 current)
                // Past Technical Events
                eventRepo.save(new Event("Hackathon 2026", "Computer Lab", "24-hour coding challenge", "Technical", "2026-03-10", ""));
                eventRepo.save(new Event("AI Workshop", "Tech Center", "Introduction to Machine Learning", "Technical", "2026-03-25", ""));
                eventRepo.save(new Event("Web Dev Bootcamp", "Lab 201", "Full-stack development training", "Technical", "2026-04-05", ""));
                eventRepo.save(new Event("Robotics Competition", "Engineering Hall", "Build and program robots", "Technical", "2026-04-12", ""));
                eventRepo.save(new Event("Cybersecurity Seminar", "Conference Room", "Learn ethical hacking", "Technical", "2026-04-18", ""));
                
                // Current Technical Events
                eventRepo.save(new Event("Tech Symposium", "Main Auditorium", "Latest technology trends", "Technical", "2026-05-10", ""));
                eventRepo.save(new Event("Mobile App Contest", "Innovation Lab", "Develop mobile applications", "Technical", "2026-05-25", ""));
                eventRepo.save(new Event("Cloud Computing Workshop", "Tech Hub", "AWS and Azure training", "Technical", "2026-06-05", ""));
                eventRepo.save(new Event("Data Science Bootcamp", "Computer Center", "Big data analytics", "Technical", "2026-06-15", ""));
                eventRepo.save(new Event("IoT Project Expo", "Exhibition Hall", "Internet of Things showcase", "Technical", "2026-06-30", ""));

                // SPORTS EVENTS (10 total: 5 past + 5 current)
                // Past Sports Events
                eventRepo.save(new Event("Cricket Tournament", "Sports Ground", "Inter-department cricket", "Sports", "2026-03-12", ""));
                eventRepo.save(new Event("Basketball Championship", "Sports Complex", "College basketball finals", "Sports", "2026-03-28", ""));
                eventRepo.save(new Event("Swimming Competition", "Pool Area", "Individual and relay races", "Sports", "2026-04-02", ""));
                eventRepo.save(new Event("Athletics Meet", "Track Field", "Running and field events", "Sports", "2026-04-08", ""));
                eventRepo.save(new Event("Badminton Tournament", "Indoor Stadium", "Singles and doubles matches", "Sports", "2026-04-20", ""));
                
                // Current Sports Events
                eventRepo.save(new Event("Football League", "Main Ground", "Inter-college football", "Sports", "2026-05-12", ""));
                eventRepo.save(new Event("Tennis Open", "Tennis Court", "College tennis championship", "Sports", "2026-05-28", ""));
                eventRepo.save(new Event("Volleyball Championship", "Sports Hall", "Team volleyball competition", "Sports", "2026-06-08", ""));
                eventRepo.save(new Event("Marathon 2026", "Campus Route", "5K and 10K running event", "Sports", "2026-06-20", ""));
                eventRepo.save(new Event("Sports Day", "Entire Campus", "Annual sports celebration", "Sports", "2026-07-05", ""));

                // FUN EVENTS (10 total: 5 past + 5 current)
                // Past Fun Events
                eventRepo.save(new Event("Game Night", "Student Center", "Board games and video games", "Fun", "2026-03-18", ""));
                eventRepo.save(new Event("Comedy Show", "Auditorium", "Stand-up comedy evening", "Fun", "2026-03-30", ""));
                eventRepo.save(new Event("Karaoke Night", "Music Room", "Sing your favorite songs", "Fun", "2026-04-06", ""));
                eventRepo.save(new Event("Treasure Hunt", "Entire Campus", "Campus-wide treasure hunt", "Fun", "2026-04-14", ""));
                eventRepo.save(new Event("Movie Marathon", "Auditorium", "Classic movie screening", "Fun", "2026-04-22", ""));
                
                // Current Fun Events
                eventRepo.save(new Event("Talent Show", "Main Stage", "Showcase your unique talents", "Fun", "2026-05-18", ""));
                eventRepo.save(new Event("Food Festival", "Campus Grounds", "Taste different cuisines", "Fun", "2026-06-02", ""));
                eventRepo.save(new Event("DJ Night", "Open Area", "Dance to the beats", "Fun", "2026-06-12", ""));
                eventRepo.save(new Event("Magic Show", "Auditorium", "Professional magic performance", "Fun", "2026-06-22", ""));
                eventRepo.save(new Event("Carnival Day", "Main Campus", "Fun rides and games", "Fun", "2026-07-10", ""));

                System.out.println("40 Events Seeded Successfully!");
                System.out.println("- 20 Past Events (before 2026-04-23)");
                System.out.println("- 20 Current Events (2026-04-23 and after)");
                System.out.println("- 10 Cultural, 10 Technical, 10 Sports, 10 Fun");
            }
            
            // 3. Generate sample certificates for past events
            if (certRepo.count() == 0) {
                List<Event> allEvents = eventRepo.findAll();
                
                // Filter past events (before today)
                List<Event> pastEvents = allEvents.stream()
                    .filter(e -> {
                        try {
                            return java.time.LocalDate.parse(e.getDate()).isBefore(java.time.LocalDate.now());
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .limit(6)
                    .toList();
                
                // Use consistent student name and signature for all certificates
                // Different student names for variety
                String[] studentNames = {
                    "Rajesh Kumar",
                    "Priya Sharma",
                    "Amit Patel",
                    "Sneha Reddy",
                    "Vikram Singh",
                    "Anjali Gupta"
                };
                
                // Random authorized signatures for each certificate
                String[] authorizedSignatures = {
                    "Dr. Rajesh Kumar (Principal)",
                    "Prof. Sunita Verma (Dean)",
                    "Dr. Anil Mehta (HOD)",
                    "Prof. Kavita Desai (Coordinator)",
                    "Dr. Ramesh Iyer (Director)",
                    "Prof. Neha Joshi (Vice Principal)"
                };
                
                String collegeFullName = "Shree Swami Atmanand Saraswati Institute of Technology (SSASIT)";
                
                String[] categories = {"Participant", "Winner", "Runner-up", "Participant", "Winner", "Participant"};
                String[] positions = {null, "1st", "2nd", null, "1st", null};
                
                for (int i = 0; i < Math.min(6, pastEvents.size()); i++) {
                    Event event = pastEvents.get(i);
                    
                    Certificate cert = new Certificate();
                    cert.setUserId(admin.getId());
                    cert.setEventId(event.getId());
                    cert.setUserName(studentNames[i]); // Different student name for each certificate
                    cert.setUserEmail(admin.getEmail()); // Keep same email for admin
                    cert.setEventTitle(event.getTitle());
                    cert.setCertificateId("CERT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    cert.setCategory(categories[i]);
                    cert.setPosition(positions[i]);
                    cert.setIssuedDate(LocalDateTime.now().minusDays(30 - i * 5));
                    cert.setIssuerName(collegeFullName); // Full college name at top
                    cert.setIssuerSignature(authorizedSignatures[i]); // Random signature for each
                    cert.setCertificateTemplate("Standard");
                    cert.setQrCodeData("https://ssasit.edu/verify/" + cert.getCertificateId());
                    cert.setVerificationUrl("https://ssasit.edu/verify/" + cert.getCertificateId());
                    cert.setIsActive(true);
                    cert.setNotes("Certificate issued by " + collegeFullName);
                    
                    certRepo.save(cert);
                }
                
                System.out.println("6 Sample Certificates Generated for Past Events!");
                System.out.println("College: " + collegeFullName);
                System.out.println("Student Names: Different for each certificate");
                System.out.println("Authorized Signatures: Random for each certificate");
            }
            
            // 4. Generate sample notifications
            if (notificationRepo.count() == 0) {
                List<Event> allEvents = eventRepo.findAll();
                
                // Event Approved Notification
                if (allEvents.size() > 0) {
                    Notification notif1 = new Notification();
                    notif1.setUserId(admin.getId());
                    notif1.setUserName(admin.getName());
                    notif1.setUserEmail(admin.getEmail());
                    notif1.setTitle("Event Approved");
                    notif1.setMessage("Your event '" + allEvents.get(0).getTitle() + "' has been approved by the admin!");
                    notif1.setType("EVENT_APPROVED");
                    notif1.setRelatedEventId(allEvents.get(0).getId());
                    notif1.setRelatedEventTitle(allEvents.get(0).getTitle());
                    notif1.setCreatedDate(LocalDateTime.now().minusHours(2));
                    notificationRepo.save(notif1);
                }
                
                // Certificate Available Notification
                List<Certificate> certs = certRepo.findAll();
                if (certs.size() > 0) {
                    Notification notif2 = new Notification();
                    notif2.setUserId(admin.getId());
                    notif2.setUserName(admin.getName());
                    notif2.setUserEmail(admin.getEmail());
                    notif2.setTitle("Certificate Available");
                    notif2.setMessage("Your certificate for '" + certs.get(0).getEventTitle() + "' is now available for download!");
                    notif2.setType("CERTIFICATE_AVAILABLE");
                    notif2.setRelatedEventId(certs.get(0).getEventId());
                    notif2.setRelatedEventTitle(certs.get(0).getEventTitle());
                    notif2.setCreatedDate(LocalDateTime.now().minusHours(5));
                    notificationRepo.save(notif2);
                }
                
                // Event Starting Soon Notifications
                List<Event> upcomingEvents = allEvents.stream()
                    .filter(e -> {
                        try {
                            return java.time.LocalDate.parse(e.getDate()).isAfter(java.time.LocalDate.now());
                        } catch (Exception ex) {
                            return false;
                        }
                    })
                    .limit(3)
                    .toList();
                
                for (int i = 0; i < Math.min(3, upcomingEvents.size()); i++) {
                    Event event = upcomingEvents.get(i);
                    Notification notif = new Notification();
                    notif.setUserId(admin.getId());
                    notif.setUserName(admin.getName());
                    notif.setUserEmail(admin.getEmail());
                    notif.setTitle("Event Starting Soon");
                    notif.setMessage("'" + event.getTitle() + "' is starting soon on " + event.getDate() + ". Don't miss it!");
                    notif.setType("EVENT_STARTING_SOON");
                    notif.setRelatedEventId(event.getId());
                    notif.setRelatedEventTitle(event.getTitle());
                    notif.setCreatedDate(LocalDateTime.now().minusHours(10 + i * 2));
                    notificationRepo.save(notif);
                }
                
                System.out.println("Sample Notifications Generated!");
            }
            
            // 5. Generate 10 sample registrations for each event
            if (registrationRepo.count() == 0) {
                List<Event> allEvents = eventRepo.findAll();
                
                // Sample student names for variety
                String[] studentNames = {
                    "Rajesh Kumar", "Priya Sharma", "Amit Patel", "Sneha Reddy", "Vikram Singh",
                    "Anjali Gupta", "Rahul Verma", "Pooja Desai", "Karan Mehta", "Divya Iyer"
                };
                
                String[] studentEmails = {
                    "rajesh.kumar@student.edu", "priya.sharma@student.edu", "amit.patel@student.edu",
                    "sneha.reddy@student.edu", "vikram.singh@student.edu", "anjali.gupta@student.edu",
                    "rahul.verma@student.edu", "pooja.desai@student.edu", "karan.mehta@student.edu",
                    "divya.iyer@student.edu"
                };
                
                String[] paymentMethods = {"DESK", "ONLINE", "DESK", "ONLINE", "DESK", "ONLINE", "DESK", "ONLINE", "DESK", "ONLINE"};
                String[] statuses = {"COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED"};
                
                int totalRegistrations = 0;
                
                // Create 10 registrations for each event
                for (Event event : allEvents) {
                    for (int i = 0; i < 10; i++) {
                        Registration registration = new Registration();
                        registration.setUserId(admin.getId()); // Using admin ID for simplicity
                        registration.setEventId(event.getId());
                        registration.setUserName(studentNames[i]);
                        registration.setUserEmail(studentEmails[i]);
                        registration.setPaymentMethod(paymentMethods[i]);
                        registration.setRegistrationFee(event.getRegistrationFee());
                        registration.setRegistrationDate(LocalDateTime.now().minusDays(30 - i));
                        registration.setEventRole("Participant");
                        registration.setStatus(statuses[i]);
                        registration.setPaymentStatus(statuses[i]);
                        registration.setTransactionId("REG" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                        registration.setPaymentDate(LocalDateTime.now().minusDays(28 - i));
                        
                        registrationRepo.save(registration);
                        totalRegistrations++;
                    }
                }
                
                System.out.println(totalRegistrations + " Sample Registrations Generated!");
                System.out.println("- 10 registrations per event");
                System.out.println("- Total events: " + allEvents.size());
                System.out.println("- Different student names for variety");
            }
        };
    }
}
