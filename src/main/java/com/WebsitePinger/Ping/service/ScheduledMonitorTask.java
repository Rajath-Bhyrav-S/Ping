package com.WebsitePinger.Ping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set; // Added import
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ScheduledMonitorTask {

    private static final Logger log = LoggerFactory.getLogger(ScheduledMonitorTask.class);

    private final WebsiteFetcher websiteFetcher;
    private final ContentComparator contentComparator;
    private final NotificationService notificationService;

    // Using a map to potentially monitor multiple URLs in the future
    // Key: URL, Value: Boolean (indicates if monitoring is active)
    private final ConcurrentMap<String, Boolean> monitoredUrls = new ConcurrentHashMap<>();

    @Autowired
    public ScheduledMonitorTask(WebsiteFetcher websiteFetcher, ContentComparator contentComparator, NotificationService notificationService) {
        this.websiteFetcher = websiteFetcher;
        this.contentComparator = contentComparator;
        this.notificationService = notificationService;
        // Set the default URL to monitor based on user request
        startMonitoring("https://eresultsglobal.contineo.in/"); // User specified URL
    }

    // Method to add a URL to monitor (will be used by UI/API later)
    public void startMonitoring(String url) {
        log.info("Adding URL to monitor: {}", url);
        monitoredUrls.put(url, true);
        // Optionally perform an initial fetch immediately?
    }

    // Method to stop monitoring a URL
    public void stopMonitoring(String url) {
        log.info("Stopping monitoring for URL: {}", url);
        monitoredUrls.remove(url);
        contentComparator.clearState(url); // Clear stored state
    }

    // Fixed rate scheduler: runs every 2000 milliseconds (2 seconds)
    // Note: Using fixedRate means the next task starts 2 seconds after the *start*
    // of the previous one. If tasks take longer than 2s, they might overlap.
    // Consider fixedDelay if you want the interval *between* the end of one task and the start of the next.
    // Use fixedRateString to allow configuration via application.properties
    @Scheduled(fixedRateString = "${monitor.check.interval.ms:2000}") // Default to 2000ms if property not found
    public void checkWebsites() {
        if (monitoredUrls.isEmpty()) {
            // log.trace("No URLs currently being monitored."); // Can be noisy
            return;
        }

        log.debug("Running scheduled website check for {} URLs.", monitoredUrls.size());

        monitoredUrls.keySet().forEach(url -> {
            log.debug("Checking URL: {}", url);
            websiteFetcher.fetchContent(url)
                .subscribe(newContent -> { // Process the fetched content asynchronously
                    // newContent can be null if fetcher failed and returned Mono.empty()
                    ComparisonResult result = contentComparator.compareAndStore(url, newContent);

                    // Log the outcome of the check for this cycle
                    if (result.changed()) {
                        // Notification service already logs the details for changes
                        notificationService.notifyChange(url, LocalDateTime.now(), result.previousContent(), result.newContent());
                        log.info("Check completed for URL [{}]: Change DETECTED.", url); // Add summary log
                    } else if (newContent == null && result.previousContent() == null) {
                         // Handle case where fetch failed on the very first check
                        log.warn("Check completed for URL [{}]: Fetch failed (null content) on initial check.", url);
                        // Optionally notify error
                        // notificationService.notifyError(url, LocalDateTime.now(), "Initial fetch failed (null content).");
                    } else if (newContent == null) {
                        // Handle case where fetch failed but we have previous content (no change reported by comparator)
                        log.warn("Check completed for URL [{}]: Fetch failed (null content), no change detected from last known state.", url);
                         // Optionally notify error
                        // notificationService.notifyError(url, LocalDateTime.now(), "Fetch failed (null content).");
                    } else {
                        // This means result.changed() is false and newContent is not null
                        log.info("Check completed for URL [{}]: No change detected.", url);
                    }
                }, error -> {
                    // Handle errors that might occur during the subscription processing itself
                    log.error("Check failed for URL [{}] due to subscription processing error: {}", url, error.getMessage()); // Keep this summary log
                    // The notification service will log the detailed error
                    notificationService.notifyError(url, LocalDateTime.now(), "Subscription processing error: " + error.getMessage());
                });
        });
    }

    /**
     * Returns the set of currently monitored URLs.
     * Primarily for testing or status endpoints.
     * @return A set of monitored URLs.
     */
    public Set<String> getMonitoredUrls() {
        // Return an immutable copy to prevent external modification
        return Set.copyOf(monitoredUrls.keySet());
    }
}
