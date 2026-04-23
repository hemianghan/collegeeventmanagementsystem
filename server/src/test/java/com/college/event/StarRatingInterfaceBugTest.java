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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import net.jqwik.api.*;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition Exploration Test for Star Rating Interface
 * 
 * **Validates: Requirements 2.2**
 * 
 * CRITICAL: This test MUST FAIL on unfixed code - failure confirms the bug exists
 * 
 * This test encodes the expected behavior and will validate the fix when it passes after implementation.
 * The goal is to surface counterexamples that demonstrate non-functional star rating interface.
 * 
 * EXPECTED OUTCOME: Test FAILS showing missing or non-functional star rating interface in dashboard
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.test.context.ActiveProfiles("test")
public class StarRatingInterfaceBugTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private RegistrationController registrationController;
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    private MockMvc mockMvc;
    private User testUser;
    private Event testEvent;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // Create test user (student who should be able to rate events)
        testUser = new User();
        testUser.setName("Test Student");
        testUser.setEmail("teststudent@college.edu");
        testUser.setPassword("password");
        testUser.setRole("Student");
        testUser = userRepository.save(testUser);
        
        // Create test event that user can rate
        testEvent = new Event();
        testEvent.setTitle("Test Event for Rating");
        testEvent.setLocation("Test Location");
        testEvent.setDescription("Test Description");
        testEvent.setCategory("Technical");
        testEvent.setDate("2026-06-15");
        testEvent.setRegistrationFee(100.0);
        testEvent = eventRepository.save(testEvent);
        
        // Create registration so user can rate the event
        createUserRegistration();
    }
    
    private void createUserRegistration() {
        Map<String, Object> registrationRequest = new HashMap<>();
        registrationRequest.put("userId", testUser.getId());
        registrationRequest.put("eventId", testEvent.getId());
        registrationRequest.put("userName", testUser.getName());
        registrationRequest.put("userEmail", testUser.getEmail());
        registrationRequest.put("paymentMethod", "ONLINE");
        
        ResponseEntity<Map<String, Object>> response = registrationController.registerForEvent(registrationRequest);
        assertTrue((Boolean) response.getBody().get("success"), 
            "User registration should be successful for rating test setup");
    }
    
    /**
     * Property 1: Bug Condition - Star Rating Interface Bug
     * 
     * This test implements the bug condition check:
     * isBugCondition(input) where input.action == "RATE_EVENT" 
     * AND NOT starRatingPopupFunctional
     * 
     * Expected outcome: Test FAILS on unfixed code (this proves the bug exists)
     * The test will show that star rating popup functionality is not available in dashboard context
     */
    @Property
    void testBugCondition_StarRatingInterfaceBug(@ForAll("ratingInteractionScenarios") RatingInteractionScenario scenario) {
        // ARRANGE: Verify user has events to rate
        List<Registration> userRegistrations = registrationRepository.findByUserId(testUser.getId());
        assertFalse(userRegistrations.isEmpty(), "User should have registrations to rate events");
        
        // ACT: Test dashboard.html for star rating interface availability
        try {
            String dashboardContent = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            
            // VERIFY: Check for star rating interface components in dashboard
            boolean hasRatingStars = dashboardContent.contains("rating-star") || 
                                   dashboardContent.contains("star-rating") ||
                                   dashboardContent.contains("⭐") && dashboardContent.contains("rating");
            
            boolean hasRatingPopup = dashboardContent.contains("rating-input") ||
                                   dashboardContent.contains("rating popup") ||
                                   dashboardContent.contains("selectedRating");
            
            boolean hasRatingEventHandlers = dashboardContent.contains("onclick") && 
                                           (dashboardContent.contains("rating") || dashboardContent.contains("star"));
            
            // Check if rating functionality is imported from review-board
            boolean hasRatingImport = dashboardContent.contains("review-board") && 
                                    dashboardContent.contains("rating");
            
            // ASSERT: This is the bug condition test - on unfixed code, this will FAIL
            // because the dashboard lacks star rating interface functionality
            assertTrue(hasRatingStars, 
                String.format("CRITICAL BUG: Star rating interface missing in dashboard! " +
                    "Dashboard should contain star rating elements for scenario: %s. " +
                    "Expected: rating-star elements or ⭐ with rating functionality. " +
                    "This confirms the bug exists - star rating interface is not available in dashboard context.",
                    scenario));
            
            assertTrue(hasRatingPopup, 
                String.format("CRITICAL BUG: Star rating popup functionality missing in dashboard! " +
                    "Dashboard should contain rating popup elements for scenario: %s. " +
                    "Expected: rating-input or rating popup functionality. " +
                    "This confirms the bug exists - no star rating popup when clicked.",
                    scenario));
            
            assertTrue(hasRatingEventHandlers, 
                String.format("CRITICAL BUG: Star rating event handlers missing in dashboard! " +
                    "Dashboard should contain rating event handlers for scenario: %s. " +
                    "Expected: onclick handlers for rating interactions. " +
                    "This confirms the bug exists - star rating interface doesn't respond to user interactions.",
                    scenario));
            
            // Additional check: Verify rating functionality is properly integrated
            boolean hasRatingFunctionality = hasRatingStars && hasRatingPopup && hasRatingEventHandlers;
            assertTrue(hasRatingFunctionality, 
                String.format("CRITICAL BUG: Complete star rating functionality missing in dashboard! " +
                    "Dashboard should have integrated star rating interface for scenario: %s. " +
                    "Missing components: Stars=%s, Popup=%s, Handlers=%s, Import=%s. " +
                    "This confirms the bug exists - star rating interface is non-functional in dashboard context.",
                    scenario, hasRatingStars, hasRatingPopup, hasRatingEventHandlers, hasRatingImport));
            
        } catch (Exception e) {
            fail("Failed to test dashboard for star rating interface: " + e.getMessage());
        }
    }
    
    /**
     * Unit test version of the bug condition for clearer failure reporting
     */
    @Test
    void testBugCondition_StarRatingInterface_DashboardContext() {
        // ARRANGE: Verify test setup
        List<Registration> userRegistrations = registrationRepository.findByUserId(testUser.getId());
        assertEquals(1, userRegistrations.size(), "User should have exactly one registration for testing");
        
        System.out.println("=== STAR RATING INTERFACE BUG TEST ===");
        System.out.println("User ID: " + testUser.getId());
        System.out.println("User registrations: " + userRegistrations.size());
        System.out.println("Test event: " + testEvent.getTitle());
        
        // ACT: Test dashboard.html for star rating interface
        try {
            String dashboardContent = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            
            // Debug: Check what rating-related content exists
            boolean hasAnyRatingContent = dashboardContent.toLowerCase().contains("rating") ||
                                        dashboardContent.contains("⭐") ||
                                        dashboardContent.contains("star");
            
            System.out.println("Dashboard has any rating content: " + hasAnyRatingContent);
            
            // VERIFY: Check for specific star rating interface components
            boolean hasRatingStarClass = dashboardContent.contains("rating-star");
            boolean hasRatingInputClass = dashboardContent.contains("rating-input");
            boolean hasStarEmoji = dashboardContent.contains("⭐");
            boolean hasRatingPopupLogic = dashboardContent.contains("selectedRating") || 
                                        dashboardContent.contains("updateRatingDisplay");
            boolean hasRatingEventListener = dashboardContent.contains("addEventListener") && 
                                           dashboardContent.contains("rating");
            
            System.out.println("Rating star class: " + hasRatingStarClass);
            System.out.println("Rating input class: " + hasRatingInputClass);
            System.out.println("Star emoji: " + hasStarEmoji);
            System.out.println("Rating popup logic: " + hasRatingPopupLogic);
            System.out.println("Rating event listener: " + hasRatingEventListener);
            
            // Check if review-board functionality is imported
            boolean hasReviewBoardImport = dashboardContent.contains("review-board.html") ||
                                         dashboardContent.contains("openReviewBoard");
            
            System.out.println("Review board import/link: " + hasReviewBoardImport);
            
            // CRITICAL BUG CHECKS: These should fail on unfixed code
            
            // Check 1: Star rating interface elements
            if (!hasRatingStarClass && !hasRatingInputClass) {
                fail("STAR RATING INTERFACE BUG CONFIRMED! " +
                    "Dashboard.html is missing star rating interface elements. " +
                    "Expected: .rating-star or .rating-input classes for star selection. " +
                    "Found: Neither rating-star nor rating-input classes exist in dashboard. " +
                    "This confirms the bug - no star rating interface in dashboard context.");
            }
            
            // Check 2: Star rating popup functionality
            if (!hasRatingPopupLogic) {
                fail("STAR RATING POPUP BUG CONFIRMED! " +
                    "Dashboard.html is missing star rating popup functionality. " +
                    "Expected: selectedRating variable or updateRatingDisplay function for popup behavior. " +
                    "Found: No rating popup logic in dashboard JavaScript. " +
                    "This confirms the bug - star rating doesn't show popup when clicked.");
            }
            
            // Check 3: Star rating event handlers
            if (!hasRatingEventListener && !dashboardContent.contains("onclick") && !hasRatingPopupLogic) {
                fail("STAR RATING EVENT HANDLER BUG CONFIRMED! " +
                    "Dashboard.html is missing star rating event handlers. " +
                    "Expected: Event listeners or onclick handlers for rating interactions. " +
                    "Found: No rating event handling in dashboard. " +
                    "This confirms the bug - star rating interface doesn't respond to user interactions.");
            }
            
            // Check 4: Integration with rating functionality
            boolean hasIntegratedRatingFunctionality = hasRatingStarClass && hasRatingPopupLogic && 
                                                     (hasRatingEventListener || dashboardContent.contains("onclick"));
            
            if (!hasIntegratedRatingFunctionality) {
                fail("STAR RATING INTEGRATION BUG CONFIRMED! " +
                    "Dashboard.html lacks integrated star rating functionality. " +
                    "Components found: Stars=" + hasRatingStarClass + 
                    ", Popup=" + hasRatingPopupLogic + 
                    ", Events=" + hasRatingEventListener + ". " +
                    "This confirms the bug - star rating interface is non-functional in dashboard context. " +
                    "Users cannot rate events from the main dashboard.");
            }
            
            // If we reach here, the star rating interface is working (test should pass after fix)
            assertTrue(hasIntegratedRatingFunctionality, 
                "Star rating interface should be fully functional in dashboard context");
            
        } catch (Exception e) {
            fail("Failed to test dashboard for star rating interface: " + e.getMessage());
        }
    }
    
    /**
     * Test the specific bug scenario: Star rating interface should start empty and show popup when clicked
     */
    @Test
    void testBugCondition_StarRatingStartsEmptyAndShowsPopup() {
        // ARRANGE: Verify user can rate events
        List<Registration> userRegistrations = registrationRepository.findByUserId(testUser.getId());
        assertFalse(userRegistrations.isEmpty(), "User should have events to rate");
        
        System.out.println("=== STAR RATING POPUP BEHAVIOR TEST ===");
        System.out.println("Testing that star rating starts empty and shows popup when clicked");
        
        // ACT: Check dashboard for proper star rating behavior
        try {
            String dashboardContent = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            
            // VERIFY: Check for empty star rating initial state
            boolean hasEmptyStarState = dashboardContent.contains("star.empty") ||
                                      dashboardContent.contains("selectedRating = 0") ||
                                      dashboardContent.contains("rating: 0");
            
            // Check for popup show functionality
            boolean hasPopupShowLogic = dashboardContent.contains("popup") && 
                                      (dashboardContent.contains("show") || dashboardContent.contains("display"));
            
            // Check for click event handling on stars
            boolean hasStarClickHandling = (dashboardContent.contains("click") || dashboardContent.contains("onclick")) &&
                                         dashboardContent.contains("star");
            
            System.out.println("Empty star state: " + hasEmptyStarState);
            System.out.println("Popup show logic: " + hasPopupShowLogic);
            System.out.println("Star click handling: " + hasStarClickHandling);
            
            // CRITICAL BUG CHECKS for the specific behavior described in requirements
            
            if (!hasEmptyStarState) {
                fail("STAR RATING EMPTY STATE BUG CONFIRMED! " +
                    "Dashboard star rating interface should start empty (no stars selected). " +
                    "Expected: star.empty class or selectedRating = 0 initialization. " +
                    "Found: No empty star state initialization in dashboard. " +
                    "This confirms the bug - star rating doesn't start empty as required.");
            }
            
            if (!hasPopupShowLogic || !hasStarClickHandling) {
                fail("STAR RATING POPUP CLICK BUG CONFIRMED! " +
                    "Dashboard star rating should show popup when clicked. " +
                    "Expected: Popup display logic + star click event handling. " +
                    "Found: PopupLogic=" + hasPopupShowLogic + ", ClickHandling=" + hasStarClickHandling + ". " +
                    "This confirms the bug - star rating doesn't show popup when clicked.");
            }
            
            // If we reach here, the star rating behavior is correct (test should pass after fix)
            assertTrue(hasEmptyStarState && hasPopupShowLogic && hasStarClickHandling,
                "Star rating should start empty and show popup when clicked");
            
        } catch (Exception e) {
            fail("Failed to test star rating popup behavior: " + e.getMessage());
        }
    }
    
    /**
     * Test that verifies star rating is available in dashboard context, not just review-board
     */
    @Test
    void testBugCondition_StarRatingAvailableInDashboardNotJustReviewBoard() {
        System.out.println("=== STAR RATING DASHBOARD CONTEXT TEST ===");
        System.out.println("Testing that star rating is available in dashboard, not just review-board");
        
        try {
            // Get both dashboard and review-board content
            String dashboardContent = mockMvc.perform(get("/dashboard.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            
            String reviewBoardContent = mockMvc.perform(get("/review-board.html"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
            
            // Check if review-board has star rating (it should)
            boolean reviewBoardHasRating = reviewBoardContent.contains("rating-star") &&
                                         reviewBoardContent.contains("selectedRating");
            
            // Check if dashboard has star rating (this is what's missing - the bug)
            boolean dashboardHasRating = dashboardContent.contains("rating-star") &&
                                       dashboardContent.contains("selectedRating");
            
            System.out.println("Review-board has star rating: " + reviewBoardHasRating);
            System.out.println("Dashboard has star rating: " + dashboardHasRating);
            
            // Verify review-board has the functionality (sanity check)
            assertTrue(reviewBoardHasRating, 
                "Review-board should have star rating functionality (sanity check)");
            
            // CRITICAL BUG CHECK: Dashboard should also have star rating functionality
            if (!dashboardHasRating) {
                fail("STAR RATING DASHBOARD CONTEXT BUG CONFIRMED! " +
                    "Star rating functionality exists in review-board.html but is missing from dashboard.html. " +
                    "Users should be able to rate events from the main dashboard, not just the review board. " +
                    "Expected: Star rating interface integrated into dashboard context. " +
                    "Found: Star rating only available in separate review-board page. " +
                    "This confirms the bug - star rating interface is not available in dashboard context.");
            }
            
            // If we reach here, dashboard has star rating functionality (test should pass after fix)
            assertTrue(dashboardHasRating,
                "Dashboard should have star rating functionality, not just review-board");
            
        } catch (Exception e) {
            fail("Failed to compare dashboard and review-board star rating functionality: " + e.getMessage());
        }
    }
    
    /**
     * Generator for rating interaction scenarios
     */
    @Provide
    Arbitrary<RatingInteractionScenario> ratingInteractionScenarios() {
        return Arbitraries.of(
            new RatingInteractionScenario("click_empty_stars", "User clicks on empty star rating"),
            new RatingInteractionScenario("hover_stars", "User hovers over star rating"),
            new RatingInteractionScenario("select_rating", "User selects a star rating"),
            new RatingInteractionScenario("popup_display", "Star rating popup should display"),
            new RatingInteractionScenario("rating_submission", "User submits star rating")
        );
    }
    
    /**
     * Test scenario for rating interactions
     */
    static class RatingInteractionScenario {
        final String interactionType;
        final String description;
        
        RatingInteractionScenario(String interactionType, String description) {
            this.interactionType = interactionType;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return String.format("RatingInteractionScenario{type='%s', desc='%s'}", 
                interactionType, description);
        }
    }
}