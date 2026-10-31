package com.bahram.studentapi.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("/api/v1/ai")
public class AiController {
    
    private final ChatClient chatClient;
    private final StudentTools studentTools;

    public AiController(ChatClient.Builder chatClientBuilder, StudentTools studentTools) {

        this.chatClient = chatClientBuilder.build();
        this.studentTools = studentTools;

    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/agent")
    public String agent(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .tools(studentTools)
                .call()
                .content();
    }
    
    
}
