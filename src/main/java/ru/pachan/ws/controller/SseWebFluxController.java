package ru.pachan.ws.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.pachan.ws.service.ContactWebFluxService;

@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/sse/web-flux")
public class SseWebFluxController {

    private final ContactWebFluxService contactWebFluxService;

    @PostMapping()
    public void contactWebFlux(@RequestParam String connectionId, @RequestParam String message) {
        contactWebFluxService.sendContact(connectionId, message);
    }

    @GetMapping("/{connectionId}")
    public Flux<ServerSentEvent<Object>> contactSseWebFlux(@PathVariable String connectionId) {
        return Flux.<ServerSentEvent<Object>>create(sink -> {
            contactWebFluxService.addSubscription(connectionId, sink);
            sink.onCancel(() -> contactWebFluxService.removeSubscription(connectionId));
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .event("contactAdvice")
                    .comment("SSE Connected")
                    .build();
            sink.next(event);
        }).doOnCancel(() -> contactWebFluxService.removeSubscription(connectionId));
    }

}
