# Requirements Document

## Introduction

This feature adds a student-focused rating board that displays event ratings only for events the logged-in student has registered for. The system creates sample ratings for registered events (10-12 events per student) to demonstrate the rating functionality with realistic, personalized content. Each sample rating includes authentic event photos from different event types and shows how students can rate and review events they have attended.

## Glossary

- **Student_Rating_Board**: A personalized display component that shows approved event ratings/memories only for events the logged-in student has registered for
- **Registered_Event**: An event that the current student has registered for through the registration system
- **Sample_Rating**: Pre-populated demonstration data representing ratings for the student's registered events, including photo, review text, star rating (1-5), and event association
- **Event_Photo**: An image captured during an event showing activities such as group activities, performances, workshops, or other event moments
- **Memory_System**: The existing system component that manages student ratings, reviews, and event photos with admin approval workflow
- **Seed_Data_Service**: A service component that populates the database with sample ratings for registered events during system initialization
- **Registration_System**: The existing system that manages student event registrations with Registration model and RegistrationController

## Requirements

### Requirement 1: Student Registration-Based Sample Rating Creation

**User Story:** As a student, I want to see sample ratings for events I have registered for, so that I can understand how to rate and review events I attend.

#### Acceptance Criteria

1. WHEN a student logs into the system, THE Seed_Data_Service SHALL identify all events the student has registered for through the Registration_System
2. THE Seed_Data_Service SHALL create sample ratings only for events where the student has an active registration
3. FOR ALL registered events of the logged-in student, THE Seed_Data_Service SHALL create sample ratings representing different student perspectives
4. THE Seed_Data_Service SHALL create sample ratings for 6-10 of the student's registered events (subset of their 10-12 total registrations)
5. THE Seed_Data_Service SHALL assign star ratings between 1 and 5 stars to each sample rating
6. THE Seed_Data_Service SHALL include descriptive review text for each sample rating
7. THE Seed_Data_Service SHALL mark all sample ratings as approved for immediate display
8. WHEN creating sample ratings, THE Seed_Data_Service SHALL assign realistic student names and email addresses to each rating (not the logged-in student's details)

### Requirement 2: Registered Event Photo Management

**User Story:** As a student, I want to see sample photos from events I registered for, so that I can see realistic examples of event memories from my own event portfolio.

#### Acceptance Criteria

1. THE Seed_Data_Service SHALL include event photos that match the categories of events the student has registered for
2. WHEN the student has registered for group activity events, THE Seed_Data_Service SHALL include at least one photo showing a group activity
3. WHEN the student has registered for performance or cultural events, THE Seed_Data_Service SHALL include at least one photo showing a performance or cultural event
4. WHEN the student has registered for workshop or educational events, THE Seed_Data_Service SHALL include at least one photo showing a workshop or educational event
5. WHEN storing event photos, THE Memory_System SHALL store photos in a format compatible with the existing imageUrl field (Base64 or file path)
6. FOR ALL event photos, THE Seed_Data_Service SHALL ensure photos are appropriate for a college event management system
7. THE Seed_Data_Service SHALL match photo content to the specific event categories the student has registered for

### Requirement 3: Student-Specific Rating Board Display

**User Story:** As a student, I want to view sample ratings only for events I have registered for, so that I see personalized and relevant event feedback examples.

#### Acceptance Criteria

1. WHEN a student accesses the rating board, THE Student_Rating_Board SHALL display only approved sample ratings for events the student has registered for
2. THE Student_Rating_Board SHALL retrieve the student's registered events using the existing /api/registrations/user/{userId}/detailed endpoint
3. THE Student_Rating_Board SHALL filter displayed ratings to show only ratings associated with the student's registered event IDs
4. THE Student_Rating_Board SHALL display each sample rating with its associated event photo
5. THE Student_Rating_Board SHALL display the star rating (1-5 stars) for each sample rating
6. THE Student_Rating_Board SHALL display the review text for each sample rating
7. THE Student_Rating_Board SHALL display the event title from the student's registered events
8. THE Student_Rating_Board SHALL display the sample student name for each rating (not the logged-in student's name)
9. WHEN displaying sample ratings, THE Student_Rating_Board SHALL order ratings by creation date with newest first

### Requirement 4: Registration-Based Sample Data Creation

**User Story:** As a student, I want sample ratings to be created for my specific registered events, so that the demonstration data is relevant to my event portfolio.

#### Acceptance Criteria

1. THE Seed_Data_Service SHALL query the Registration_System to get all events the student has registered for
2. THE Seed_Data_Service SHALL use the existing Registration model and RegistrationRepository to identify registered events
3. THE Seed_Data_Service SHALL create sample ratings only for event IDs found in the student's registration records
4. WHEN the student has fewer than 6 registered events, THE Seed_Data_Service SHALL create sample ratings for all registered events
5. WHEN the student has more than 10 registered events, THE Seed_Data_Service SHALL create sample ratings for a representative subset of 6-10 events
6. THE Seed_Data_Service SHALL ensure sample ratings use the actual event titles and details from the student's registered events
7. THE Seed_Data_Service SHALL verify that each sample rating references a valid event ID from the student's registrations

### Requirement 5: Student-Specific Sample Data Management

**User Story:** As a system administrator, I want to manage sample ratings per student based on their registrations, so that each student sees personalized demonstration data.

#### Acceptance Criteria

1. THE Memory_System SHALL store a flag indicating whether a rating is sample data or real user data
2. THE Memory_System SHALL associate sample ratings with the specific student user ID for whom they were created
3. WHERE an admin interface exists, THE Memory_System SHALL allow filtering sample ratings by student and by registered events
4. THE Memory_System SHALL allow deletion of sample ratings for a specific student without affecting other students' sample data
5. WHEN displaying ratings to students, THE Student_Rating_Board SHALL NOT visually distinguish sample ratings from real ratings
6. THE Memory_System SHALL track the creation source (seed data vs user submission) and target student ID for each rating
7. THE Seed_Data_Service SHALL create separate sample rating sets for different students based on their individual registrations

### Requirement 6: Integration with Existing Registration and Memory Systems

**User Story:** As a developer, I want sample ratings to integrate with both the Registration and Memory systems, so that no database schema changes are required and the feature leverages existing functionality.

#### Acceptance Criteria

1. THE Seed_Data_Service SHALL use the existing Memory entity to store sample ratings
2. THE Seed_Data_Service SHALL use the existing RegistrationRepository to query student registrations via findByUserId method
3. THE Seed_Data_Service SHALL populate all required Memory fields (userId, eventId, userName, userEmail, eventTitle, reviewText, imageUrl, rating, category)
4. THE Seed_Data_Service SHALL set isApproved to true for all sample ratings
5. THE Seed_Data_Service SHALL set category to "Memory" for all sample ratings
6. THE Seed_Data_Service SHALL use the existing MemoryRepository to persist sample ratings
7. WHEN the system starts, THE Seed_Data_Service SHALL check if sample data already exists for the student before creating new sample ratings
8. IF sample ratings already exist for the student's registered events, THEN THE Seed_Data_Service SHALL skip sample data creation for that student
9. THE Student_Rating_Board SHALL use the existing /api/registrations/user/{userId}/detailed endpoint to get registered events

### Requirement 7: Registered Event-Specific Sample Rating Content Quality

**User Story:** As a student, I want sample ratings to contain realistic and helpful content relevant to the events I registered for, so that I understand how to write meaningful reviews for my own event experiences.

#### Acceptance Criteria

1. THE Seed_Data_Service SHALL create review text between 50 and 300 characters for each sample rating
2. THE Seed_Data_Service SHALL include specific details about the event experience that match the registered event's category and type
3. THE Seed_Data_Service SHALL vary the tone and style of review text across sample ratings (enthusiastic, constructive, descriptive)
4. THE Seed_Data_Service SHALL ensure review text matches the star rating (positive text for 4-5 stars, constructive text for 2-3 stars)
5. THE Seed_Data_Service SHALL include at least one sample rating with 5 stars, one with 4 stars, and one with 3 stars across the student's registered events
6. THE Seed_Data_Service SHALL avoid generic or template-like review text
7. THE Seed_Data_Service SHALL tailor review content to reflect the specific event type (technical workshop, cultural performance, sports event, etc.) from the student's registrations
8. WHEN creating sample ratings, THE Seed_Data_Service SHALL use the actual event titles from the student's Registration records in the review text
