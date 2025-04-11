package com.WebsitePinger.Ping.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
// import org.jsoup.safety.Safelist; // Safelist not used in current cleaning logic
import org.slf4j.Logger; // Keep only one Logger import
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ContentComparator {

    private static final Logger log = LoggerFactory.getLogger(ContentComparator.class);
    // Stores the last known content for each monitored URL
    private final ConcurrentMap<String, String> lastKnownContent = new ConcurrentHashMap<>();

    /**
     * Compares the new content with the last known content for the given URL.
     * Updates the stored content if it's the first check or if changes are detected.
     *
     * @param url        The URL being checked.
     * @param newContent The newly fetched content.
     * @return A ComparisonResult indicating if a change occurred and including previous/new content.
     */
    public ComparisonResult compareAndStore(String url, String newContent) {
        if (newContent == null) {
            log.warn("Received null content for URL: {}. Treating as no change from last known state.", url);
            // Return no change, keeping the existing previous content if available
            String previousContent = lastKnownContent.get(url);
            // Still return no change if new content is null, using the last known original content
            String originalPreviousContent = lastKnownContent.get(url);
            return ComparisonResult.noChange(originalPreviousContent != null ? originalPreviousContent : "");
        }

        String originalPreviousContent = lastKnownContent.get(url);

        // Clean the new content for comparison
        String cleanedNewContent = cleanHtml(newContent);

        if (originalPreviousContent == null) {
            log.info("First check for URL: {}. Storing initial original content.", url);
            lastKnownContent.put(url, newContent); // Store the original new content
            // Return initial state with original content
            return ComparisonResult.initial(newContent);
        }

        // Clean the previous content for comparison
        String cleanedPreviousContent = cleanHtml(originalPreviousContent);

        // Compare the CLEANED versions
        if (!Objects.equals(cleanedPreviousContent, cleanedNewContent)) {
            log.info("Change detected for URL {} based on cleaned content comparison.", url);
            lastKnownContent.put(url, newContent); // Store the new ORIGINAL content
            // Return result with ORIGINAL contents for notification
            return ComparisonResult.changed(originalPreviousContent, newContent);
        } else {
            log.debug("No change detected for URL {} based on cleaned content comparison.", url);
            // Return no change result with the new ORIGINAL content
            return ComparisonResult.noChange(newContent);
        }
    }

    /**
     * Cleans HTML content to remove potentially dynamic elements before comparison.
     * Currently removes script tags and normalizes whitespace.
     *
     * @param html The HTML content to clean.
     * @return The cleaned HTML content as a String.
     */
    private String cleanHtml(String html) {
        if (html == null) {
            return "";
        }
        try {
            // More robust cleaning: Parse, remove dynamic elements, get body HTML, normalize whitespace
            Document doc = Jsoup.parse(html);
            doc.select("script").remove(); // Remove script tags
            doc.select("input[type=hidden]").remove(); // Remove hidden form fields

            // Correct way to remove comments: Traverse and remove comment nodes
            doc.traverse(new org.jsoup.select.NodeVisitor() {
                @Override
                public void head(org.jsoup.nodes.Node node, int depth) {
                    if (node instanceof org.jsoup.nodes.Comment) {
                        node.remove();
                    }
                }
                @Override
                public void tail(org.jsoup.nodes.Node node, int depth) {
                    // Not needed for removal
                }
            });

            // Consider removing style tags if needed: doc.select("style").remove();

            // --- AGGRESSIVE CLEANING: Extract only text content from the body ---
            String cleanedText = doc.body().text();

            // Normalize whitespace within the extracted text
            return cleanedText.replaceAll("\\s+", " ").trim();

        } catch (Exception e) {
            log.error("Error cleaning HTML, returning original: {}", e.getMessage());
            return html; // Fallback to original if cleaning fails
        }
    }

    /**
     * Clears the stored state for a specific URL.
     * Useful when monitoring for a URL is stopped.
     * @param url The URL to remove from tracking.
     */
    public void clearState(String url) {
        String removedContent = lastKnownContent.remove(url);
        if (removedContent != null) {
            log.info("Cleared stored state for URL: {}", url);
        }
    }

     /**
     * Gets the last known content for a URL. Primarily for testing or debugging.
     * @param url The URL.
     * @return The last known content, or null if not tracked.
     */
    public String getLastKnownContent(String url) {
        return lastKnownContent.get(url);
    }
}
