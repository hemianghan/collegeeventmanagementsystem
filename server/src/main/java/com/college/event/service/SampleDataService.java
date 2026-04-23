package com.college.event.service;

import com.college.event.model.Memory;
import com.college.event.model.Event;
import com.college.event.model.Registration;
import com.college.event.repository.MemoryRepository;
import com.college.event.repository.EventRepository;
import com.college.event.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SampleDataService {

    @Autowired
    private MemoryRepository memoryRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private RegistrationRepository registrationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeSampleData() {
        // Check if sample data already exists
        long existingRegistrations = registrationRepository.count();
        
        if (existingRegistrations > 0) {
            System.out.println("✅ Sample data already exists (" + existingRegistrations + " registrations found)");
            System.out.println("   Skipping sample data creation to preserve existing data");
            
            // Add past event registrations for students if they don't exist
            addPastEventRegistrationsForStudents();
            
            // Add sample certificates to existing registrations
            addSampleCertificates();
            return;
        }
        
        System.out.println("📝 No existing data found. Creating sample data...");
        
        // Create sample student registrations (10-12 events)
        createSampleStudentRegistrations();
        
        // Add past event registrations for students
        addPastEventRegistrationsForStudents();
        
        // Add sample certificates to registrations
        addSampleCertificates();
        
        // Only add sample data if no approved memories exist
        long approvedCount = memoryRepository.countByIsApprovedTrue();
        if (approvedCount == 0) {
            createSampleMemories();
        }
    }

    private void createSampleStudentRegistrations() {
        try {
            // Get or create sample events
            List<Event> events = eventRepository.findAll();
            if (events.isEmpty()) {
                createSampleEvents();
                events = eventRepository.findAll();
            }

            // Create sample student names and emails
            String[] studentNames = {
                "Test Student", "Alice Johnson", "Bob Smith", "Carol Davis", "David Wilson",
                "Emma Brown", "Frank Miller", "Grace Lee", "Henry Taylor", "Ivy Chen"
            };
            String[] studentEmails = {
                "student@college.edu", "alice@college.edu", "bob@college.edu", "carol@college.edu", "david@college.edu",
                "emma@college.edu", "frank@college.edu", "grace@college.edu", "henry@college.edu", "ivy@college.edu"
            };

            // STUDENT PANEL: Create 12 registrations for multiple students (for "My Registration")
            
            // Student 1: Test Student
            Long testStudentId = 1001L;
            String testStudentName = "Test Student";
            String testStudentEmail = "student@college.edu";
            
            // Student 2: Hemishaan Ghan
            Long hemishaanId = 1002L;
            String hemishaanName = "Hemishaan Ghan";
            String hemishaanEmail = "hemishaanghan1@gmail.com";

            // Take first 12 events for both students
            int studentRegistrationCount = Math.min(12, events.size());
            
            // Create registrations for Test Student
            for (int i = 0; i < studentRegistrationCount; i++) {
                Event event = events.get(i);
                
                Registration registration = new Registration();
                registration.setUserId(testStudentId);
                registration.setEventId(event.getId());
                registration.setUserName(testStudentName);
                registration.setUserEmail(testStudentEmail);
                registration.setPaymentMethod("DESK");
                registration.setRegistrationFee(event.getRegistrationFee());
                registration.setRegistrationDate(LocalDateTime.now().minusDays(i + 1));
                registration.setEventRole("Participant");
                registration.setStatus("PAID");
                registration.setPaymentStatus("COMPLETED");
                registration.setTransactionId("TXN" + System.currentTimeMillis() + String.format("%03d", i));
                registration.setPaymentDate(LocalDateTime.now().minusDays(i));
                
                registrationRepository.save(registration);
            }
            
            // Create registrations for Hemishaan
            for (int i = 0; i < studentRegistrationCount; i++) {
                Event event = events.get(i);
                
                Registration registration = new Registration();
                registration.setUserId(hemishaanId);
                registration.setEventId(event.getId());
                registration.setUserName(hemishaanName);
                registration.setUserEmail(hemishaanEmail);
                registration.setPaymentMethod("DESK");
                registration.setRegistrationFee(event.getRegistrationFee());
                registration.setRegistrationDate(LocalDateTime.now().minusDays(i + 1));
                registration.setEventRole("Participant");
                registration.setStatus("PAID");
                registration.setPaymentStatus("COMPLETED");
                registration.setTransactionId("TXN" + System.currentTimeMillis() + String.format("%03d", i + 100));
                registration.setPaymentDate(LocalDateTime.now().minusDays(i));
                
                registrationRepository.save(registration);
            }
            
            System.out.println("✅ STUDENT PANEL: Created " + studentRegistrationCount + " registrations for each student:");
            System.out.println("   1. " + testStudentEmail + " (ID: " + testStudentId + ") - " + studentRegistrationCount + " events");
            System.out.println("   2. " + hemishaanEmail + " (ID: " + hemishaanId + ") - " + studentRegistrationCount + " events");

            // ADMIN PANEL: Create 10 registrations per event (for "Student Registration" view)
            for (Event event : events) {
                // Create 10 different students registered for each event
                for (int j = 0; j < 10; j++) {
                    Long studentId = 2000L + j; // Different student IDs (2000-2009)
                    String studentName = studentNames[j % studentNames.length];
                    String studentEmail = studentEmails[j % studentEmails.length];
                    
                    // Skip if this is the test student (already registered above)
                    if (studentId.equals(testStudentId)) {
                        continue;
                    }
                    
                    // Check if this student is already registered for this event
                    List<Registration> existing = registrationRepository.findByUserIdAndEventId(studentId, event.getId());
                    if (!existing.isEmpty()) {
                        continue; // Skip if already registered
                    }
                    
                    Registration registration = new Registration();
                    registration.setUserId(studentId);
                    registration.setEventId(event.getId());
                    registration.setUserName(studentName + " " + (j + 1)); // Make names unique
                    registration.setUserEmail(studentEmail.replace("@", "+" + (j + 1) + "@")); // Make emails unique
                    registration.setPaymentMethod(j % 2 == 0 ? "DESK" : "ONLINE");
                    registration.setRegistrationFee(event.getRegistrationFee());
                    registration.setRegistrationDate(LocalDateTime.now().minusDays(j + 1));
                    registration.setEventRole("Participant");
                    registration.setStatus("PAID");
                    registration.setPaymentStatus(j % 3 == 0 ? "PENDING" : "COMPLETED");
                    registration.setTransactionId("TXN" + System.currentTimeMillis() + String.format("%03d", j) + event.getId());
                    registration.setPaymentDate(LocalDateTime.now().minusDays(j));
                    
                    registrationRepository.save(registration);
                }
            }
            
            System.out.println("✅ ADMIN PANEL: Created 10 registrations per event for admin view");
            System.out.println("   Admins can view 10 student registrations per event in 'Student Registration'");
            
        } catch (Exception e) {
            System.err.println("Error creating sample student registrations: " + e.getMessage());
        }
    }

    private void addPastEventRegistrationsForStudents() {
        try {
            // Get all events
            List<Event> events = eventRepository.findAll();
            if (events.isEmpty()) {
                return;
            }

            // Filter past events (events with dates before today)
            LocalDateTime today = LocalDateTime.now();
            List<Event> pastEvents = events.stream()
                .filter(event -> {
                    try {
                        // Parse event date (assuming format "YYYY-MM-DD")
                        String[] dateParts = event.getDate().split("-");
                        LocalDateTime eventDate = LocalDateTime.of(
                            Integer.parseInt(dateParts[0]), 
                            Integer.parseInt(dateParts[1]), 
                            Integer.parseInt(dateParts[2]), 
                            0, 0
                        );
                        return eventDate.isBefore(today);
                    } catch (Exception e) {
                        return false; // Skip events with invalid dates
                    }
                })
                .collect(java.util.stream.Collectors.toList());

            if (pastEvents.isEmpty()) {
                System.out.println("📅 No past events found to create registrations for");
                return;
            }

            // Student IDs and details
            Long[] studentIds = {1001L, 1002L};
            String[] studentNames = {"Test Student", "Hemishaan Ghan"};
            String[] studentEmails = {"student@college.edu", "hemishaanghan1@gmail.com"};

            int addedRegistrations = 0;

            // Add past event registrations for each student
            for (int i = 0; i < studentIds.length; i++) {
                Long studentId = studentIds[i];
                String studentName = studentNames[i];
                String studentEmail = studentEmails[i];

                // Take up to 8 past events for each student
                int eventsToRegister = Math.min(8, pastEvents.size());
                
                for (int j = 0; j < eventsToRegister; j++) {
                    Event pastEvent = pastEvents.get(j);
                    
                    // Check if student is already registered for this event
                    List<Registration> existingRegistrations = registrationRepository.findByUserIdAndEventId(studentId, pastEvent.getId());
                    if (!existingRegistrations.isEmpty()) {
                        continue; // Skip if already registered
                    }
                    
                    // Create past event registration
                    Registration registration = new Registration();
                    registration.setUserId(studentId);
                    registration.setEventId(pastEvent.getId());
                    registration.setUserName(studentName);
                    registration.setUserEmail(studentEmail);
                    registration.setPaymentMethod("DESK");
                    registration.setRegistrationFee(pastEvent.getRegistrationFee());
                    registration.setRegistrationDate(LocalDateTime.now().minusDays(j + 10)); // Past registration dates
                    registration.setEventRole("Participant");
                    registration.setStatus("PAID");
                    registration.setPaymentStatus("COMPLETED");
                    registration.setTransactionId("PAST" + System.currentTimeMillis() + String.format("%03d", j) + studentId);
                    registration.setPaymentDate(LocalDateTime.now().minusDays(j + 9));
                    
                    registrationRepository.save(registration);
                    addedRegistrations++;
                }
            }

            if (addedRegistrations > 0) {
                System.out.println("✅ PAST EVENTS: Added " + addedRegistrations + " past event registrations for students");
                System.out.println("   Students can now see past events in 'My Events' section");
            } else {
                System.out.println("📅 PAST EVENTS: All students already have past event registrations");
            }

        } catch (Exception e) {
            System.err.println("Error adding past event registrations: " + e.getMessage());
        }
    }

    private void createSampleMemories() {
        List<Event> events = eventRepository.findAll();
        
        // Create sample memories for existing events or create sample events if none exist
        if (events.isEmpty()) {
            createSampleEvents();
            events = eventRepository.findAll();
        }

        // Create 13 sample memories with unique events and realistic event photos
        SampleMemoryData[] sampleData = {
            new SampleMemoryData(
                "Alex Johnson", "alex@college.edu",
                "Amazing workshop! Learned so much about AI and machine learning. The hands-on sessions were incredibly valuable. Great speakers and networking opportunities!",
                "Review", 5, 12,
                createTechWorkshopPhoto()
            ),
            new SampleMemoryData(
                "Sarah Chen", "sarah@college.edu", 
                "What a fantastic celebration of diversity! The food stalls, performances, and cultural displays were absolutely wonderful. Loved the traditional dance performances!",
                "Memory", 5, 18,
                createCulturalFestivalPhoto()
            ),
            new SampleMemoryData(
                "Mike Rodriguez", "mike@college.edu",
                "Great organization and team spirit! The competition was intense and fair. Our basketball team played amazingly. Looking forward to next year's championship!",
                "Feedback", 4, 15,
                createSportsEventPhoto()
            ),
            new SampleMemoryData(
                "Emma Wilson", "emma@college.edu",
                "Excellent networking opportunities! Met representatives from top companies like Google, Microsoft, and Amazon. Got valuable career advice and potential internship leads.",
                "Review", 5, 22,
                createCareerFairPhoto()
            ),
            new SampleMemoryData(
                "David Kim", "david@college.edu",
                "Incredible performances by student bands! The sound quality was perfect and the atmosphere was electric. The jazz ensemble was particularly outstanding!",
                "Memory", 4, 20,
                createMusicConcertPhoto()
            ),
            new SampleMemoryData(
                "Lisa Park", "lisa@college.edu",
                "Very informative session about sustainable practices. The speakers were knowledgeable and engaging. Learned practical tips for reducing campus carbon footprint.",
                "Feedback", 4, 9,
                createSustainabilityPhoto()
            ),
            new SampleMemoryData(
                "James Wilson", "james@college.edu",
                "Outstanding hackathon event! 48 hours of intense coding and innovation. Our team developed an amazing mobile app for campus navigation. Great prizes and mentorship!",
                "Review", 5, 25,
                createHackathonPhoto()
            ),
            // Additional 6 random student feedback
            new SampleMemoryData(
                "Priya Sharma", "priya@college.edu",
                "The photography exhibition was absolutely stunning! So many talented students showcased their work. The nature photography section was my favorite. Inspiring creativity everywhere!",
                "Memory", 5, 14,
                createPhotographyPhoto()
            ),
            new SampleMemoryData(
                "Ryan Thompson", "ryan@college.edu",
                "Debate championship was intense! The topics were thought-provoking and the arguments were well-researched. Great to see students passionate about current issues.",
                "Feedback", 4, 11,
                createDebatePhoto()
            ),
            new SampleMemoryData(
                "Aisha Patel", "aisha@college.edu",
                "Science fair blew my mind! The robotics projects were incredible, especially the AI-powered assistant. Makes me excited about the future of technology!",
                "Review", 5, 19,
                createScienceFairPhoto()
            ),
            new SampleMemoryData(
                "Carlos Martinez", "carlos@college.edu",
                "Art workshop was so relaxing and fun! Learned pottery techniques and painted my first canvas. The instructors were patient and encouraging. Definitely joining art club!",
                "Memory", 4, 8,
                createArtWorkshopPhoto()
            ),
            new SampleMemoryData(
                "Sophie Anderson", "sophie@college.edu",
                "Book reading session was cozy and inspiring! The author shared amazing insights about creative writing. Got my book signed and made new friends who love reading too!",
                "Feedback", 4, 13,
                createBookReadingPhoto()
            ),
            new SampleMemoryData(
                "Arjun Singh", "arjun@college.edu",
                "Gaming tournament was epic! The esports competition was professionally organized. Our team made it to semifinals in League of Legends. Can't wait for next year!",
                "Review", 5, 27,
                createGamingPhoto()
            )
        };

        // Ensure we have enough unique events
        while (events.size() < sampleData.length) {
            createAdditionalSampleEvent(events.size());
            events = eventRepository.findAll();
        }

        for (int i = 0; i < sampleData.length && i < events.size(); i++) {
            Memory memory = new Memory();
            memory.setUserId(9000L + i); // Sample user IDs
            memory.setUserName(sampleData[i].userName);
            memory.setUserEmail(sampleData[i].userEmail);
            memory.setEventId(events.get(i).getId());
            memory.setEventTitle(events.get(i).getTitle());
            memory.setReviewText(sampleData[i].reviewText);
            memory.setRating(sampleData[i].rating);
            memory.setCategory(sampleData[i].category);
            memory.setIsApproved(true); // Pre-approved sample data
            memory.setLikes(sampleData[i].likes);
            memory.setCreatedDate(LocalDateTime.now().minusDays(i + 1));
            memory.setImageUrl(sampleData[i].imageData);
            
            memoryRepository.save(memory);
        }
    }

    private void createSampleEvents() {
        String[] eventTitles = {
            "AI & Machine Learning Workshop",
            "International Cultural Festival 2024", 
            "Inter-College Basketball Championship",
            "Tech Career Fair 2024",
            "Annual Music Concert Night",
            "Campus Sustainability Summit",
            "48-Hour Innovation Hackathon",
            "Student Photography Exhibition",
            "Inter-College Debate Championship",
            "Annual Science Fair 2024",
            "Art & Craft Workshop",
            "Book Reading & Author Meet",
            "Gaming & Esports Tournament"
        };
        String[] eventCategories = {"Technical", "Cultural", "Sports", "Technical", "Cultural", "Fun", "Technical", "Cultural", "Fun", "Technical", "Cultural", "Fun", "Fun"};
        String[] eventDescriptions = {
            "Hands-on workshop covering AI fundamentals, machine learning algorithms, and practical applications in industry",
            "Celebrate global diversity with traditional food, music performances, cultural displays, and international student showcases",
            "Annual inter-college basketball tournament featuring teams from 12 colleges competing for the championship trophy",
            "Meet top tech employers including Google, Microsoft, Amazon, and startups. Explore internships and full-time opportunities",
            "Student band performances featuring jazz, rock, classical, and contemporary music. Open mic sessions included",
            "Learn about environmental sustainability, green campus initiatives, and practical eco-friendly practices for students",
            "48-hour coding competition where teams build innovative solutions to real-world problems. Mentorship and prizes included",
            "Student photography showcase featuring campus life, nature, portraits, and creative compositions from talented photographers",
            "Inter-college debate competition on contemporary social, political, and environmental topics with expert judges",
            "Annual science fair showcasing innovative student research projects, experiments, and technological innovations",
            "Creative workshop for painting, sculpture, pottery, and traditional crafts with expert instructors and materials provided",
            "Meet bestselling authors, participate in book discussions, creative writing workshops, and get books signed",
            "Competitive gaming tournament featuring popular games like League of Legends, CS:GO, and mobile gaming championships"
        };

        for (int i = 0; i < eventTitles.length; i++) {
            Event event = new Event();
            event.setTitle(eventTitles[i]);
            event.setLocation("College Campus - " + getLocationForEvent(i));
            event.setDescription(eventDescriptions[i]);
            event.setCategory(eventCategories[i]);
            
            // Create mix of past and future events
            if (i < 8) {
                // First 8 events are past events (for student past registrations)
                event.setDate("2024-" + String.format("%02d", (i % 8) + 1) + "-15");
                event.setRegistrationDeadline("2024-" + String.format("%02d", (i % 8) + 1) + "-12"); // 3 days before
            } else {
                // Last 5 events are future events
                String eventDate = "2026-" + String.format("%02d", ((i - 8) % 5) + 5) + "-15";
                event.setDate(eventDate);
                
                // Set registration deadlines for future events
                if (i == 8) {
                    // First future event: deadline is tomorrow (urgent)
                    event.setRegistrationDeadline("2026-04-24"); // Tomorrow
                } else if (i == 9) {
                    // Second future event: deadline is in 3 days
                    event.setRegistrationDeadline("2026-04-26"); // 3 days from now
                } else if (i == 10) {
                    // Third future event: deadline is today (last day)
                    event.setRegistrationDeadline("2026-04-23"); // Today
                } else {
                    // Other events: normal deadline (3 days before event)
                    event.setRegistrationDeadline("2026-" + String.format("%02d", ((i - 8) % 5) + 5) + "-12");
                }
            }
            
            event.setRegistrationFee(i == 2 ? 50.0 : (i % 3 == 0 ? 75.0 : 100.0)); // Varied fees
            eventRepository.save(event);
        }
    }

    private void createAdditionalSampleEvent(int index) {
        String[] additionalTitles = {
            "Photography Exhibition", "Debate Championship", "Science Fair", "Art & Craft Workshop"
        };
        String[] additionalCategories = {"Cultural", "Fun", "Technical", "Cultural"};
        String[] additionalDescriptions = {
            "Student photography showcase featuring campus life, nature, and creative compositions",
            "Inter-college debate competition on contemporary social and political topics",
            "Annual science fair showcasing innovative student research projects and experiments",
            "Creative workshop for painting, sculpture, and traditional crafts with expert instructors"
        };
        
        int i = index % additionalTitles.length;
        Event event = new Event();
        event.setTitle(additionalTitles[i]);
        event.setLocation("College Campus - " + getLocationForEvent(index + 7));
        event.setDescription(additionalDescriptions[i]);
        event.setCategory(additionalCategories[i]);
        event.setDate("2024-" + String.format("%02d", ((index + 7) % 12) + 1) + "-20");
        event.setRegistrationFee(75.0);
        eventRepository.save(event);
    }

    private String getLocationForEvent(int index) {
        String[] locations = {
            "Tech Lab Building", "Main Auditorium", "Sports Complex", "Career Center", 
            "Music Hall", "Conference Center", "Innovation Hub", "Art Gallery", 
            "Debate Hall", "Science Building", "Craft Center"
        };
        return locations[index % locations.length];
    }

    private String getColorForCategory(String category) {
        switch (category) {
            case "Review": return "#667eea";
            case "Memory": return "#ff6b94";
            case "Feedback": return "#34d399";
            default: return "#764ba2";
        }
    }

    private String createSampleImageData(String eventTitle, String color) {
        String svg = String.format(
            "<svg width=\"300\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"300\" height=\"200\" fill=\"%s\"/>" +
            "<text x=\"150\" y=\"100\" font-family=\"Arial\" font-size=\"16\" fill=\"white\" text-anchor=\"middle\" dy=\".3em\">%s</text>" +
            "</svg>", 
            color, 
            eventTitle.length() > 20 ? eventTitle.substring(0, 17) + "..." : eventTitle
        );
        
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    // Realistic event photo creators
    private String createTechWorkshopPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<defs><linearGradient id=\"techGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">" +
            "<stop offset=\"0%\" style=\"stop-color:#667eea;stop-opacity:1\" />" +
            "<stop offset=\"100%\" style=\"stop-color:#764ba2;stop-opacity:1\" /></linearGradient></defs>" +
            "<rect width=\"400\" height=\"300\" fill=\"url(#techGrad)\"/>" +
            "<rect x=\"50\" y=\"50\" width=\"300\" height=\"180\" fill=\"#2d3748\" rx=\"10\"/>" +
            "<rect x=\"70\" y=\"70\" width=\"260\" height=\"140\" fill=\"#1a202c\" rx=\"5\"/>" +
            "<text x=\"200\" y=\"90\" font-family=\"monospace\" font-size=\"12\" fill=\"#00ff00\">$ python ai_workshop.py</text>" +
            "<text x=\"200\" y=\"110\" font-family=\"monospace\" font-size=\"12\" fill=\"#00ff00\">Training model...</text>" +
            "<text x=\"200\" y=\"130\" font-family=\"monospace\" font-size=\"12\" fill=\"#00ff00\">Accuracy: 94.2%</text>" +
            "<circle cx=\"100\" cy=\"250\" r=\"15\" fill=\"#ffd700\"/>" +
            "<circle cx=\"150\" cy=\"250\" r=\"15\" fill=\"#ffd700\"/>" +
            "<circle cx=\"200\" cy=\"250\" r=\"15\" fill=\"#ffd700\"/>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">AI Workshop in Progress</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createCulturalFestivalPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#ff6b94\"/>" +
            "<circle cx=\"100\" cy=\"100\" r=\"30\" fill=\"#ffd700\"/>" +
            "<circle cx=\"200\" cy=\"80\" r=\"25\" fill=\"#ff4757\"/>" +
            "<circle cx=\"300\" cy=\"120\" r=\"35\" fill=\"#3742fa\"/>" +
            "<rect x=\"50\" y=\"180\" width=\"80\" height=\"60\" fill=\"#2ed573\" rx=\"10\"/>" +
            "<rect x=\"160\" y=\"180\" width=\"80\" height=\"60\" fill=\"#ffa502\" rx=\"10\"/>" +
            "<rect x=\"270\" y=\"180\" width=\"80\" height=\"60\" fill=\"#ff6348\" rx=\"10\"/>" +
            "<text x=\"90\" y=\"215\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\">FOOD</text>" +
            "<text x=\"200\" y=\"215\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\">MUSIC</text>" +
            "<text x=\"310\" y=\"215\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\">DANCE</text>" +
            "<text x=\"200\" y=\"270\" font-family=\"Arial\" font-size=\"16\" fill=\"white\" text-anchor=\"middle\">Cultural Festival 2024</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createSportsEventPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#34d399\"/>" +
            "<rect x=\"50\" y=\"50\" width=\"300\" height=\"200\" fill=\"#065f46\" rx=\"20\"/>" +
            "<circle cx=\"200\" cy=\"150\" r=\"60\" fill=\"none\" stroke=\"white\" stroke-width=\"3\"/>" +
            "<circle cx=\"200\" cy=\"150\" r=\"8\" fill=\"white\"/>" +
            "<rect x=\"50\" y=\"140\" width=\"20\" height=\"20\" fill=\"white\"/>" +
            "<rect x=\"330\" y=\"140\" width=\"20\" height=\"20\" fill=\"white\"/>" +
            "<circle cx=\"120\" cy=\"120\" r=\"12\" fill=\"#ffd700\"/>" +
            "<circle cx=\"280\" cy=\"180\" r=\"12\" fill=\"#ffd700\"/>" +
            "<text x=\"200\" y=\"40\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">BASKETBALL</text>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Championship Game</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createCareerFairPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#764ba2\"/>" +
            "<rect x=\"50\" y=\"80\" width=\"80\" height=\"60\" fill=\"#4285f4\" rx=\"5\"/>" +
            "<rect x=\"160\" y=\"80\" width=\"80\" height=\"60\" fill=\"#ea4335\" rx=\"5\"/>" +
            "<rect x=\"270\" y=\"80\" width=\"80\" height=\"60\" fill=\"#34a853\" rx=\"5\"/>" +
            "<text x=\"90\" y=\"115\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">GOOGLE</text>" +
            "<text x=\"200\" y=\"115\" font-family=\"Arial\" font-size=\"10\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">MICROSOFT</text>" +
            "<text x=\"310\" y=\"115\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">AMAZON</text>" +
            "<rect x=\"50\" y=\"180\" width=\"80\" height=\"60\" fill=\"#ff9500\" rx=\"5\"/>" +
            "<rect x=\"160\" y=\"180\" width=\"80\" height=\"60\" fill=\"#1da1f2\" rx=\"5\"/>" +
            "<rect x=\"270\" y=\"180\" width=\"80\" height=\"60\" fill=\"#0077b5\" rx=\"5\"/>" +
            "<text x=\"90\" y=\"215\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">APPLE</text>" +
            "<text x=\"200\" y=\"215\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">TWITTER</text>" +
            "<text x=\"310\" y=\"215\" font-family=\"Arial\" font-size=\"10\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">LINKEDIN</text>" +
            "<text x=\"200\" y=\"30\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">CAREER FAIR 2024</text>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Top Tech Companies</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createMusicConcertPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#1a1a2e\"/>" +
            "<rect x=\"100\" y=\"200\" width=\"200\" height=\"80\" fill=\"#16213e\" rx=\"10\"/>" +
            "<rect x=\"110\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"140\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"170\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"200\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"230\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"260\" y=\"210\" width=\"20\" height=\"60\" fill=\"white\"/>" +
            "<rect x=\"120\" y=\"210\" width=\"15\" height=\"40\" fill=\"black\"/>" +
            "<rect x=\"150\" y=\"210\" width=\"15\" height=\"40\" fill=\"black\"/>" +
            "<rect x=\"210\" y=\"210\" width=\"15\" height=\"40\" fill=\"black\"/>" +
            "<rect x=\"240\" y=\"210\" width=\"15\" height=\"40\" fill=\"black\"/>" +
            "<circle cx=\"80\" cy=\"100\" r=\"20\" fill=\"#ffd700\"/>" +
            "<circle cx=\"150\" cy=\"80\" r=\"15\" fill=\"#ff6b94\"/>" +
            "<circle cx=\"250\" cy=\"90\" r=\"18\" fill=\"#4ecdc4\"/>" +
            "<circle cx=\"320\" cy=\"110\" r=\"22\" fill=\"#45b7d1\"/>" +
            "<text x=\"200\" y=\"50\" font-family=\"Arial\" font-size=\"20\" fill=\"#ffd700\" text-anchor=\"middle\" font-weight=\"bold\">♪ MUSIC NIGHT ♪</text>" +
            "<text x=\"200\" y=\"290\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Student Band Performance</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createSustainabilityPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#2d5016\"/>" +
            "<circle cx=\"200\" cy=\"150\" r=\"80\" fill=\"#4ade80\"/>" +
            "<circle cx=\"200\" cy=\"150\" r=\"60\" fill=\"#22c55e\"/>" +
            "<circle cx=\"200\" cy=\"150\" r=\"40\" fill=\"#16a34a\"/>" +
            "<rect x=\"180\" y=\"80\" width=\"40\" height=\"60\" fill=\"#8b5cf6\"/>" +
            "<polygon points=\"180,80 220,80 200,60\" fill=\"#a78bfa\"/>" +
            "<rect x=\"120\" y=\"120\" width=\"30\" height=\"40\" fill=\"#06b6d4\"/>" +
            "<rect x=\"250\" y=\"130\" width=\"30\" height=\"30\" fill=\"#f59e0b\"/>" +
            "<text x=\"200\" y=\"40\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">♻️ SUSTAINABILITY</text>" +
            "<text x=\"200\" y=\"250\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Green Campus Initiative</text>" +
            "<text x=\"200\" y=\"270\" font-family=\"Arial\" font-size=\"12\" fill=\"#4ade80\" text-anchor=\"middle\">Reduce • Reuse • Recycle</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createHackathonPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#0f172a\"/>" +
            "<rect x=\"50\" y=\"50\" width=\"120\" height=\"80\" fill=\"#1e293b\" rx=\"5\"/>" +
            "<rect x=\"230\" y=\"50\" width=\"120\" height=\"80\" fill=\"#1e293b\" rx=\"5\"/>" +
            "<rect x=\"140\" y=\"150\" width=\"120\" height=\"80\" fill=\"#1e293b\" rx=\"5\"/>" +
            "<rect x=\"60\" y=\"60\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<rect x=\"240\" y=\"60\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<rect x=\"150\" y=\"160\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<text x=\"110\" y=\"85\" font-family=\"monospace\" font-size=\"8\" fill=\"#00ff00\">console.log('Hello');</text>" +
            "<text x=\"110\" y=\"95\" font-family=\"monospace\" font-size=\"8\" fill=\"#00ff00\">function hack() {</text>" +
            "<text x=\"290\" y=\"85\" font-family=\"monospace\" font-size=\"8\" fill=\"#ff6b6b\">ERROR: 404</text>" +
            "<text x=\"290\" y=\"95\" font-family=\"monospace\" font-size=\"8\" fill=\"#4ecdc4\">Debugging...</text>" +
            "<text x=\"200\" y=\"185\" font-family=\"monospace\" font-size=\"8\" fill=\"#ffd93d\">npm start</text>" +
            "<text x=\"200\" y=\"195\" font-family=\"monospace\" font-size=\"8\" fill=\"#ffd93d\">Server running...</text>" +
            "<text x=\"200\" y=\"30\" font-family=\"Arial\" font-size=\"18\" fill=\"#ffd93d\" text-anchor=\"middle\" font-weight=\"bold\">48H HACKATHON</text>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Innovation Challenge</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createPhotographyPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#2c3e50\"/>" +
            "<rect x=\"50\" y=\"50\" width=\"300\" height=\"200\" fill=\"#34495e\" rx=\"15\"/>" +
            "<circle cx=\"200\" cy=\"120\" r=\"40\" fill=\"#ecf0f1\" stroke=\"#bdc3c7\" stroke-width=\"3\"/>" +
            "<circle cx=\"200\" cy=\"120\" r=\"25\" fill=\"#3498db\"/>" +
            "<rect x=\"160\" y=\"40\" width=\"80\" height=\"20\" fill=\"#95a5a6\" rx=\"5\"/>" +
            "<rect x=\"320\" y=\"70\" width=\"15\" height=\"15\" fill=\"#e74c3c\" rx=\"3\"/>" +
            "<rect x=\"70\" y=\"180\" width=\"60\" height=\"50\" fill=\"#f39c12\" rx=\"5\"/>" +
            "<rect x=\"150\" y=\"180\" width=\"60\" height=\"50\" fill=\"#e67e22\" rx=\"5\"/>" +
            "<rect x=\"230\" y=\"180\" width=\"60\" height=\"50\" fill=\"#d35400\" rx=\"5\"/>" +
            "<rect x=\"310\" y=\"180\" width=\"60\" height=\"50\" fill=\"#c0392b\" rx=\"5\"/>" +
            "<text x=\"200\" y=\"25\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">📸 PHOTOGRAPHY EXHIBITION</text>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Student Showcase</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createDebatePhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#8e44ad\"/>" +
            "<rect x=\"50\" y=\"100\" width=\"120\" height=\"150\" fill=\"#9b59b6\" rx=\"10\"/>" +
            "<rect x=\"230\" y=\"100\" width=\"120\" height=\"150\" fill=\"#9b59b6\" rx=\"10\"/>" +
            "<rect x=\"170\" y=\"50\" width=\"60\" height=\"80\" fill=\"#e74c3c\" rx=\"5\"/>" +
            "<text x=\"110\" y=\"130\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">TEAM A</text>" +
            "<text x=\"290\" y=\"130\" font-family=\"Arial\" font-size=\"12\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">TEAM B</text>" +
            "<text x=\"200\" y=\"80\" font-family=\"Arial\" font-size=\"10\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">JUDGE</text>" +
            "<circle cx=\"110\" cy=\"160\" r=\"8\" fill=\"#f1c40f\"/>" +
            "<circle cx=\"290\" cy=\"160\" r=\"8\" fill=\"#f1c40f\"/>" +
            "<circle cx=\"200\" cy=\"90\" r=\"6\" fill=\"#e67e22\"/>" +
            "<text x=\"200\" y=\"25\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">🎤 DEBATE CHAMPIONSHIP</text>" +
            "<text x=\"200\" y=\"280\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Inter-College Competition</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createScienceFairPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#2980b9\"/>" +
            "<rect x=\"50\" y=\"80\" width=\"80\" height=\"120\" fill=\"#3498db\" rx=\"8\"/>" +
            "<rect x=\"160\" y=\"80\" width=\"80\" height=\"120\" fill=\"#3498db\" rx=\"8\"/>" +
            "<rect x=\"270\" y=\"80\" width=\"80\" height=\"120\" fill=\"#3498db\" rx=\"8\"/>" +
            "<circle cx=\"90\" cy=\"120\" r=\"15\" fill=\"#e74c3c\"/>" +
            "<rect x=\"180\" y=\"110\" width=\"40\" height=\"20\" fill=\"#f39c12\" rx=\"3\"/>" +
            "<polygon points=\"290,110 310,110 300,130\" fill=\"#2ecc71\"/>" +
            "<text x=\"90\" y=\"160\" font-family=\"Arial\" font-size=\"8\" fill=\"white\" text-anchor=\"middle\">ROBOT</text>" +
            "<text x=\"200\" y=\"160\" font-family=\"Arial\" font-size=\"8\" fill=\"white\" text-anchor=\"middle\">SOLAR</text>" +
            "<text x=\"310\" y=\"160\" font-family=\"Arial\" font-size=\"8\" fill=\"white\" text-anchor=\"middle\">CHEM</text>" +
            "<text x=\"200\" y=\"40\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">🔬 SCIENCE FAIR</text>" +
            "<text x=\"200\" y=\"250\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Innovation & Research</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createArtWorkshopPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#e67e22\"/>" +
            "<rect x=\"80\" y=\"80\" width=\"240\" height=\"140\" fill=\"#f39c12\" rx=\"10\"/>" +
            "<circle cx=\"150\" cy=\"130\" r=\"20\" fill=\"#e74c3c\"/>" +
            "<circle cx=\"200\" cy=\"130\" r=\"20\" fill=\"#3498db\"/>" +
            "<circle cx=\"250\" cy=\"130\" r=\"20\" fill=\"#2ecc71\"/>" +
            "<rect x=\"120\" y=\"160\" width=\"160\" height=\"40\" fill=\"#95a5a6\" rx=\"5\"/>" +
            "<rect x=\"130\" y=\"170\" width=\"20\" height=\"20\" fill=\"#8e44ad\"/>" +
            "<rect x=\"160\" y=\"170\" width=\"20\" height=\"20\" fill=\"#f1c40f\"/>" +
            "<rect x=\"190\" y=\"170\" width=\"20\" height=\"20\" fill=\"#e91e63\"/>" +
            "<rect x=\"220\" y=\"170\" width=\"20\" height=\"20\" fill=\"#00bcd4\"/>" +
            "<rect x=\"250\" y=\"170\" width=\"20\" height=\"20\" fill=\"#4caf50\"/>" +
            "<text x=\"200\" y=\"40\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">🎨 ART WORKSHOP</text>" +
            "<text x=\"200\" y=\"260\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Creative Expression</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createBookReadingPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#8b4513\"/>" +
            "<rect x=\"100\" y=\"80\" width=\"200\" height=\"140\" fill=\"#deb887\" rx=\"10\"/>" +
            "<rect x=\"120\" y=\"100\" width=\"160\" height=\"100\" fill=\"#f5f5dc\" rx=\"5\"/>" +
            "<line x1=\"140\" y1=\"120\" x2=\"260\" y2=\"120\" stroke=\"#333\" stroke-width=\"2\"/>" +
            "<line x1=\"140\" y1=\"140\" x2=\"260\" y2=\"140\" stroke=\"#333\" stroke-width=\"2\"/>" +
            "<line x1=\"140\" y1=\"160\" x2=\"260\" y2=\"160\" stroke=\"#333\" stroke-width=\"2\"/>" +
            "<line x1=\"140\" y1=\"180\" x2=\"260\" y2=\"180\" stroke=\"#333\" stroke-width=\"2\"/>" +
            "<rect x=\"50\" y=\"120\" width=\"30\" height=\"40\" fill=\"#cd853f\" rx=\"3\"/>" +
            "<rect x=\"320\" y=\"120\" width=\"30\" height=\"40\" fill=\"#cd853f\" rx=\"3\"/>" +
            "<rect x=\"60\" y=\"180\" width=\"30\" height=\"40\" fill=\"#d2691e\" rx=\"3\"/>" +
            "<rect x=\"310\" y=\"180\" width=\"30\" height=\"40\" fill=\"#d2691e\" rx=\"3\"/>" +
            "<text x=\"200\" y=\"40\" font-family=\"Arial\" font-size=\"18\" fill=\"white\" text-anchor=\"middle\" font-weight=\"bold\">📚 BOOK READING</text>" +
            "<text x=\"200\" y=\"260\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Author Meet & Greet</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    private String createGamingPhoto() {
        String svg = "<svg width=\"400\" height=\"300\" xmlns=\"http://www.w3.org/2000/svg\">" +
            "<rect width=\"400\" height=\"300\" fill=\"#1a1a2e\"/>" +
            "<rect x=\"50\" y=\"60\" width=\"120\" height=\"80\" fill=\"#16213e\" rx=\"8\"/>" +
            "<rect x=\"230\" y=\"60\" width=\"120\" height=\"80\" fill=\"#16213e\" rx=\"8\"/>" +
            "<rect x=\"140\" y=\"160\" width=\"120\" height=\"80\" fill=\"#16213e\" rx=\"8\"/>" +
            "<rect x=\"60\" y=\"70\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<rect x=\"240\" y=\"70\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<rect x=\"150\" y=\"170\" width=\"100\" height=\"60\" fill=\"#000\" rx=\"3\"/>" +
            "<text x=\"110\" y=\"95\" font-family=\"monospace\" font-size=\"8\" fill=\"#00ff41\">PLAYER 1</text>" +
            "<text x=\"110\" y=\"105\" font-family=\"monospace\" font-size=\"8\" fill=\"#00ff41\">SCORE: 2450</text>" +
            "<text x=\"290\" y=\"95\" font-family=\"monospace\" font-size=\"8\" fill=\"#ff073a\">PLAYER 2</text>" +
            "<text x=\"290\" y=\"105\" font-family=\"monospace\" font-size=\"8\" fill=\"#ff073a\">SCORE: 2380</text>" +
            "<text x=\"200\" y=\"195\" font-family=\"monospace\" font-size=\"8\" fill=\"#ffd700\">FINAL MATCH</text>" +
            "<text x=\"200\" y=\"205\" font-family=\"monospace\" font-size=\"8\" fill=\"#ffd700\">LIVE NOW</text>" +
            "<text x=\"200\" y=\"30\" font-family=\"Arial\" font-size=\"18\" fill=\"#ffd700\" text-anchor=\"middle\" font-weight=\"bold\">🎮 GAMING TOURNAMENT</text>" +
            "<text x=\"200\" y=\"270\" font-family=\"Arial\" font-size=\"14\" fill=\"white\" text-anchor=\"middle\">Esports Championship</text>" +
            "</svg>";
        return "data:image/svg+xml;base64," + java.util.Base64.getEncoder().encodeToString(svg.getBytes());
    }

    // Helper class for sample memory data
    private static class SampleMemoryData {
        String userName;
        String userEmail;
        String reviewText;
        String category;
        int rating;
        int likes;
        String imageData;

        SampleMemoryData(String userName, String userEmail, String reviewText, String category, int rating, int likes, String imageData) {
            this.userName = userName;
            this.userEmail = userEmail;
            this.reviewText = reviewText;
            this.category = category;
            this.rating = rating;
            this.likes = likes;
            this.imageData = imageData;
        }
    }

    /**
     * Add sample certificates to existing registrations
     * This method assigns certificate IDs to some registrations to simulate completed events
     */
    private void addSampleCertificates() {
        try {
            // Get all registrations for our test students
            List<Registration> testStudentRegistrations = registrationRepository.findByUserId(1001L);
            List<Registration> hemishaanRegistrations = registrationRepository.findByUserId(1002L);
            
            int certificatesAdded = 0;
            
            // Add certificates to some of Test Student's registrations (about 60% of them)
            for (int i = 0; i < testStudentRegistrations.size(); i++) {
                Registration registration = testStudentRegistrations.get(i);
                
                // Only add certificate if it doesn't already exist and for about 60% of registrations
                if ((registration.getCertificateId() == null || registration.getCertificateId().isEmpty()) && i % 5 != 0) {
                    String certificateId = "CERT-" + registration.getUserId() + "-" + registration.getEventId() + "-" + System.currentTimeMillis();
                    registration.setCertificateId(certificateId);
                    registrationRepository.save(registration);
                    certificatesAdded++;
                }
            }
            
            // Add certificates to some of Hemishaan's registrations (about 70% of them)
            for (int i = 0; i < hemishaanRegistrations.size(); i++) {
                Registration registration = hemishaanRegistrations.get(i);
                
                // Only add certificate if it doesn't already exist and for about 70% of registrations
                if ((registration.getCertificateId() == null || registration.getCertificateId().isEmpty()) && i % 3 != 0) {
                    String certificateId = "CERT-" + registration.getUserId() + "-" + registration.getEventId() + "-" + System.currentTimeMillis();
                    registration.setCertificateId(certificateId);
                    registrationRepository.save(registration);
                    certificatesAdded++;
                }
            }
            
            if (certificatesAdded > 0) {
                System.out.println("✅ CERTIFICATES: Added " + certificatesAdded + " sample certificates to student registrations");
                System.out.println("   Students can now see their certificate count in the Profile section");
            } else {
                System.out.println("📜 CERTIFICATES: Sample certificates already exist for students");
            }
            
        } catch (Exception e) {
            System.err.println("Error adding sample certificates: " + e.getMessage());
        }
    }
}