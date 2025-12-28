package com.casper.rp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving HTML pages.
 */
@Controller
public class WebController {
    
    /**
     * Serve the main demo page.
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }
    
    /**
     * Serve breach detection demo page.
     */
    @GetMapping("/breach")
    public String breach() {
        return "breach";
    }
}

