# Implementation Plan: Event Rating Board with Sample Data

## Overview

This implementation creates a student-focused rating board that displays event ratings only for events the logged-in student has registered for, along with a sample data population service that creates personalized demonstration ratings based on each student's individual event registrations. The solution integrates with the existing Registration system to query student registrations first, then creates 6-10 sample ratings specifically for the student's registered events using the existing Memory model without schema changes.

## Tasks

- [ ] 1. Create PhotoDataProvider component for registration-based event photos
  - Create PhotoDataProvider class with Base64-encoded sample photos categorized by event type
  - Include photos for different event categories (group activities, performances, workshops, sports)
  - Implement photo selection logic based on student's registered event categories
  - Add method to match photos with registered event types
  - Store diverse sample photos as Base64 strings for each category
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7_

- [ ]* 1.1 Write property test for PhotoDataProvider registration matching
  - **Property 7: Registration-Matched Photo Categories**
  - **Validates: Requirements 2.1, 2.7**

- [ ]* 1.2 Write property test for compatible photo format
  - **Property 8: Compatible Photo Format**
  - **Validates: Requirements 2.5**

- [ ] 2. Create ReviewContentGenerator for registered event-specific content
  - Create ReviewContentGenerator class with event-specific review templates
  - Implement review generation that references actual registered event titles
  - Create review logic that matches star ratings (positive for 4-5 stars, constructive for 2-3 stars)
  - Ensure review text includes actual event titles from student's registration records
  - Tailor review content to specific event types from student's registrations
  - Ensure review text length between 50-300 characters
  - _Requirements: 7.1, 7.2, 7.4, 7.7, 7.8_

- [ ]* 2.1 Write property test for review text length validation
  - **Property 4: Non-Empty Review Text with Length Constraints**
  - **Validates: Requirements 1.6, 7.1**

- [ ]* 2.2 Write property test for review sentiment rating alignment
  - **Property 16: Review Sentiment Rating Alignment**
  - **Validates: Requirements 7.4**

- [ ]* 2.3 Write property test for review content event matching
  - **Property 15: Review Content Event Matching**
  - **Validates: Requirements 7.2, 7.7, 7.8**

- [ ] 3. Create SeedDataService with registration-based sample data creation
  - Create SeedDataService class with @Service annotation
  - Implement initializeSampleDataForStudent(Long userId) method
  - Add getStudentRegistrations(Long userId) method using RegistrationRepository
  - Implement selectRegisteredEventsForSamples() to choose 6-10 events from student's registrations
  - Add studentSampleDataExists(Long userId) method to prevent duplicates per student
  - Create student-specific sample data identification and tracking
  - _Requirements: 1.2, 4.1, 4.2, 4.3, 4.7, 5.1, 5.2, 6.7_

- [ ]* 3.1 Write property test for registration-based sample rating creation
  - **Property 1: Registration-Based Sample Rating Creation**
  - **Validates: Requirements 1.2, 4.3, 4.7**

- [ ]* 3.2 Write property test for student-specific sample rating count
  - **Property 2: Student-Specific Sample Rating Count**
  - **Validates: Requirements 1.4**

- [ ]* 3.3 Write property test for duplicate prevention per student
  - **Property 14: Duplicate Prevention**
  - **Validates: Requirements 6.7**

- [ ] 4. Implement student-specific sample rating creation logic
  - Create createStudentSpecificSampleRating() method for individual students
  - Set userId in reserved range (9001-9010) for sample identification (not target student ID)
  - Generate realistic student names and emails (not target student's actual details)
  - Set isApproved=true and category="Memory" for all sample ratings
  - Use actual event IDs and titles from student's registrations
  - Assign varied star ratings ensuring distribution requirements (5-star, 4-star, 3-star)
  - Associate sample ratings with target student ID for personalized display
  - _Requirements: 1.5, 1.7, 1.8, 6.3, 6.4, 6.5, 7.5_

- [ ]* 4.1 Write property test for valid star rating range
  - **Property 3: Valid Star Rating Range**
  - **Validates: Requirements 1.5**

- [ ]* 4.2 Write property test for pre-approved sample ratings
  - **Property 5: Pre-Approved Sample Ratings**
  - **Validates: Requirements 1.7, 6.4**

- [ ]* 4.3 Write property test for sample student identity protection
  - **Property 6: Sample Student Identity Protection**
  - **Validates: Requirements 1.8**

- [ ]* 4.4 Write property test for complete memory field population
  - **Property 10: Complete Memory Field Population**
  - **Validates: Requirements 6.3**

- [ ]* 4.5 Write property test for correct category assignment
  - **Property 11: Correct Category Assignment**
  - **Validates: Requirements 6.5**

- [ ]* 4.6 Write property test for rating distribution requirements
  - **Property 17: Rating Distribution Requirements**
  - **Validates: Requirements 7.5**

- [ ] 5. Checkpoint - Ensure registration-based components work independently
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Create StudentRatingBoard component for registration-filtered display
  - Create StudentRatingBoard class with @Component annotation
  - Implement getStudentRegisteredEventRatings(Long userId) method
  - Add integration with existing /api/registrations/user/{userId}/detailed endpoint
  - Implement filterRatingsByRegisteredEvents() to show only ratings for student's registered events
  - Add orderRatingsByDateDesc() for proper rating display order
  - Ensure seamless integration with existing MemoryController
  - _Requirements: 3.1, 3.2, 3.3, 3.9, 6.9_

- [ ]* 6.1 Write property test for student registration filtering
  - **Property 9: Student Registration Filtering**
  - **Validates: Requirements 3.1, 3.3**

- [ ] 7. Integrate with Registration system and existing endpoints
  - Add dependency injection for RegistrationRepository in SeedDataService
  - Implement integration with existing Registration model and findByUserId method
  - Add error handling for registration query failures
  - Ensure graceful handling when students have no registrations
  - Add logging for registration-based sample data operations
  - _Requirements: 4.1, 4.2, 6.1, 6.2_

- [ ]* 7.1 Write integration tests for registration system integration
  - Test sample data creation using actual RegistrationRepository queries
  - Test error handling when registration system is unavailable
  - Test behavior with students who have no registrations
  - _Requirements: 4.1, 4.2_

- [ ] 8. Implement student-specific sample data management
  - Add methods to identify and manage sample data per student
  - Implement student-specific sample data deletion without affecting other students
  - Add sample data association tracking with target student IDs
  - Ensure sample ratings are correctly filtered and displayed per student
  - Add capability to regenerate sample data for individual students
  - _Requirements: 5.1, 5.2, 5.4, 5.6, 5.7_

- [ ]* 8.1 Write property test for sample data identification
  - **Property 12: Sample Data Identification**
  - **Validates: Requirements 5.1, 5.6**

- [ ]* 8.2 Write property test for student-specific sample data association
  - **Property 13: Student-Specific Sample Data Association**
  - **Validates: Requirements 5.2, 5.7**

- [ ] 9. Checkpoint - Verify student-specific integration
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Add comprehensive error handling for registration-based approach
  - Handle registration query failures with graceful degradation
  - Add error handling for students with insufficient registered events
  - Implement fallback behavior when registered events no longer exist
  - Add validation for registration data integrity
  - Ensure student privacy protection in all error scenarios
  - _Requirements: Error handling requirements from design document_

- [ ]* 10.1 Write unit tests for error handling scenarios
  - Test behavior when RegistrationRepository queries fail
  - Test sample data creation for students with fewer than 6 registrations
  - Test handling of invalid registration data
  - _Requirements: Error handling validation_

- [ ] 11. Final integration and student-specific validation
  - Wire all components together for student-focused functionality
  - Verify sample ratings are created only for student's registered events
  - Test that each student sees personalized sample data relevant to their registrations
  - Ensure sample data creation is per-student and doesn't interfere between students
  - Validate that student rating board shows only ratings for registered events
  - _Requirements: 1.2, 3.1, 4.3, 4.7, 5.2_

- [ ]* 11.1 Write end-to-end integration tests for student-specific functionality
  - Test complete student-specific sample data creation flow
  - Verify student rating board displays only registered event ratings
  - Test multiple students with different registration portfolios
  - _Requirements: 3.1, 3.3, 5.2, 5.7_

- [ ] 12. Final checkpoint - Ensure student-focused functionality works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Property tests validate universal correctness properties from the design document
- Sample data is created per student based on their individual registrations (6-10 out of 10-12 total registrations)
- Uses existing Registration system with RegistrationRepository.findByUserId method
- Rating board shows only ratings for events the logged-in student has registered for
- Sample data is personalized and relevant to each student's event portfolio
- Integration with Registration, Memory, and Event systems without schema changes
- Student identity protection ensures sample ratings don't use target student's actual details
- Each student receives separate, personalized sample data sets based on their registrations