package com.WebsitePinger.Ping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service // Mark this as the primary implementation for now
public class ConsoleNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleNotificationService.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final int SNIPPET_LENGTH = 100; // Max length for showing content snippets

    @Override
    public void notifyChange(String url, LocalDateTime detectionTime, String previousContent, String newContent) {
        log.warn("--- CHANGE DETECTED ---");
        log.warn("URL: {}", url);
        log.warn("Time: {}", detectionTime.format(formatter));

        // Provide some context about the change
        int prevLength = (previousContent != null) ? previousContent.length() : 0;
        int newLength = (newContent != null) ? newContent.length() : 0;

        log.warn("Content length changed from {} to {} characters.", prevLength, newLength);

        // Optional: Show snippets (can be noisy for large pages)
        // if (previousContent != null) {
        //     log.warn("Previous Snippet (first {} chars): {}", SNIPPET_LENGTH, previousContent.substring(0, Math.min(prevLength, SNIPPET_LENGTH)));
        // } else {
        //     log.warn("Previous Snippet: [N/A - First Check]");
        // }
        // if (newContent != null) {
        //     log.warn("New Snippet (first {} chars): {}", SNIPPET_LENGTH, newContent.substring(0, Math.min(newLength, SNIPPET_LENGTH)));
        // } else {
        //      log.warn("New Snippet: [N/A - Content Fetch Failed?]"); // Should ideally not happen if change detected
        // }


        // TODO: Implement a more sophisticated diff mechanism if needed
        // Libraries like java-diff-utils can generate textual diffs.

        log.warn("-----------------------");
    }

    @Override
    public void notifyError(String url, LocalDateTime errorTime, String errorMessage) {
        log.error("--- MONITORING ERROR ---");
        log.error("URL: {}", url);
        log.error("Time: {}", errorTime.format(formatter));
        log.error("Error: {}", errorMessage);
        log.error("------------------------");
        // Handle errors appropriately, maybe different notification channel
    }
}
