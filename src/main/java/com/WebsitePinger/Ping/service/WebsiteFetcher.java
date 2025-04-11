package com.WebsitePinger.Ping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class WebsiteFetcher {

    private static final Logger log = LoggerFactory.getLogger(WebsiteFetcher.class);
    private final WebClient webClient;

    public WebsiteFetcher(WebClient.Builder webClientBuilder) {
        // Configure WebClient - you might want to customize timeouts, headers etc.
        this.webClient = webClientBuilder.baseUrl("").build(); // Base URL set per request
    }

    /**
     * Fetches the content of the given URL.
     *
     * @param url The URL to fetch.
     * @return A Mono emitting the website content as a String, or an empty Mono if an error occurs.
     */
    public Mono<String> fetchContent(String url) {
        log.debug("Fetching content for URL: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve() // Retrieve the response body
                .bodyToMono(String.class) // Convert body to String
                .timeout(Duration.ofSeconds(10)) // Add a timeout
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(5)) // Basic retry logic
                        .filter(throwable -> {
                            // Add specific exceptions to retry on if needed
                            log.warn("Retrying fetch for {} due to error: {}", url, throwable.getMessage());
                            return true; // Retry on most errors for this example
                        }))
                .doOnError(error -> log.error("Error fetching URL {}: {}", url, error.getMessage()))
                .onErrorResume(error -> {
                    // Handle specific errors differently if needed
                    // For now, return empty Mono on any error after retries
                    return Mono.empty();
                });
    }
}
