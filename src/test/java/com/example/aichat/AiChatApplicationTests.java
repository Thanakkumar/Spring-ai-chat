package com.example.aichat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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
		String response = chatController.getMessage("Say hello in one word");
		System.out.println(">>> Ollama qwen3.5:4b Response: " + response);
		assertNotNull(response);
	}

}
