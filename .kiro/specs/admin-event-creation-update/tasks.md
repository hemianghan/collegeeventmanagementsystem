# Implementation Plan: Admin Event Creation and Update

## Overview

This implementation plan breaks down the admin event creation and update feature into discrete, actionable tasks. The feature enables administrators to create, update, and delete events through REST API endpoints with proper authorization, validation, and error handling. The frontend already has a modal form UI in place, so we'll focus on backend implementation and ensuring proper integration.

## Tasks

- [ ] 1. Create error handling infrastructure
  - [ ] 1.1 Create ErrorResponse model class
    - Create `ErrorResponse.java` in `com.college.event.model` package
    - Include fields: error, message, field, status
    - Add constructors for different error scenarios
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ] 1.2 Create custom exception classes
    - Create `UnauthenticatedException.java` for authentication failures
    - Create `UnauthorizedException.java` for authorization failures
    - Create `EventNotFoundException.java` for resource not found errors
    - Create `ValidationException.java` for validation errors
    - _Requirements: 1.2, 1.3, 3.2, 4.2_

  - [ ] 1.3 Implement GlobalExceptionHandler
    - Create `GlobalExceptionHandler.java` with `@RestControllerAdvice`
    - Add handler for `UnauthenticatedException` returning 401
    - Add handler for `UnauthorizedException` returning 403
    - Add handler for `MethodArgumentNotValidException` returning 400
    - Add handler for `EventNotFoundException` returning 404
    - Add handler for generic `Exception` returning 500
    - _Requirements: 8.1, 8.2, 8.3_

  - [ ]* 1.4 Write unit tests for exception handlers
    - Test each exception handler returns correct HTTP status
    - Test error response format includes required fields
    - Test validation error includes field name
    - _Requirements: 8.1, 8.2, 8.3_

- [ ] 2. Implement authorization service
  - [ ] 2.1 Create AuthorizationService class
    - Create `AuthorizationService.java` in `com.college.event.service` package
    - Inject `UserRepository` dependency
    - Implement `validateAdminUser(String authToken)` method
    - Implement `validateAuthenticatedUser(String authToken)` method
    - Extract user email from "Bearer <email>" token format
    - Look up user in database by email
    - Verify user role for admin operations
    - Throw appropriate exceptions for auth failures
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 2.2 Write property test for admin authorization
    - **Property 1: Admin Authorization for Event Mutations**
    - **Validates: Requirements 1.1, 1.2**
    - Generate random users with "Admin" and "Student" roles
    - Verify admin users pass authorization
    - Verify non-admin users fail with UnauthorizedException
    - _Requirements: 1.1, 1.2_

  - [ ]* 2.3 Write property test for unauthenticated requests
    - **Property 2: Unauthenticated Request Rejection**
    - **Validates: Requirements 1.3**
    - Generate random invalid/missing tokens
    - Verify all fail with UnauthenticatedException
    - _Requirements: 1.3_

  - [ ]* 2.4 Write unit tests for AuthorizationService
    - Test valid admin token returns user
    - Test student token throws UnauthorizedException
    - Test invalid token throws UnauthenticatedException
    - Test missing token throws UnauthenticatedException
    - _Requirements: 1.1, 1.2, 1.3_

- [ ] 3. Add validation to Event entity
  - [ ] 3.1 Add validation annotations to Event model
    - Add `@NotBlank(message = "Title is required")` to title field
    - Add `@NotBlank(message = "Date is required")` to date field
    - Add `@NotBlank(message = "Location is required")` to location field
    - Add `@NotBlank(message = "Category is required")` to category field
    - Add `@Min(value = 0, message = "Registration fee must be non-negative")` to registrationFee field
    - Ensure default registrationFee = 100.0 in constructor
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 6.1_

  - [ ]* 3.2 Write property test for required field validation
    - **Property 4: Required Field Validation**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**
    - Generate events with randomly missing required fields
    - Verify validation fails for each missing field
    - Verify error message identifies the field
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [ ]* 3.3 Write property test for registration fee validation
    - **Property 5: Registration Fee Non-Negativity**
    - **Validates: Requirements 2.5**
    - Generate random negative registration fees
    - Verify all negative values are rejected
    - Verify zero and positive values are accepted
    - _Requirements: 2.5_

  - [ ]* 3.4 Write property test for default registration fee
    - **Property 12: Default Registration Fee**
    - **Validates: Requirements 6.1**
    - Generate events without registrationFee specified
    - Verify all default to 100.0
    - _Requirements: 6.1_

- [ ] 4. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 5. Update EventController with authorization and validation
  - [ ] 5.1 Add authorization to POST /api/events endpoint
    - Inject `AuthorizationService` into EventController
    - Add `@RequestHeader("Authorization") String authToken` parameter
    - Call `authService.validateAdminUser(authToken)` before processing
    - Add `@Valid` annotation to `@RequestBody Event` parameter
    - Return 201 Created with saved event on success
    - Handle exceptions through GlobalExceptionHandler
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.6_

  - [ ] 5.2 Add authorization to PUT /api/events/{id} endpoint
    - Add `@RequestHeader("Authorization") String authToken` parameter
    - Call `authService.validateAdminUser(authToken)` before processing
    - Add `@Valid` annotation to `@RequestBody Event` parameter
    - Check if event exists, throw EventNotFoundException if not
    - Update event fields and save
    - Preserve event ID during update
    - Return 200 OK with updated event
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5_

  - [ ] 5.3 Add authorization to DELETE /api/events/{id} endpoint
    - Add `@RequestHeader("Authorization") String authToken` parameter
    - Call `authService.validateAdminUser(authToken)` before processing
    - Check if event exists, throw EventNotFoundException if not
    - Delete event from repository
    - Return 204 No Content on success
    - _Requirements: 4.1, 4.2, 4.3_

  - [ ] 5.4 Ensure GET /api/events remains public
    - Verify no authorization required for GET endpoint
    - Ensure all event fields are returned in response
    - _Requirements: 5.1, 5.2, 5.3_

  - [ ]* 5.5 Write property test for valid event creation
    - **Property 6: Valid Event Creation**
    - **Validates: Requirements 2.6**
    - Generate random valid events with all required fields
    - Verify all are created successfully with 201 status
    - Verify returned event includes generated ID
    - _Requirements: 2.6_

  - [ ]* 5.6 Write property test for event update success
    - **Property 7: Event Update Success**
    - **Validates: Requirements 3.1, 3.5**
    - Create events, then generate random valid updates
    - Verify all updates succeed with 200 status
    - Verify original ID is preserved
    - _Requirements: 3.1, 3.5_

  - [ ]* 5.7 Write property test for non-existent resource errors
    - **Property 8: Non-Existent Resource Error**
    - **Validates: Requirements 3.2, 4.2**
    - Generate random non-existent event IDs
    - Verify update requests return 404
    - Verify delete requests return 404
    - _Requirements: 3.2, 4.2_

  - [ ]* 5.8 Write property test for authentication precedes processing
    - **Property 3: Authentication Precedes Processing**
    - **Validates: Requirements 1.4**
    - Send requests with invalid auth and invalid data
    - Verify 401/403 returned before validation errors
    - _Requirements: 1.4_

- [ ] 6. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. Implement field preservation and optional field handling
  - [ ] 7.1 Add property test for field round-trip preservation
    - **Property 14: Event Field Round-Trip Preservation**
    - **Validates: Requirements 7.1, 7.2, 7.3, 9.1, 9.3, 10.1, 10.3**
    - Generate events with random values for all fields
    - Create event, then retrieve it
    - Verify all field values match exactly
    - _Requirements: 7.1, 7.2, 7.3, 9.1, 9.3, 10.1, 10.3_

  - [ ]* 7.2 Write property test for optional field acceptance
    - **Property 15: Optional Field Acceptance**
    - **Validates: Requirements 7.2**
    - Generate events with null/empty imageUrl and description
    - Verify all are accepted and stored correctly
    - _Requirements: 7.2_

  - [ ]* 7.3 Write property test for category string permissiveness
    - **Property 16: Category String Permissiveness**
    - **Validates: Requirements 9.2**
    - Generate random strings (empty, long, special chars)
    - Use as category values
    - Verify all are accepted and stored
    - _Requirements: 9.2_

  - [ ]* 7.4 Write property test for date string acceptance
    - **Property 17: Date String Acceptance**
    - **Validates: Requirements 10.2**
    - Generate random string values as dates
    - Verify all are accepted without transformation
    - _Requirements: 10.2_

  - [ ]* 7.5 Write property test for explicit registration fee preservation
    - **Property 13: Explicit Registration Fee Preservation**
    - **Validates: Requirements 6.2**
    - Generate events with random valid registration fees
    - Verify exact values are stored and returned
    - _Requirements: 6.2_

- [ ] 8. Add validation dependency to build configuration
  - [ ] 8.1 Add Spring Boot Validation dependency
    - Add `implementation 'org.springframework.boot:spring-boot-starter-validation'` to build.gradle
    - Verify jqwik dependency already exists for property-based testing
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5_

- [ ] 9. Verify frontend integration
  - [ ] 9.1 Test event creation from dashboard modal
    - Open dashboard.html in browser
    - Log in as admin user
    - Click "Create Event" button
    - Fill in all required fields
    - Submit form
    - Verify success message displays
    - Verify modal closes
    - Verify new event appears in event list
    - _Requirements: 11.1, 11.2, 11.3, 11.4_

  - [ ] 9.2 Test validation error display
    - Open event creation modal
    - Submit form with missing required fields
    - Verify validation error messages display
    - Verify modal remains open
    - _Requirements: 11.5, 11.7_

  - [ ] 9.3 Test authorization error handling
    - Log in as student user
    - Attempt to access admin controls
    - Verify appropriate error handling
    - _Requirements: 1.2, 11.5_

  - [ ] 9.4 Test modal interaction behavior
    - Open event creation modal
    - Click outside modal area
    - Verify modal closes without submission
    - Open modal again
    - Click cancel button
    - Verify modal closes without submission
    - Verify underlying page is not interactive when modal is open
    - _Requirements: 11.6, 11.8_

- [ ] 10. Integration testing
  - [ ]* 10.1 Write integration test for complete event lifecycle
    - Test create → retrieve → update → delete sequence
    - Use real database (H2 in-memory)
    - Verify data persistence at each step
    - _Requirements: 2.6, 3.1, 4.1, 5.1_

  - [ ]* 10.2 Write integration test for authorization flow
    - Test complete request with authentication token
    - Verify token extraction and user lookup
    - Verify role validation
    - Test with both admin and student users
    - _Requirements: 1.1, 1.2, 1.3, 1.4_

  - [ ]* 10.3 Write integration test for error scenarios
    - Test authentication failure (401)
    - Test authorization failure (403)
    - Test validation failure (400)
    - Test resource not found (404)
    - Verify error response format for each
    - _Requirements: 8.1, 8.2, 8.3_

- [ ] 11. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties using jqwik
- Unit tests validate specific examples and edge cases
- The frontend modal UI already exists in dashboard.html, so frontend tasks focus on integration testing
- Authorization uses simple "Bearer <email>" token format (not production-ready JWT)
- All property-based tests should use jqwik with minimum 100 iterations
- Tag all property tests with: `@Tag("Feature: admin-event-creation-update, Property {number}: {property_text}")`
