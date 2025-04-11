package com.WebsitePinger.Ping;

import com.WebsitePinger.Ping.service.ScheduledMonitorTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Enable and configure MockMvc
class PingApplicationTests {

	@Autowired
	private MockMvc mockMvc; // Inject MockMvc for sending HTTP requests

	@Autowired
	private ScheduledMonitorTask monitorTask; // Inject the task to check its state

	private static final String TEST_URL_1 = "https://httpbin.org/get"; // Use a real, stable URL for testing
	private static final String TEST_URL_2 = "https://httpbin.org/delay/1"; // Another test URL

	@BeforeEach
	void setUp() {
		// Ensure a clean state before each test by removing test URLs
		// Note: The default "https://example.com" added in the constructor will remain unless explicitly removed.
		monitorTask.stopMonitoring(TEST_URL_1);
		monitorTask.stopMonitoring(TEST_URL_2);
	}

	@Test
	void contextLoads() {
		// Basic test to ensure the application context loads
		assertNotNull(monitorTask);
		assertNotNull(mockMvc);
	}

	@Test
	void testStartMonitoringEndpoint() throws Exception {
		// Initially, the test URL should not be monitored (unless it's the default one)
		assertFalse(monitorTask.getMonitoredUrls().contains(TEST_URL_1), "URL should not be monitored initially");

		// Perform POST request to start monitoring
		mockMvc.perform(post("/api/monitor").param("url", TEST_URL_1))
				.andExpect(status().isOk()) // Expect HTTP 200 OK
				.andExpect(content().string(containsString("Started monitoring URL: " + TEST_URL_1))); // Expect success message

		// Verify that the URL is now in the monitored list
		assertTrue(monitorTask.getMonitoredUrls().contains(TEST_URL_1), "URL should be monitored after POST request");
	}

	@Test
	void testStopMonitoringEndpoint() throws Exception {
		// First, start monitoring the URL
		monitorTask.startMonitoring(TEST_URL_2);
		assertTrue(monitorTask.getMonitoredUrls().contains(TEST_URL_2), "URL should be monitored before DELETE request");

		// Perform DELETE request to stop monitoring
		mockMvc.perform(delete("/api/monitor").param("url", TEST_URL_2))
				.andExpect(status().isOk()) // Expect HTTP 200 OK
				.andExpect(content().string(containsString("Stopped monitoring URL: " + TEST_URL_2))); // Expect success message

		// Verify that the URL is no longer in the monitored list
		assertFalse(monitorTask.getMonitoredUrls().contains(TEST_URL_2), "URL should not be monitored after DELETE request");
	}

	@Test
	void testStartMonitoringEndpoint_BadRequest_NoUrl() throws Exception {
		mockMvc.perform(post("/api/monitor")) // No 'url' parameter
				.andExpect(status().isBadRequest()); // Expect HTTP 400 Bad Request
	}

	@Test
	void testStopMonitoringEndpoint_BadRequest_NoUrl() throws Exception {
		mockMvc.perform(delete("/api/monitor")) // No 'url' parameter
				.andExpect(status().isBadRequest()); // Expect HTTP 400 Bad Request
	}

	@Test
	void testStartMonitoringEndpoint_BadRequest_EmptyUrl() throws Exception {
		mockMvc.perform(post("/api/monitor").param("url", "")) // Empty 'url' parameter
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("URL parameter is required.")));
	}

	@Test
	void testStopMonitoringEndpoint_BadRequest_EmptyUrl() throws Exception {
		mockMvc.perform(delete("/api/monitor").param("url", "")) // Empty 'url' parameter
				.andExpect(status().isBadRequest())
				.andExpect(content().string(containsString("URL parameter is required.")));
	}
}
