# Implementation Plan: Admin Notification System

## Overview

This implementation plan breaks down the Admin Notification System into discrete, sequential tasks. The system enables administrators to create and send notifications to students through web notifications and email. The implementation follows a bottom-up approach: database schema changes, backend services, API endpoints, frontend UI, and testing.

## Tasks

- [x] 1. Extend Notification model with admin notification fields
  - Add new fields to Notification entity: `createdBy`, `deliveryMethod`, `targetAudience`, `targetEventId`
  - Add database indexes for `createdBy`, `targetAudience`, and `targetEventId`
  - Update Notification constructor to initialize new fields
  - _Requirements: 3.2, 6.2, 6.3_

- [ ]* 1.1 Write property test for notification data persistence
  - **Property 1: Notification Data Persistence Round-Trip**
  - **Validates: Requirements 3.2, 3.4, 7.5, 8.1**

- [ ]* 1.2 Write property test for default field values
  - **Property 4: Default Field Values on Creation**
  - **Validates: Requirements 3.3**

- [ ]* 1.3 Write property test for null event association storage
  - **Property 12: Null Event Association Storage**
  - **Validates: Requirements 8.5**

- [ ] 2. Create DTOs for admin notification requests and responses
  - Create `NotificationRequest` DTO with validation annotations
  - Create `NotificationResponse` DTO with success/error fields
  - Create `NotificationHistoryDTO` DTO for history view
  - Create `RecipientCountResponse` DTO for recipient preview
  - _Requirements: 1.2, 1.3, 5.6, 6.3_

- [ ]* 2.1 Write property tests for validation constraints
  - **Property 21: Title Length Validation**
  - **Property 22: Message Length Validation**
  - **Property 23: Student Selection Count Validation**
  - **Property 27: Notification Type Acceptance**
  - **Validates: Requirements 1.2, 1.3, 2.6, 7.1**

- [ ] 3. Implement EmailService for SMTP email delivery
  - Create `EmailService` class with `JavaMailSender` autowired
  - Implement `sendEmailAsync()` method with `@Async` annotation
  - Implement `createEmailMessage()` method for MimeMessage composition
  - Implement `formatEmailBody()` method with HTML template
  - Add error handling with logging for email failures
  - Configure async thread pool executor (max 10 concurrent threads)
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.6, 4.7, 4.8, 9.1, 9.2, 9.3, 9.4_

- [ ]* 3.1 Write property test for email subject matching title
  - **Property 8: Email Subject Matches Title**
  - **Validates: Requirements 4.3**

- [ ]* 3.2 Write property test for email body containing message
  - **Property 9: Email Body Contains Message**
  - **Validates: Requirements 4.4**

- [ ]* 3.3 Write property test for email HTML formatting
  - **Property 10: Email HTML Formatting**
  - **Validates: Requirements 4.6**

- [ ]* 3.4 Write property test for email sender configuration
  - **Property 11: Email Sender Configuration**
  - **Validates: Requirements 9.3, 9.4**

- [ ]* 3.5 Write property test for email composition with event association
  - **Property 7: Email Composition with Event Association**
  - **Validates: Requirements 4.5, 8.4**

- [ ]* 3.6 Write property test for email address extraction
  - **Property 29: Email Address Extraction**
  - **Validates: Requirements 4.2**

- [ ]* 3.7 Write property test for email notification type label
  - **Property 28: Email Notification Type Label**
  - **Validates: Requirements 7.6**

- [ ]* 3.8 Write property test for email error resilience
  - **Property 24: Email Error Resilience**
  - **Validates: Requirements 4.8**

- [ ]* 3.9 Write property test for email error logging
  - **Property 25: Email Error Logging**
  - **Validates: Requirements 4.7**

- [ ] 4. Configure async execution for email service
  - Create `AsyncConfig` class with `@EnableAsync` annotation
  - Configure `ThreadPoolTaskExecutor` with max 10 threads
  - Set thread name prefix to "email-async-"
  - _Requirements: 12.4, 12.5, 12.6_

- [ ]* 4.1 Write property test for async email response
  - **Property 18: Async Email Response**
  - **Validates: Requirements 12.4, 12.5**

- [ ] 5. Implement AdminNotificationService for core business logic
  - Create `AdminNotificationService` class
  - Implement `resolveRecipients()` method for all three target audience types
  - Implement `createWebNotifications()` method for bulk notification creation
  - Implement `sendEmailNotificationsAsync()` method to queue email tasks
  - Implement batch processing logic for large recipient lists (batches of 100)
  - Add logging for batch processing start/end times
  - _Requirements: 2.2, 2.4, 3.1, 5.2, 12.1, 12.2, 12.3, 12.7_

- [ ]* 5.1 Write property test for all students recipient resolution
  - **Property 5: All Students Recipient Resolution**
  - **Validates: Requirements 2.2**

- [ ]* 5.2 Write property test for event participants recipient resolution
  - **Property 6: Event Participants Recipient Resolution**
  - **Validates: Requirements 2.4**

- [ ]* 5.3 Write property test for recipient count matching notification count
  - **Property 2: Recipient Count Matches Notification Count**
  - **Validates: Requirements 3.1, 5.2**

- [ ]* 5.4 Write property test for dual delivery method
  - **Property 26: Dual Delivery Method**
  - **Validates: Requirements 5.3**

- [ ]* 5.5 Write property test for batch processing
  - **Property 19: Batch Processing for Large Recipient Lists**
  - **Validates: Requirements 12.2**

- [ ]* 5.6 Write property test for bulk operation logging
  - **Property 20: Bulk Operation Logging**
  - **Validates: Requirements 12.7**

- [ ]* 5.7 Write property test for validation before send
  - **Property 30: Validation Before Send**
  - **Validates: Requirements 5.1**

- [ ] 6. Implement notification history service methods
  - Add `getNotificationHistory()` method to AdminNotificationService
  - Implement pagination with Spring Data Page
  - Implement filtering by notification type
  - Implement filtering by date range
  - Create query methods in NotificationRepository for history retrieval
  - _Requirements: 6.2, 6.4, 6.5, 6.7, 6.8_

- [ ]* 6.1 Write property test for notification sorting by timestamp
  - **Property 3: Notification Sorting by Timestamp**
  - **Validates: Requirements 3.6, 6.4**

- [ ]* 6.2 Write property test for notification history field completeness
  - **Property 13: Notification History Field Completeness**
  - **Validates: Requirements 6.3**

- [ ]* 6.3 Write property test for notification history pagination
  - **Property 14: Notification History Pagination**
  - **Validates: Requirements 6.5**

- [ ]* 6.4 Write property test for history filtering by type
  - **Property 15: History Filtering by Type**
  - **Validates: Requirements 6.7**

- [ ]* 6.5 Write property test for history filtering by date range
  - **Property 16: History Filtering by Date Range**
  - **Validates: Requirements 6.8**

- [ ] 7. Create AdminNotificationController with REST endpoints
  - Create `AdminNotificationController` class with `@RestController` annotation
  - Implement `POST /api/admin/notifications/send` endpoint
  - Implement `GET /api/admin/notifications/history` endpoint
  - Implement `GET /api/admin/notifications/recipient-count` endpoint
  - Add request validation with `@Valid` annotation
  - Add error handling with try-catch blocks
  - Return appropriate HTTP status codes (200, 400, 403, 500)
  - _Requirements: 1.1, 2.7, 5.1, 5.5, 5.6, 6.1_

- [ ]* 7.1 Write unit tests for AdminNotificationController endpoints
  - Test successful notification creation
  - Test validation errors
  - Test notification history retrieval
  - Test recipient count preview

- [ ] 8. Implement admin authorization middleware
  - Create `AdminAuthorizationInterceptor` implementing `HandlerInterceptor`
  - Implement `preHandle()` method to verify admin role
  - Extract user from Authorization header
  - Return 401 Unauthorized if no token provided
  - Return 403 Forbidden if user is not admin
  - Create `WebMvcConfig` to register interceptor for `/api/admin/**` paths
  - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5_

- [ ]* 8.1 Write property test for admin authorization check
  - **Property 17: Admin Authorization Check**
  - **Validates: Requirements 11.1, 11.4**

- [ ]* 8.2 Write unit tests for authorization scenarios
  - Test admin user access granted
  - Test student user access denied (403)
  - Test unauthenticated user access denied (401)

- [ ] 9. Checkpoint - Ensure all backend tests pass
  - Run all unit tests and property-based tests
  - Verify all endpoints are accessible with proper authorization
  - Test email service with test SMTP server
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Create admin sidebar navigation for notification features
  - Add "Send Notifications" link to admin sidebar in `index.html`
  - Add "Notification History" link to admin sidebar
  - Add icons using Bootstrap Icons (bi-bell, bi-clock-history)
  - Add click event handlers to open modal and navigate to history
  - _Requirements: 1.1, 6.1_

- [ ] 11. Create notification creation modal UI
  - Create modal HTML structure with Bootstrap 5 components
  - Add title input field with character counter (max 200)
  - Add message textarea with character counter (max 2000)
  - Add notification type dropdown (announcement, reminder, result)
  - Add delivery method checkboxes (web, email)
  - Add target audience radio buttons (all students, event participants, individual students)
  - Add conditional event selector (shown when "event participants" selected)
  - Add conditional student multi-select (shown when "individual students" selected)
  - Add optional event association selector
  - Add recipient count display
  - Add send button (initially disabled)
  - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.6, 2.1, 2.7_

- [ ] 12. Implement frontend validation for notification form
  - Add real-time validation for title length (1-200 characters)
  - Add real-time validation for message length (1-2000 characters)
  - Add validation for required fields (type, delivery method, target audience)
  - Add conditional validation for event selection
  - Add conditional validation for student selection (1-100 students)
  - Display inline error messages for each field
  - Enable/disable send button based on validation state
  - _Requirements: 1.7, 1.8, 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8, 10.9_

- [ ] 13. Implement target audience selector with dynamic behavior
  - Add event handlers for target audience radio button changes
  - Fetch events from `/api/events` when "event participants" selected
  - Populate event dropdown with fetched events
  - Fetch students from `/api/users?role=Student` when "individual students" selected
  - Populate student multi-select with fetched students
  - Implement searchable student multi-select
  - Call `/api/admin/notifications/recipient-count` to display count
  - Update recipient count display when selection changes
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [ ] 14. Implement notification sending functionality
  - Add click event handler for send button
  - Collect form data into NotificationRequest object
  - Send POST request to `/api/admin/notifications/send`
  - Display loading spinner during request
  - Handle success response with success message and recipient count
  - Handle error response with error message display
  - Clear form after successful send
  - Close modal after successful send
  - _Requirements: 5.1, 5.2, 5.5, 5.6, 5.7_

- [ ] 15. Create notification history view UI
  - Create notification history page/section in admin dashboard
  - Create table with columns: title, type, target audience, delivery method, recipient count, event, created date, created by
  - Add notification type icons (announcement, reminder, result)
  - Add pagination controls (previous, next, page number)
  - Add filter controls (type dropdown, date range picker)
  - Add search input for title filtering
  - _Requirements: 6.1, 6.2, 6.3, 6.5, 6.7, 6.8_

- [ ] 16. Implement notification history data fetching and display
  - Add function to fetch notification history from `/api/admin/notifications/history`
  - Implement pagination logic (20 notifications per page)
  - Implement filter application (type, date range)
  - Populate table with fetched data
  - Format dates for display
  - Display event title as clickable link (if event association exists)
  - Handle empty state (no notifications)
  - _Requirements: 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8_

- [ ] 17. Add notification type icons and styling
  - Add CSS classes for notification type icons
  - Use Bootstrap Icons for announcement (bi-megaphone), reminder (bi-alarm), result (bi-trophy)
  - Apply icon colors (announcement: blue, reminder: orange, result: green)
  - Add hover effects for interactive elements
  - Ensure responsive design for mobile devices
  - _Requirements: 7.2, 7.3, 7.4_

- [ ] 18. Implement event association display in notifications
  - Display event title as clickable link in web notifications
  - Add click handler to navigate to event details page
  - Display event title in notification history
  - Include event link in email notifications
  - Handle null event associations gracefully
  - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [ ] 19. Add error handling and user feedback
  - Display validation errors inline for each form field
  - Display success toast notification after sending
  - Display error toast notification on failure
  - Show loading spinner during API requests
  - Handle network errors gracefully
  - Display warning when email service is unavailable
  - _Requirements: 5.6, 9.7, 10.1-10.9_

- [ ] 20. Checkpoint - Test complete end-to-end workflow
  - Test admin login and navigation to notification features
  - Test notification creation for all target audience types
  - Test notification sending with web-only, email-only, and both delivery methods
  - Test notification history viewing with filters
  - Test authorization (student users cannot access admin features)
  - Ensure all tests pass, ask the user if questions arise.

- [ ]* 21. Write integration tests for complete notification flow
  - Test notification creation API with database persistence
  - Test email service integration with mock SMTP server
  - Test authorization integration with token validation
  - Test notification history retrieval with pagination and filters

- [ ] 22. Final integration and cleanup
  - Review all code for consistency and best practices
  - Ensure all error messages are user-friendly
  - Verify all logging statements are in place
  - Test with realistic data volumes (100+ students, 10+ events)
  - Update documentation with setup instructions
  - _Requirements: All_

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property-based tests use jqwik framework with minimum 100 iterations
- All property tests are tagged with feature name and property number
- Checkpoints ensure incremental validation at key milestones
- Backend implementation (tasks 1-9) should be completed before frontend (tasks 10-19)
- Email service uses async execution to avoid blocking API responses
- Authorization middleware applies to all `/api/admin/**` endpoints
- Frontend uses Bootstrap 5 for UI components and styling
