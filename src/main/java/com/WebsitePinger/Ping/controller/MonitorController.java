package com.WebsitePinger.Ping.controller;

import com.WebsitePinger.Ping.service.ScheduledMonitorTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set; // Import needed for return type if we add status endpoint

@RestController
@RequestMapping("/api/monitor") // Base path for monitoring endpoints
public class MonitorController {

    private static final Logger log = LoggerFactory.getLogger(MonitorController.class);
    private final ScheduledMonitorTask monitorTask;

    @Autowired
    public MonitorController(ScheduledMonitorTask monitorTask) {
        this.monitorTask = monitorTask;
    }

    /**
     * Starts monitoring the specified URL.
     * Example: POST /api/monitor?url=https://example.com
     *
     * @param url The URL to monitor.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping
    public ResponseEntity<String> startMonitoring(@RequestParam String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("URL parameter is required.");
        }
        try {
            // Basic URL validation could be added here
            log.info("Received request to start monitoring URL: {}", url);
            monitorTask.startMonitoring(url);
            return ResponseEntity.ok("Started monitoring URL: " + url);
        } catch (Exception e) {
            log.error("Error starting monitoring for URL {}: {}", url, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error starting monitoring: " + e.getMessage());
        }
    }

    /**
     * Stops monitoring the specified URL.
     * Example: DELETE /api/monitor?url=https://example.com
     *
     * @param url The URL to stop monitoring.
     * @return ResponseEntity indicating success or failure.
     */
    @DeleteMapping
    public ResponseEntity<String> stopMonitoring(@RequestParam String url) {
        if (url == null || url.isBlank()) {
            return ResponseEntity.badRequest().body("URL parameter is required.");
        }
        try {
            log.info("Received request to stop monitoring URL: {}", url);
            monitorTask.stopMonitoring(url);
            return ResponseEntity.ok("Stopped monitoring URL: " + url);
        } catch (Exception e) {
            log.error("Error stopping monitoring for URL {}: {}", url, e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error stopping monitoring: " + e.getMessage());
        }
    }

    // TODO: Add endpoint to view status (e.g., GET /api/monitor/status)
    // This would require ScheduledMonitorTask to expose the list of monitoredUrls.
    /*
    @GetMapping("/status")
    public ResponseEntity<Set<String>> getStatus() {
        // Need to modify ScheduledMonitorTask to return monitoredUrls.keySet()
        // return ResponseEntity.ok(monitorTask.getMonitoredUrls());
        return ResponseEntity.ok(Set.of("Status endpoint not fully implemented yet."));
    }
    */
}
