package com.service.hashTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.hashTable.entity.Storage;
import com.service.hashTable.service.HashTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HashTableApplicationTests {
	private static final String FILE_NAME = "dump.json";
	private HashTableService hashTableService;

	@BeforeEach
	void setUp() {
		hashTableService = new HashTableService();
	}

	@Test
	void testSetAndGet() {
		hashTableService.set("key", "value", null);
		assertEquals("value", hashTableService.get("key"));
	}

	@Test
	void testRemove() {
		hashTableService.set("key", "value", null);
		assertEquals("value", hashTableService.remove("key"));
		assertNull(hashTableService.get("key"));
	}

	@Test
	void testTTLExpiry() throws InterruptedException {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		hashTableService.set("key", "value", 1000L);
		scheduler.schedule(hashTableService::removeExpiredRecords, 1500L, TimeUnit.MILLISECONDS);

		scheduler.shutdown();
		boolean terminated = scheduler.awaitTermination(2, TimeUnit.SECONDS);

		if (!terminated) {
			fail("Scheduler did not terminate in the expected time");
		}

		assertNull(hashTableService.get("key"));
	}

	@Test
	void testDump() throws IOException {
		hashTableService.set("key", "value", 10000L);

		boolean result = hashTableService.dump();

		assertTrue(result);
		File file = new File(FILE_NAME);
		assertTrue(file.exists());
	}

	@Test
	void testLoad() throws IOException {
		Map<String, Storage> map = new HashMap<>();
		map.put("key", new Storage("value", System.currentTimeMillis() + 10000L));

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonData = objectMapper.writeValueAsString(map);

		MultipartFile mockFile = new MockMultipartFile("file", "dump.json",
				"application/json", jsonData.getBytes(StandardCharsets.UTF_8));

		hashTableService.load(mockFile);

		assertEquals("value", hashTableService.get("key"));
	}

	@Test
	void testLoadEmptyFile() {
		MultipartFile emptyFile = new MockMultipartFile("file", "dump.json", "application/json", new byte[0]);

		IOException exception = assertThrows(IOException.class, () -> {
			hashTableService.load(emptyFile);
		});

		assertEquals("File is empty", exception.getMessage());
	}
}
