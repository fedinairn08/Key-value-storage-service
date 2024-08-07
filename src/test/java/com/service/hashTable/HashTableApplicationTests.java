package com.service.hashTable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.service.hashTable.entity.Storage;
import com.service.hashTable.service.HashTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class HashTableApplicationTests {
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
	public void testDump() throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		hashTableService.set("key1", "value1", 1000L);
		hashTableService.set("key2", "value2", 2000L);

		Map<String, Storage> expectedMap = new HashMap<>();
		expectedMap.put("key1", new Storage("value1", System.currentTimeMillis() + 1000L));
		expectedMap.put("key2", new Storage("value2", System.currentTimeMillis() + 2000L));
		String expectedJson = objectMapper.writeValueAsString(expectedMap);

		String result = hashTableService.dump();

		Map<String, Storage> resultData = objectMapper.readValue(result, Map.class);
		Map<String, Storage> expectedData = objectMapper.readValue(expectedJson, Map.class);

		assertTrue(resultData.equals(expectedData));
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
