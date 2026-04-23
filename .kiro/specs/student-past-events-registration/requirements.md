# Requirements Document

## Introduction

This feature enables students to view past events in the student panel and manage their event registrations. Students can register for events they are interested in and cancel their registrations at any time. The system displays 12 past events in the browse events section, with dynamic button states that reflect the student's current registration status. All registration changes persist in the backend H2 database and are reflected in the "My Events" section.

## Glossary

- **Student_Panel**: The web interface accessible to students for browsing and managing event registrations
- **Event_Display**: The component that renders the list of 12 past events in the browse events section
- **Registration_Button**: The interactive button that allows students to register for or cancel registration from an event
- **My_Events_Counter**: The display element showing the count of events a student is registered for
- **Backend_API**: The REST API endpoints for registration and cancellation operations
- **Database**: The H2 database that persists registration data using JPA/Hibernate
- **Registration_State**: The current status of a student's registration for a specific event (registered or not registered)

## Requirements

### Requirement 1: Display Past Events

**User Story:** As a student, I want to see 12 past events in the browse events section, so that I can discover events I might want to register for.

#### Acceptance Criteria

1. THE Event_Display SHALL render exactly 12 past events in the browse events section
2. WHEN the Student_Panel loads, THE Event_Display SHALL fetch past events from the Backend_API
3. THE Event_Display SHALL display event details including title, date, description, and registration status
4. WHEN no past events exist, THE Event_Display SHALL show an appropriate message indicating no events are available

### Requirement 2: Register for Events

**User Story:** As a student, I want to register for an event by clicking a "Register" button, so that I can participate in events I'm interested in.

#### Acceptance Criteria

1. WHEN a student is not registered for an event, THE Registration_Button SHALL display "Register"
2. WHEN a student clicks the "Register" button, THE Backend_API SHALL create a registration record in the Database
3. WHEN registration is successful, THE Registration_Button SHALL change its text to "Cancel Registration"
4. WHEN registration is successful, THE My_Events_Counter SHALL increment by 1
5. IF registration fails, THEN THE Student_Panel SHALL display an error message to the student
6. THE Backend_API SHALL validate that a student cannot register for the same event twice

### Requirement 3: Cancel Event Registration

**User Story:** As a student, I want to cancel my event registration by clicking "Cancel Registration", so that I can withdraw from events I no longer wish to attend.

#### Acceptance Criteria

1. WHEN a student is registered for an event, THE Registration_Button SHALL display "Cancel Registration"
2. WHEN a student clicks the "Cancel Registration" button, THE Backend_API SHALL delete the registration record from the Database
3. WHEN cancellation is successful, THE Registration_Button SHALL change its text to "Register"
4. WHEN cancellation is successful, THE My_Events_Counter SHALL decrement by 1
5. IF cancellation fails, THEN THE Student_Panel SHALL display an error message to the student

### Requirement 4: Dynamic Button State Management

**User Story:** As a student, I want the registration button to reflect my current registration status, so that I always know whether I'm registered for an event.

#### Acceptance Criteria

1. WHEN the Student_Panel loads, THE Registration_Button SHALL display the correct state based on the student's Registration_State
2. WHEN a registration operation completes, THE Registration_Button SHALL update its state without requiring a page reload
3. WHEN a cancellation operation completes, THE Registration_Button SHALL update its state without requiring a page reload
4. THE Registration_Button SHALL be disabled during API operations to prevent duplicate requests
5. WHEN an API operation completes, THE Registration_Button SHALL be re-enabled

### Requirement 5: Persist Registration Data

**User Story:** As a student, I want my registration changes to be saved permanently, so that my registrations are preserved across sessions.

#### Acceptance Criteria

1. WHEN a student registers for an event, THE Backend_API SHALL persist the registration to the Database using JPA/Hibernate
2. WHEN a student cancels a registration, THE Backend_API SHALL remove the registration from the Database
3. WHEN the Student_Panel reloads, THE Event_Display SHALL reflect the current Registration_State from the Database
4. THE Database SHALL maintain referential integrity between student IDs and event IDs
5. THE Backend_API SHALL use the existing registration endpoints at `/api/registrations/register` and `/api/registrations/cancel/{userId}/{eventId}`

### Requirement 6: Update My Events Counter

**User Story:** As a student, I want to see an updated count of my registered events, so that I know how many events I'm currently registered for.

#### Acceptance Criteria

1. WHEN a student registers for an event, THE My_Events_Counter SHALL increment immediately
2. WHEN a student cancels a registration, THE My_Events_Counter SHALL decrement immediately
3. WHEN the Student_Panel loads, THE My_Events_Counter SHALL display the accurate count from the Database
4. THE My_Events_Counter SHALL update without requiring a page reload

### Requirement 7: Handle Student Authentication

**User Story:** As a student, I want the system to use my authenticated student ID, so that registrations are associated with my account.

#### Acceptance Criteria

1. THE Student_Panel SHALL retrieve the authenticated student's user ID from the session or authentication context
2. WHEN making registration requests, THE Backend_API SHALL use the authenticated student's user ID
3. WHEN making cancellation requests, THE Backend_API SHALL use the authenticated student's user ID
4. THE Backend_API SHALL validate that students can only manage their own registrations

### Requirement 8: Provide User Feedback

**User Story:** As a student, I want to receive clear feedback on my registration actions, so that I know whether my actions succeeded or failed.

#### Acceptance Criteria

1. WHEN a registration succeeds, THE Student_Panel SHALL display a success message
2. WHEN a registration fails, THE Student_Panel SHALL display an error message with the failure reason
3. WHEN a cancellation succeeds, THE Student_Panel SHALL display a success message
4. WHEN a cancellation fails, THE Student_Panel SHALL display an error message with the failure reason
5. THE Student_Panel SHALL clear feedback messages after 5 seconds or when the user performs another action
