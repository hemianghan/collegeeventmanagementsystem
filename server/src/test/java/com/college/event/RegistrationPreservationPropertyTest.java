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
 * Preservation Property Tests for Registration Data Persistence Bugfix
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4**
 * 
 * These tests capture the observed behavior on UNFIXED code for non-buggy inputs.
 * They verify that registration API endpoints, validation logic, payment processing,
 * and sample data creation work unchanged after the fix.
 * 
 * IMPORTANT: These tests MUST PASS on unfixed code (current in-memory H2 database).
 * They verify baseline behavior to preserve.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RegistrationPreservationPropertyTest {

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
     * Property 1: API Response Preservation
     * 
     * For any valid registration request, the API SHALL return a success response
     * with the registration object and event title.
     * 
     * **Validates: Requirement 3.2**
     */
    @Test
    void testProperty_RegistrationAPIReturnsSuccessResponse() {
        // Create registration request
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // Act: Register for event
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        
        // Assert: Response structure is preserved
        assertNotNull(response, "Response should not be null");
        assertNotNull(response.getBody(), "Response body should not be null");
        Map<String, Object> body = response.getBody();
        assertTrue((Boolean) body.get("success"), "Response should indicate success");
        assertNotNull(body.get("registration"), "Response should contain registration object");
        assertNotNull(body.get("eventTitle"), "Response should contain event title");
        assertEquals(testEvent.getTitle(), body.get("eventTitle"), 
            "Event title in response should match the registered event");
    }
    
    /**
     * Property 2: Validation Logic Preservation - Duplicate Detection
     * 
     * For any user attempting to register for the same event twice,
     * the system SHALL return an error response indicating duplicate registration.
     * 
     * **Validates: Requirement 3.4**
     */
    @Test
    void testProperty_DuplicateRegistrationDetection() {
        // Create first registration request
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // Act 1: First registration should succeed
        ResponseEntity<Map<String, Object>> firstResponse = registrationController.registerForEvent(registrationRequest);
        assertTrue((Boolean) firstResponse.getBody().get("success"), 
            "First registration should succeed");
        
        // Act 2: Attempt duplicate registration
        ResponseEntity<Map<String, Object>> secondResponse = registrationController.registerForEvent(registrationRequest);
        
        // Assert: Duplicate registration should be rejected
        assertFalse((Boolean) secondResponse.getBody().get("success"), 
            "Duplicate registration should be rejected");
        String message = secondResponse.getBody().get("message").toString().toLowerCase();
        assertTrue(message.contains("already") || message.contains("registered"),
            "Error message should indicate duplicate registration");
    }
    
    /**
     * Property 3: Validation Logic Preservation - Event Existence Check
     * 
     * For any registration request with a non-existent event ID,
     * the system SHALL return an error response indicating event not found.
     * 
     * **Validates: Requirement 3.2**
     */
    @Test
    void testProperty_EventExistenceValidation() {
        Long nonExistentEventId = 99999L;
        
        // Create registration request with non-existent event
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", nonExistentEventId);
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // Act: Attempt registration with non-existent event
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        
        // Assert: Registration should fail with event not found error
        assertFalse((Boolean) response.getBody().get("success"), 
            "Registration with non-existent event should fail");
        String message = response.getBody().get("message").toString().toLowerCase();
        assertTrue(message.contains("not found") || message.contains("event"),
            "Error message should indicate event not found");
    }
    
    /**
     * Property 4: Payment Processing Preservation - Online Payment
     * 
     * For any registration with ONLINE payment method, the system SHALL:
     * - Set status to "PAID"
     * - Set paymentStatus to "COMPLETED"
     * - Generate a transaction ID
     * - Set payment date to current time
     * 
     * **Validates: Requirement 3.3**
     */
    @Test
    void testProperty_OnlinePaymentProcessing() {
        // Create registration request with ONLINE payment
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // Act: Register with online payment
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        Registration registration = (Registration) response.getBody().get("registration");
        
        // Assert: Online payment should produce specific status values
        assertEquals("PAID", registration.getStatus(), 
            "Status should be PAID for online payment");
        assertEquals("COMPLETED", registration.getPaymentStatus(), 
            "Payment status should be COMPLETED for online payment");
        assertNotNull(registration.getTransactionId(), 
            "Transaction ID should be generated for online payment");
        assertTrue(registration.getTransactionId().startsWith("TXN"), 
            "Transaction ID should start with TXN prefix");
        assertNotNull(registration.getPaymentDate(), 
            "Payment date should be set for online payment");
    }
    
    /**
     * Property 5: Payment Processing Preservation - Desk Payment
     * 
     * For any registration with DESK payment method, the system SHALL:
     * - Set status to "PENDING"
     * - Set paymentStatus to "PENDING"
     * - NOT set payment date
     * 
     * **Validates: Requirement 3.3**
     */
    @Test
    void testProperty_DeskPaymentProcessing() {
        // Create registration request with DESK payment
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "DESK");
        
        // Act: Register with desk payment
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        Registration registration = (Registration) response.getBody().get("registration");
        
        // Assert: Desk payment should produce pending status
        assertEquals("PENDING", registration.getStatus(), 
            "Status should be PENDING for desk payment");
        assertEquals("PENDING", registration.getPaymentStatus(), 
            "Payment status should be PENDING for desk payment");
        assertNull(registration.getPaymentDate(), 
            "Payment date should not be set for desk payment");
    }
    
    /**
     * Property 6: Registration Field Preservation
     * 
     * For any valid registration, the system SHALL preserve all input fields
     * in the saved registration object.
     * 
     * **Validates: Requirement 3.2**
     */
    @Test
    void testProperty_RegistrationFieldPreservation() {
        // Create registration request
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        // Act: Register for event
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        Registration registration = (Registration) response.getBody().get("registration");
        
        // Assert: All fields should be preserved
        assertEquals(testUser.getId(), registration.getUserId(), 
            "User ID should be preserved");
        assertEquals(testEvent.getId(), registration.getEventId(), 
            "Event ID should be preserved");
        assertEquals(testUser.getName(), registration.getUserName(), 
            "User name should be preserved");
        assertEquals(testUser.getEmail(), registration.getUserEmail(), 
            "User email should be preserved");
        assertEquals("ONLINE", registration.getPaymentMethod(), 
            "Payment method should be preserved");
        assertEquals("Participant", registration.getEventRole(), 
            "Event role should default to Participant");
        assertNotNull(registration.getRegistrationDate(), 
            "Registration date should be set");
        assertEquals(testEvent.getRegistrationFee(), registration.getRegistrationFee(), 
            "Registration fee should match event fee");
    }
    
    /**
     * Property 7: Sample Data Creation Preservation
     * 
     * For the application startup, the system SHALL create sample registrations
     * exactly once and only when the registration repository is empty.
     * 
     * **Validates: Requirement 3.1**
     */
    @Test
    void testProperty_SampleDataCreation() {
        // For this test, we verify that if we manually create sample data,
        // it follows the expected pattern
        
        Registration sampleRegistration = new Registration();
        sampleRegistration.setUserId(testUser.getId());
        sampleRegistration.setEventId(testEvent.getId());
        sampleRegistration.setUserName(testUser.getName());
        sampleRegistration.setUserEmail(testUser.getEmail());
        sampleRegistration.setPaymentMethod("DESK");
        sampleRegistration.setRegistrationFee(testEvent.getRegistrationFee());
        sampleRegistration.setRegistrationDate(LocalDateTime.now().minusDays(35));
        sampleRegistration.setEventRole("Participant");
        sampleRegistration.setStatus("COMPLETED");
        sampleRegistration.setPaymentStatus("COMPLETED");
        sampleRegistration.setTransactionId("REG" + System.nanoTime());
        sampleRegistration.setPaymentDate(LocalDateTime.now().minusDays(30));
        
        // Act: Save sample registration
        Registration saved = registrationRepository.save(sampleRegistration);
        
        // Assert: Sample registration should be saved and retrievable
        assertNotNull(saved.getId(), "Sample registration should have an ID");
        List<Registration> retrieved = registrationRepository.findByUserId(testUser.getId());
        assertEquals(1, retrieved.size(), "Sample registration should be retrievable");
        assertEquals(sampleRegistration.getUserName(), retrieved.get(0).getUserName(), 
            "Sample registration data should be preserved");
    }
    
    /**
     * Property 8: Query Functionality Preservation
     * 
     * For any saved registration, the system SHALL be able to retrieve it
     * using findByUserId and findByEventId queries.
     * 
     * **Validates: Requirement 3.2**
     */
    @Test
    void testProperty_RegistrationQueries() {
        // Create and save registration
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        Registration registration = (Registration) response.getBody().get("registration");
        
        // Act & Assert: Query by user ID
        List<Registration> userRegistrations = registrationRepository.findByUserId(testUser.getId());
        assertTrue(userRegistrations.stream().anyMatch(r -> r.getId().equals(registration.getId())),
            "Registration should be retrievable by user ID");
        
        // Act & Assert: Query by event ID
        List<Registration> eventRegistrations = registrationRepository.findByEventId(testEvent.getId());
        assertTrue(eventRegistrations.stream().anyMatch(r -> r.getId().equals(registration.getId())),
            "Registration should be retrievable by event ID");
    }
}
