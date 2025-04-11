package com.WebsitePinger.Ping.service;

import java.time.LocalDateTime;

/**
 * Interface for sending notifications about website changes.
 */
public interface NotificationService {

    /**
     * Sends a notification about a detected change.
     *
     * @param url             The URL where the change was detected.
     * @param detectionTime   The time the change was detected.
     * @param previousContent The content before the change (can be null on first detection).
     * @param newContent      The content after the change.
     */
    void notifyChange(String url, LocalDateTime detectionTime, String previousContent, String newContent);

    /**
     * Sends a notification about an error during monitoring.
     *
     * @param url           The URL being monitored when the error occurred.
     * @param errorTime     The time the error occurred.
     * @param errorMessage  A description of the error.
     */
    void notifyError(String url, LocalDateTime errorTime, String errorMessage);
}
