package com.service.hashTable;

import com.service.hashTable.service.HashTableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
	void testTTLExpiry() throws InterruptedException {
		hashTableService.set("key", "value", 1000L);
		Thread.sleep(1500L);
		assertNull(hashTableService.get("key"));
	}

}
