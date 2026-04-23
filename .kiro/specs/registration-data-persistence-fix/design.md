# Registration Data Persistence Bugfix Design

## Overview

The College Event Management System has a critical data persistence bug where student registration data is not being saved permanently to the database. While the registration API endpoint returns success responses and the frontend shows successful registration messages, the actual registration records are lost when the application restarts or the database connection is reset. This occurs because the application is configured to use an H2 in-memory database with `create-drop` DDL mode, which destroys all data when the application shuts down or the database session ends. The bug affects new student registrations while existing sample registrations (created during application startup) appear to work temporarily until the next restart.

## Glossary

- **Bug_Condition (C)**: The condition that triggers data loss - when registration data is saved to an H2 in-memory database with create-drop configuration
- **Property (P)**: The desired behavior when registration data is saved - data should persist permanently across application restarts and database sessions
- **Preservation**: Existing registration functionality, API responses, and sample data creation that must remain unchanged by the fix
- **RegistrationController.registerForEvent()**: The endpoint in `server/src/main/java/com/college/event/controller/RegistrationController.java` that handles new student registrations
- **H2 Database**: The current in-memory database configured in `application.properties` that loses data on restart
- **DDL Mode**: The Hibernate setting `spring.jpa.hibernate.ddl-auto=create-drop` that recreates the database schema on each startup

## Bug Details

### Bug Condition

The bug manifests when registration data is saved to the H2 in-memory database configured with `create-drop` DDL mode. The `RegistrationController.registerForEvent()` method successfully creates and saves Registration objects, but the underlying database configuration causes data loss when the application restarts or the database session ends.

**Formal Specification:**
```
FUNCTION isBugCondition(input)
  INPUT: input of type RegistrationRequest
  OUTPUT: boolean
  
  RETURN input.isNewRegistration = true
         AND databaseType = "H2_IN_MEMORY"
         AND ddlMode = "create-drop"
         AND applicationRestarted = true
END FUNCTION
```

### Examples

- **Example 1**: Student submits registration for "Tech Symposium" → API returns success → Application restarts → Registration data is lost
- **Example 2**: Student registers for "Cultural Fest 2026" with online payment → Transaction ID generated → Database session ends → Registration cannot be retrieved
- **Example 3**: Multiple students register for events → All registrations appear successful → Server restart occurs → All new registrations disappear, only sample data remains
- **Edge Case**: Sample registrations created during startup appear to work because they are recreated on each restart by the CommandLineRunner

## Expected Behavior

### Preservation Requirements

**Unchanged Behaviors:**
- Registration API endpoints must continue to return the same success/error responses
- Sample data creation during application startup must continue to work exactly as before
- Registration validation logic (duplicate detection, event existence checks) must remain unchanged
- Payment processing simulation for online and desk payments must continue to work
- All existing API endpoints for querying registrations must continue to function

**Scope:**
All functionality that does NOT involve data persistence across application restarts should be completely unaffected by this fix. This includes:
- API request/response handling and validation
- Registration object creation and field mapping
- Payment method processing and transaction ID generation
- Error handling and duplicate registration detection

## Hypothesized Root Cause

Based on the code analysis, the root cause is the database configuration in `application.properties`:

1. **In-Memory Database**: The application uses `spring.datasource.url=jdbc:h2:mem:eventdb` which creates an H2 database in memory
   - Memory databases are destroyed when the JVM process ends
   - Data exists only for the duration of the application session

2. **Create-Drop DDL Mode**: The setting `spring.jpa.hibernate.ddl-auto=create-drop` causes Hibernate to:
   - Create database tables on startup
   - Drop all tables and data on shutdown
   - This ensures data loss even if the database were persistent

3. **Sample Data Illusion**: The CommandLineRunner in `EventApplication.java` creates sample registrations on each startup
   - This makes it appear that "some" registrations work
   - In reality, these are recreated fresh each time, masking the persistence issue

4. **Transaction Scope**: While the `@Transactional` annotation on the delete method works correctly, the save operations complete successfully within their transaction scope but the data is lost due to the database configuration, not transaction issues

## Correctness Properties

Property 1: Bug Condition - Registration Data Persistence

_For any_ registration request where new registration data is saved to the database, the fixed system SHALL persist that registration data permanently across application restarts, database reconnections, and server shutdowns, making it retrievable through subsequent API calls.

**Validates: Requirements 2.1, 2.2, 2.3**

Property 2: Preservation - Existing Functionality Behavior

_For any_ registration API operation that does NOT involve cross-restart data persistence (validation, response formatting, duplicate detection, payment processing), the fixed system SHALL produce exactly the same behavior as the original system, preserving all existing functionality and API contracts.

**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

## Fix Implementation

### Changes Required

The fix involves changing the database configuration from in-memory H2 to persistent H2 file-based storage.

**File**: `server/src/main/resources/application.properties`

**Specific Changes**:
1. **Database URL Change**: Replace `jdbc:h2:mem:eventdb` with `jdbc:h2:file:./data/eventdb`
   - Changes from in-memory (`mem:`) to file-based (`file:`) storage
   - Stores database files in `./data/` directory relative to application root
   - Database files will persist across application restarts

2. **DDL Mode Change**: Replace `create-drop` with `update`
   - `update` mode preserves existing data and only adds new schema changes
   - Prevents data loss on application restart
   - Maintains schema evolution capability for future updates

3. **Data Directory Creation**: Ensure the `./data/` directory exists
   - H2 will create the directory if it doesn't exist
   - Database files `eventdb.mv.db` and `eventdb.trace.db` will be stored here

4. **Console Access Update**: H2 console will now connect to the persistent file database
   - Same console access via `http://localhost:8080/h2-console`
   - JDBC URL in console should be updated to `jdbc:h2:file:./data/eventdb`

5. **Sample Data Logic**: No changes needed to CommandLineRunner
   - The existing `if (registrationRepo.count() == 0)` checks will prevent duplicate sample data
   - Sample data will only be created once, then persist across restarts

## Testing Strategy

### Validation Approach

The testing strategy follows a two-phase approach: first, surface counterexamples that demonstrate the bug on unfixed code, then verify the fix works correctly and preserves existing behavior.

### Exploratory Bug Condition Checking

**Goal**: Surface counterexamples that demonstrate the bug BEFORE implementing the fix. Confirm or refute the root cause analysis. If we refute, we will need to re-hypothesize.

**Test Plan**: Write tests that create registrations, restart the application (or simulate database session end), and attempt to retrieve the registrations. Run these tests on the UNFIXED code to observe data loss and confirm the root cause.

**Test Cases**:
1. **Registration Persistence Test**: Create registration → Restart application → Query registration (will fail on unfixed code)
2. **Multiple Registration Test**: Create 5 registrations → Restart application → Count registrations (will show 0 on unfixed code)
3. **Sample Data vs New Data Test**: Verify sample data recreated but new registrations lost (will demonstrate the illusion on unfixed code)
4. **Database File Test**: Check if database files exist after shutdown (will fail on unfixed code with in-memory DB)

**Expected Counterexamples**:
- Registration data disappears after application restart
- Possible causes: in-memory database configuration, create-drop DDL mode, transaction scope issues

### Fix Checking

**Goal**: Verify that for all inputs where the bug condition holds, the fixed function produces the expected behavior.

**Pseudocode:**
```
FOR ALL input WHERE isBugCondition(input) DO
  result := registerForEvent_fixed(input)
  restartApplication()
  retrievedData := getUserRegistrations(input.userId)
  ASSERT retrievedData.contains(result)
END FOR
```

### Preservation Checking

**Goal**: Verify that for all inputs where the bug condition does NOT hold, the fixed function produces the same result as the original function.

**Pseudocode:**
```
FOR ALL input WHERE NOT isBugCondition(input) DO
  ASSERT registerForEvent_original(input) = registerForEvent_fixed(input)
END FOR
```

**Testing Approach**: Property-based testing is recommended for preservation checking because:
- It generates many test cases automatically across the input domain
- It catches edge cases that manual unit tests might miss
- It provides strong guarantees that behavior is unchanged for all non-persistence operations

**Test Plan**: Observe behavior on UNFIXED code first for API responses, validation, and error handling, then write property-based tests capturing that behavior.

**Test Cases**:
1. **API Response Preservation**: Verify registration API returns same success/error responses after fix
2. **Validation Logic Preservation**: Verify duplicate detection and event validation continue working
3. **Payment Processing Preservation**: Verify online/desk payment logic produces same results
4. **Sample Data Creation Preservation**: Verify CommandLineRunner creates same sample data after fix

### Unit Tests

- Test registration creation and immediate retrieval (within same session)
- Test duplicate registration detection with persistent data
- Test edge cases (invalid event IDs, missing user data) with persistent storage
- Test that API responses remain consistent before and after persistence fix

### Property-Based Tests

- Generate random registration requests and verify they persist across application restarts
- Generate random user/event combinations and verify duplicate detection works with persistent data
- Test that all non-persistence operations (validation, response formatting) produce identical results across many scenarios

### Integration Tests

- Test full registration flow with database persistence across application restarts
- Test that sample data is created only once and persists across multiple restarts
- Test that H2 console can access the persistent database files
- Test that registration statistics and detailed queries work with persistent data