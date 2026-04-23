package com.college.event;

import com.college.event.model.Registration;
import com.college.event.model.Event;
import com.college.event.model.User;
import com.college.event.repository.RegistrationRepository;
import com.college.event.repository.EventRepository;
import com.college.event.repository.UserRepository;
import com.college.event.controller.RegistrationController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import net.jqwik.api.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Registration Count Display
 * 
 * **Validates: Requirements 2.1**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * This test encodes the expected behavior and will validate the fix when it passes after implementation.
 * The goal is to surface counterexamples that demonstrate the incorrect registration count display.
 * 
 * EXPECTED OUTCOME: Test FAILS showing incorrect count (401 instead of actual 10-12)
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.test.context.ActiveProfiles("test")
public class RegistrationCountDisplayBugTest {

    @Autowired
    private RegistrationController registrationController;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private List<Event> testEvents;
    
    @BeforeEach
    void setUp() {
        // Create test user (different from admin to isolate the bug)
        testUser = new User();
        testUser.setName("Test Student");
        testUser.setEmail("teststudent@college.edu");
        testUser.setPassword("password");
        testUser.setRole("Student");
        testUser = userRepository.save(testUser);
        
        // Create test events
        testEvents = List.of(
            createEvent("Test Event 1", "Technical"),
            createEvent("Test Event 2", "Cultural"),
            createEvent("Test Event 3", "Sports")
        );
    }
    
    private Event createEvent(String title, String category) {
        Event event = new Event();
        event.setTitle(title);
        event.setLocation("Test Location");
        event.setDescription("Test Description");
        event.setCategory(category);
        event.setDate("2026-06-15");
        event.setRegistrationFee(100.0);
        return eventRepository.save(event);
    }
    
    /**
     * Property 1: Bug Condition - Registration Count Display Bug
     * 
     * This test implements the bug condition check:
     * isBugCondition(input) where input.action == "VIEW_MY_EVENTS" 
     * AND displayedCount != actualRegistrationCount
     * 
     * Expected outcome: Test FAILS on unfixed code (this proves the bug exists)
     * The test will show that loadMyEventsCount() displays incorrect count when using /stats endpoint
     */
    @Property
    void testBugCondition_RegistrationCountDisplayBug(@ForAll("userRegistrationScenarios") UserRegistrationScenario scenario) {
        // ARRANGE: Create registrations for the test user according to scenario
        createUserRegistrations(scenario.registrationCount);
        
        // ACT: Get registration stats using the same endpoint as loadMyEventsCount()
        ResponseEntity<Map<String, Object>> response = registrationController.getUserRegistrationStats(testUser.getId());
        
        // VERIFY: Response is successful
        assertNotNull(response, "Stats response should not be null");
        assertNotNull(response.getBody(), "Stats response body should not be null");
        Map<String, Object> stats = response.getBody();
        
        // Get the actual count from database for verification
        List<Registration> actualRegistrations = registrationRepository.findByUserId(testUser.getId());
        int actualCount = actualRegistrations.size();
        
        // Get the displayed count from stats endpoint (what loadMyEventsCount() uses)
        Object totalRegistrationsObj = stats.get("totalRegistrations");
        assertNotNull(totalRegistrationsObj, "totalRegistrations should be present in stats");
        int displayedCount = ((Number) totalRegistrationsObj).intValue();
        
        // ASSERT: This is the bug condition test - on unfixed code, this will FAIL
        // because the displayed count will be the total system registrations (401) 
        // instead of the user-specific registrations (10-12)
        assertEquals(actualCount, displayedCount, 
            String.format("CRITICAL BUG: Registration count display bug detected! " +
                "Expected user-specific count: %d, but /stats endpoint returned: %d. " +
                "This confirms the bug exists - the system is showing total system registrations " +
                "instead of user-specific registrations. " +
                "User ID: %d, Actual user registrations in DB: %d, " +
                "Stats endpoint response: %d", 
                actualCount, displayedCount, testUser.getId(), actualCount, displayedCount));
        
        // Additional verification: ensure we created the expected number of registrations
        assertEquals(scenario.registrationCount, actualCount,
            "Test setup should create the expected number of registrations");
        
        // Verify the registrations belong to the correct user
        for (Registration reg : actualRegistrations) {
            assertEquals(testUser.getId(), reg.getUserId(),
                "All registrations should belong to the test user");
        }
    }
    
    /**
     * Unit test version of the bug condition for clearer failure reporting
     */
    @Test
    void testBugCondition_RegistrationCountDisplay_SpecificScenario() {
        // ARRANGE: Create exactly 12 registrations for the test user (matching the bug description)
        int expectedUserRegistrations = 12;
        createUserRegistrations(expectedUserRegistrations);
        
        // Verify we have the expected sample data in the system
        long totalSystemRegistrations = registrationRepository.count();
        System.out.println("Total registrations in system: " + totalSystemRegistrations);
        System.out.println("Expected user registrations: " + expectedUserRegistrations);
        
        // ACT: Get registration stats using the same endpoint as loadMyEventsCount()
        ResponseEntity<Map<String, Object>> response = registrationController.getUserRegistrationStats(testUser.getId());
        
        // VERIFY: Response structure
        assertNotNull(response, "Stats response should not be null");
        assertNotNull(response.getBody(), "Stats response body should not be null");
        Map<String, Object> stats = response.getBody();
        
        // Get the actual count from database
        List<Registration> actualUserRegistrations = registrationRepository.findByUserId(testUser.getId());
        int actualCount = actualUserRegistrations.size();
        
        // Get the displayed count from stats endpoint
        Object totalRegistrationsObj = stats.get("totalRegistrations");
        assertNotNull(totalRegistrationsObj, "totalRegistrations should be present in stats");
        int displayedCount = ((Number) totalRegistrationsObj).intValue();
        
        // Debug information
        System.out.println("User ID: " + testUser.getId());
        System.out.println("Actual user registrations in DB: " + actualCount);
        System.out.println("Stats endpoint returned: " + displayedCount);
        System.out.println("Total system registrations: " + totalSystemRegistrations);
        
        // ASSERT: This is the critical bug test
        // On unfixed code, this will fail because displayedCount will be ~401 (total system)
        // instead of 12 (user-specific)
        assertEquals(actualCount, displayedCount, 
            String.format("REGISTRATION COUNT DISPLAY BUG DETECTED!\n" +
                "The /api/registrations/user/{userId}/stats endpoint is returning incorrect count.\n" +
                "Expected (user-specific): %d registrations\n" +
                "Actual (from stats endpoint): %d registrations\n" +
                "This suggests the endpoint is returning total system registrations instead of user-specific count.\n" +
                "Bug confirmed: loadMyEventsCount() will display %d instead of %d in the dashboard.",
                actualCount, displayedCount, displayedCount, actualCount));
        
        // Verify our test setup is correct
        assertEquals(expectedUserRegistrations, actualCount,
            "Test should create exactly " + expectedUserRegistrations + " registrations for the user");
    }
    
    /**
     * Test the exact bug scenario: Student with 10-12 registrations seeing 401
     * This test will create the conditions described in the bug report
     */
    @Test
    void testBugCondition_StudentWith10To12RegistrationsSeeing401() {
        // ARRANGE: Create a student user with exactly 11 registrations (within 10-12 range)
        User studentUser = new User();
        studentUser.setName("Bug Test Student");
        studentUser.setEmail("bugteststudent@college.edu");
        studentUser.setPassword("password");
        studentUser.setRole("Student");
        studentUser = userRepository.save(studentUser);
        
        // Create exactly 11 registrations for this student
        int expectedStudentRegistrations = 11;
        for (int i = 0; i < expectedStudentRegistrations; i++) {
            Event event = createEvent("Student Event " + (i + 1), "Technical");
            
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("userId", studentUser.getId());
            registrationRequest.put("eventId", event.getId());
            registrationRequest.put("userName", studentUser.getName());
            registrationRequest.put("userEmail", studentUser.getEmail());
            registrationRequest.put("paymentMethod", "ONLINE");
            
            ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
            assertTrue((Boolean) response.getBody().get("success"), 
                "Student registration " + (i + 1) + " should be successful");
        }
        
        // Get system state
        long totalSystemRegistrations = registrationRepository.count();
        List<Registration> studentRegistrations = registrationRepository.findByUserId(studentUser.getId());
        int actualStudentCount = studentRegistrations.size();
        
        System.out.println("=== BUG SCENARIO TEST ===");
        System.out.println("Student User ID: " + studentUser.getId());
        System.out.println("Student actual registrations in DB: " + actualStudentCount);
        System.out.println("Total system registrations: " + totalSystemRegistrations);
        
        // ACT: Test the stats endpoint that loadMyEventsCount() uses
        ResponseEntity<Map<String, Object>> response = registrationController.getUserRegistrationStats(studentUser.getId());
        
        // VERIFY: Response structure
        assertNotNull(response, "Stats response should not be null");
        assertNotNull(response.getBody(), "Stats response body should not be null");
        Map<String, Object> stats = response.getBody();
        
        // Get the displayed count from stats endpoint
        Object totalRegistrationsObj = stats.get("totalRegistrations");
        assertNotNull(totalRegistrationsObj, "totalRegistrations should be present in stats");
        int displayedCount = ((Number) totalRegistrationsObj).intValue();
        
        System.out.println("Stats endpoint returned for student: " + displayedCount);
        
        // CRITICAL BUG CHECK: The bug report says student sees 401 instead of 10-12
        if (displayedCount == 401 || displayedCount > 400) {
            fail(String.format("BUG REPRODUCED! Student with %d registrations is seeing %d in the dashboard. " +
                "This matches the bug report of seeing 401 instead of actual 10-12 registrations.",
                actualStudentCount, displayedCount));
        }
        
        // Check if it's returning total system count (which would be the bug)
        if (displayedCount == totalSystemRegistrations && displayedCount != actualStudentCount) {
            fail(String.format("BUG CONFIRMED! Stats endpoint is returning total system registrations (%d) " +
                "instead of user-specific count (%d). This explains why students see ~401 instead of their actual count.",
                displayedCount, actualStudentCount));
        }
        
        // This assertion should pass if the API is working correctly
        assertEquals(actualStudentCount, displayedCount, 
            String.format("Student should see their actual registration count (%d), not %d. " +
                "If this fails with displayedCount=401, the bug is confirmed.",
                actualStudentCount, displayedCount));
        
        // Verify our test setup
        assertEquals(expectedStudentRegistrations, actualStudentCount,
            "Test should create exactly " + expectedStudentRegistrations + " registrations for the student");
    }
    
    /**
     * Creates the specified number of registrations for the test user
     */
    private void createUserRegistrations(int count) {
        for (int i = 0; i < count; i++) {
            // Create a unique event for each registration to avoid duplicate registration errors
            Event event = createEvent("Test Event " + (i + 1), "Technical");
            
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("userId", testUser.getId());
            registrationRequest.put("eventId", event.getId());
            registrationRequest.put("userName", testUser.getName());
            registrationRequest.put("userEmail", testUser.getEmail());
            registrationRequest.put("paymentMethod", i % 2 == 0 ? "ONLINE" : "DESK");
            
            ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
            assertTrue((Boolean) response.getBody().get("success"), 
                "Registration " + (i + 1) + " should be successful");
        }
    }
    
    /**
     * Generator for user registration scenarios
     */
    @Provide
    Arbitrary<UserRegistrationScenario> userRegistrationScenarios() {
        return Arbitraries.integers()
            .between(10, 15)  // Test with 10-15 registrations (matching bug description of 10-12)
            .map(count -> new UserRegistrationScenario(count));
    }
    
    /**
     * Test scenario for user registrations
     */
    static class UserRegistrationScenario {
        final int registrationCount;
        
        UserRegistrationScenario(int registrationCount) {
            this.registrationCount = registrationCount;
        }
        
        @Override
        public String toString() {
            return String.format("UserRegistrationScenario{registrationCount=%d}", registrationCount);
        }
    }
}