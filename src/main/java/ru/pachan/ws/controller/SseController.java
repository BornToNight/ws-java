package ru.pachan.ws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.pachan.ws.service.ContactService;

import java.io.IOException;

@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/sse")
public class SseController {

    private final ContactService contactService;

    @PostMapping
    public void contact(@RequestParam String connectionId, @RequestParam String message) {
        contactService.sendContact(connectionId, message);
    }

    @GetMapping("/{connectionId}")
    public SseEmitter contactSse(@PathVariable String connectionId) {
        var emitter = new SseEmitter(15000L);
        try {
            emitter.send(SseEmitter.event()
                    .comment("SSE connected")
                    .reconnectTime(5000));
        } catch (IOException | IllegalArgumentException ignored) {
        }
        emitter.onCompletion(() -> contactService.removeSubscription(connectionId));
        emitter.onTimeout(() -> contactService.removeSubscription(connectionId));
        emitter.onError((e) -> contactService.removeSubscription(connectionId));

        contactService.addSubscription(connectionId, emitter);

        return emitter;
    }

}
