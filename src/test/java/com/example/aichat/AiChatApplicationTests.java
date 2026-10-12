package com.example.aichat;

import com.example.aichat.dto.ChatRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AiChatApplicationTests {

	@Autowired
	private ChatController chatController;

	@Test
	void contextLoads() {
		assertNotNull(chatController);
	}

	@Test
	void testChatWithOllama() {
		String response = chatController.getMessage("Say hello in one word", "test-session-1");
		System.out.println(">>> Ollama Response: " + response);
		assertNotNull(response);
	}

	@Test
	void testPostChatWithMemory() {
		Map<String, String> response = chatController.chatWithMemoryPost(
				new ChatRequest("My name is Thanakkumar", "user-100")
		);
		System.out.println(">>> POST Chat Response: " + response);
		assertNotNull(response);
		assertTrue(response.containsKey("response"));
	}

}
