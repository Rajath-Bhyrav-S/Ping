package com.WebsitePinger.Ping.service;

/**
 * Holds the result of a content comparison.
 */
public record ComparisonResult(boolean changed, String previousContent, String newContent) {

    /**
     * Static factory for a "no change" result.
     */
    public static ComparisonResult noChange(String currentContent) {
        return new ComparisonResult(false, currentContent, currentContent);
    }

    /**
     * Static factory for a "change detected" result.
     */
    public static ComparisonResult changed(String previousContent, String newContent) {
        return new ComparisonResult(true, previousContent, newContent);
    }

     /**
     * Static factory for the initial state (no previous content).
     * Treat initial fetch as unchanged for notification purposes by default.
     */
    public static ComparisonResult initial(String newContent) {
        // Return changed=false so the first fetch doesn't trigger a "change" notification
        return new ComparisonResult(false, null, newContent);
        // If you want the first fetch to trigger a notification, use:
        // return new ComparisonResult(true, null, newContent);
    }
}
