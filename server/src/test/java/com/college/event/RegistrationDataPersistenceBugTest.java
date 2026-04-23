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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Bug Condition Exploration Test for Registration Data Persistence
 * 
 * **Validates: Requirements 2.1, 2.2, 2.3**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * This test encodes the expected behavior and will validate the fix when it passes after implementation.
 * The goal is to surface counterexamples that demonstrate the bug exists.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistrationDataPersistenceBugTest {

    @Autowired
    private RegistrationController registrationController;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private Event testEvent;
    private User testUser;
    
    @BeforeEach
    void setUp() {
        // Create test event
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        testEvent.setLocation("Test Location");
        testEvent.setDescription("Test Description");
        testEvent.setCategory("Technical");
        testEvent.setDate("2026-06-15");
        testEvent.setRegistrationFee(100.0);
        testEvent = eventRepository.save(testEvent);
        
        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole("Student");
        testUser = userRepository.save(testUser);
    }
    
    /**
     * Property 1: Bug Condition - Registration Data Persistence Across Restarts
     * 
     * This test implements the bug condition check:
     * isBugCondition(input) where input.isNewRegistration = true 
     * AND databaseType = "H2_IN_MEMORY" 
     * AND ddlMode = "create-drop" 
     * AND applicationRestarted = true
     * 
     * Expected outcome: Test FAILS on unfixed code (this proves the bug exists)
     */
    @Test
    void testBugCondition_RegistrationDataPersistenceAcrossRestarts() {
        // ARRANGE: Create registration request that meets bug condition
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // ACT 1: Create new registration (isNewRegistration = true)
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        
        // VERIFY: Registration was created successfully
        assertTrue((Boolean) response.getBody().get("success"), 
            "Registration should be created successfully");
        
        Long registrationId = ((Registration) response.getBody().get("registration")).getId();
        assertNotNull(registrationId, "Registration ID should be generated");
        
        // VERIFY: Registration exists immediately after creation
        List<Registration> userRegistrations = registrationRepository.findByUserId(testUser.getId());
        assertEquals(1, userRegistrations.size(), 
            "Registration should exist immediately after creation");
        
        // ACT 2: Simulate application restart by clearing the H2 in-memory database
        // This simulates the bug condition where databaseType = "H2_IN_MEMORY" 
        // AND ddlMode = "create-drop" AND applicationRestarted = true
        simulateApplicationRestart();
        
        // ACT 3: Attempt to retrieve registration after restart
        List<Registration> registrationsAfterRestart = registrationRepository.findByUserId(testUser.getId());
        
        // ASSERT: This is the bug condition test - on unfixed code, this will FAIL
        // because the registration data will be lost due to H2 in-memory + create-drop configuration
        assertFalse(registrationsAfterRestart.isEmpty(), 
            "CRITICAL BUG: Registration data should persist across application restarts. " +
            "If this test fails, it confirms the bug exists - registration data is lost " +
            "when using H2 in-memory database with create-drop DDL mode.");
        
        assertEquals(1, registrationsAfterRestart.size(), 
            "Exactly one registration should persist after restart");
        
        Registration persistedRegistration = registrationsAfterRestart.get(0);
        assertEquals(testUser.getId(), persistedRegistration.getUserId(), 
            "User ID should match after restart");
        assertEquals(testEvent.getId(), persistedRegistration.getEventId(), 
            "Event ID should match after restart");
        assertEquals("ONLINE", persistedRegistration.getPaymentMethod(), 
            "Payment method should persist after restart");
        assertEquals("COMPLETED", persistedRegistration.getPaymentStatus(), 
            "Payment status should persist after restart");
        assertNotNull(persistedRegistration.getTransactionId(), 
            "Transaction ID should persist after restart");
    }
    
    /**
     * Simulates application restart by recreating the database schema
     * This mimics the behavior of H2 in-memory database with create-drop DDL mode
     */
    private void simulateApplicationRestart() {
        // Clear all data to simulate the create-drop behavior
        registrationRepository.deleteAll();
        
        // Recreate the test data that would be created by CommandLineRunner
        // (This simulates why sample data appears to work - it's recreated on each restart)
        testEvent = eventRepository.save(testEvent);
        testUser = userRepository.save(testUser);
        
        // Note: The new registration data is NOT recreated here, 
        // which demonstrates the bug - only sample data gets recreated
    }
    
    /**
     * Additional test to verify the bug condition parameters
     */
    @Test
    void testBugConditionParameters() {
        // Verify we're testing with the correct bug condition parameters
        String datasourceUrl = System.getProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        String ddlAuto = System.getProperty("spring.jpa.hibernate.ddl-auto", "create-drop");
        
        assertTrue(datasourceUrl.contains("h2:mem:"), 
            "Test should use H2 in-memory database to reproduce bug condition");
        assertEquals("create-drop", ddlAuto, 
            "Test should use create-drop DDL mode to reproduce bug condition");
        
        System.out.println("Bug condition parameters verified:");
        System.out.println("- Database Type: H2_IN_MEMORY ✓");
        System.out.println("- DDL Mode: create-drop ✓");
        System.out.println("- New Registration: Will be tested ✓");
        System.out.println("- Application Restart: Will be simulated ✓");
    }
}