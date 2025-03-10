package ru.pachan.ws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.pachan.ws.dto.ResultDto;

@RequiredArgsConstructor
@Controller
public class WebSocketTestController {

    private final SimpMessagingTemplate messagingTemplate;

    // /app/result
    @MessageMapping("/result")
    public void processMessage(@Payload ResultDto resultDto) {

        // /user/{userId}/result
        messagingTemplate.convertAndSendToUser(
                resultDto.userId().toString(), "/result",
                resultDto.result()
        );
    }

    @PostMapping("/api/v1/test/ws/send")
    public ResponseEntity<String> sendMessage(@RequestBody ResultDto resultDto) {
        messagingTemplate.convertAndSendToUser(
                resultDto.userId().toString(), "/result",
                resultDto.result()
        );
        return ResponseEntity.ok("ok");
    }
}
