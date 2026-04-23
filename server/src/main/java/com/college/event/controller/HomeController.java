package com.college.event.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class HomeController {
    
    @GetMapping("/")
    public void home(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login.html");
    }
    
    @GetMapping("/index.html")
    public void index(HttpServletResponse response) throws IOException {
        response.sendRedirect("/login.html");
    }
}
