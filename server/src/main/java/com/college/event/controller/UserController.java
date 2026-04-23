package com.college.event.controller;

import com.college.event.model.User;
import com.college.event.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Admin whitelist - only these emails can login as admin
    private static final Set<String> ADMIN_WHITELIST = new HashSet<>() {{
        add("hemishaanghan1@gmail.com");
        add("hemianghan@gmail.com");
        add("jaymingoti@gmail.com");
    }};

    // Get admin whitelist
    @GetMapping("/admin-whitelist")
    public Set<String> getAdminWhitelist() {
        return new HashSet<>(ADMIN_WHITELIST);
    }

    // Add email to admin whitelist
    @PostMapping("/admin-whitelist")
    public String addToAdminWhitelist(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "Error: Email is required";
        }
        
        email = email.toLowerCase().trim();
        
        if (ADMIN_WHITELIST.contains(email)) {
            return "Error: Email already in admin whitelist";
        }
        
        ADMIN_WHITELIST.add(email);
        return "Success: Email added to admin whitelist";
    }

    // Remove email from admin whitelist
    @DeleteMapping("/admin-whitelist/{email}")
    public String removeFromAdminWhitelist(@PathVariable String email) {
        email = email.toLowerCase().trim();
        
        if (!ADMIN_WHITELIST.contains(email)) {
            return "Error: Email not found in admin whitelist";
        }
        
        // Prevent removing all admins
        if (ADMIN_WHITELIST.size() <= 1) {
            return "Error: Cannot remove the last admin email";
        }
        
        ADMIN_WHITELIST.remove(email);
        return "Success: Email removed from admin whitelist";
    }

    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody User loginUser) {
        Optional<User> userOpt = userRepository.findByEmail(loginUser.getEmail());
        User user;
        
        // Check if user is trying to login as admin
        if ("Admin".equals(loginUser.getRole())) {
            // Verify email is in admin whitelist
            if (!ADMIN_WHITELIST.contains(loginUser.getEmail().toLowerCase())) {
                return "Error: You are not authorized to login as admin";
            }
        }
        
        if (userOpt.isPresent()) {
            user = userOpt.get();
            // User exists - update password if provided and update role
            if (loginUser.getPassword() != null && !loginUser.getPassword().isEmpty()) {
                user.setPassword(loginUser.getPassword());
            }
            // Update role if changed during login (only if authorized)
            if (loginUser.getRole() != null) {
                if ("Admin".equals(loginUser.getRole()) && !ADMIN_WHITELIST.contains(loginUser.getEmail().toLowerCase())) {
                    return "Error: You are not authorized to login as admin";
                }
                user.setRole(loginUser.getRole());
            }
        } else {
            // Auto-register new user
            user = new User();
            user.setEmail(loginUser.getEmail());
            user.setPassword(loginUser.getPassword());
            
            // Set role based on whitelist
            if ("Admin".equals(loginUser.getRole())) {
                if (!ADMIN_WHITELIST.contains(loginUser.getEmail().toLowerCase())) {
                    return "Error: You are not authorized to login as admin";
                }
                user.setRole("Admin");
            } else {
                user.setRole("Student");
            }
            
            user.setName(loginUser.getName() != null ? loginUser.getName() : "User");
        }

        String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
        user.setOtp(otp);
        userRepository.save(user);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("hemishaanghan1@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("Event Portal Login OTP");
            message.setText("Your OTP for login is: " + otp + "\n\nWelcome to Event Portal!");
            mailSender.send(message);
            return "Success: OTP sent to " + user.getEmail();
        } catch (Exception e) {
            return "Error: Could not send email. " + e.getMessage();
        }
    }

    @PostMapping("/verify-otp")
    public User verifyOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                user.setOtp(null); // Clear OTP after success
                userRepository.save(user);
                return user;
            }
        }
        throw new RuntimeException("Invalid OTP");
    }
    
    // Endpoint to check if email is admin
    @GetMapping("/is-admin/{email}")
    public Map<String, Boolean> isAdmin(@PathVariable String email) {
        boolean isAdmin = ADMIN_WHITELIST.contains(email.toLowerCase());
        return Map.of("isAdmin", isAdmin);
    }

    /**
     * @deprecated Use /send-otp and /verify-otp instead
     */
    @Deprecated
    @PostMapping("/login")
    public User login(@RequestBody User loginUser) {
        Optional<User> user = userRepository.findByEmail(loginUser.getEmail());
        if (user.isPresent() && user.get().getPassword().equals(loginUser.getPassword())) {
            return user.get();
        }
        throw new RuntimeException("Invalid credentials");
    }

    /**
     * @deprecated Use /send-otp (which auto-registers) instead
     */
    @Deprecated
    @PostMapping("/signup")
    public User signup(@RequestBody User newUser) {
        return userRepository.save(newUser);
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.isEmpty()) {
            return "Error: Email is required";
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
            user.setOtp(otp);
            userRepository.save(user);

            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("hemishaanghan1@gmail.com");
                message.setTo(email);
                message.setSubject("EventHub Password Reset OTP");
                message.setText("Hello " + user.getName() + ",\n\nYour OTP is: " + otp);
                mailSender.send(message);
                return "Success: OTP sent to " + email;
            } catch (Exception e) {
                e.printStackTrace(); // Log the error to console
                return "Error: SMTP failure. " + e.getMessage();
            }
        }
        return "Error: User with email " + email + " not found. Please sign up first.";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getOtp() != null && user.getOtp().equals(otp)) {
                user.setPassword(newPassword);
                user.setOtp(null); 
                userRepository.save(user);
                return "Password reset successfully";
            }
        }
        throw new RuntimeException("Invalid OTP or Email");
    }

    // Get all users (for notification system)
    @GetMapping
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PutMapping("/update")
    public User update(@RequestBody User updatedUser) {
        Optional<User> userOpt = userRepository.findById(updatedUser.getId());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(updatedUser.getName());
            user.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                user.setPassword(updatedUser.getPassword());
            }
            return userRepository.save(user);
        }
        throw new RuntimeException("User not found");
    }
}
