package ru.pachan.ws.service;

import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;
import ru.pachan.ws.util.ResponseException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class ContactWebFluxService {
    private final ConcurrentMap<String, FluxSink<ServerSentEvent<Object>>> subscriptions = new ConcurrentHashMap<>();

    public void addSubscription(String connectionId, FluxSink<ServerSentEvent<Object>> sink) {
        subscriptions.put(connectionId, sink);
    }

    public void removeSubscription(String connectionId) {
        subscriptions.remove(connectionId);
    }

    public void sendContact(String connectionId, String contact) {
        FluxSink<ServerSentEvent<Object>> sink = subscriptions.get(connectionId);
        if (sink != null) {
            ServerSentEvent<Object> event = ServerSentEvent.builder()
                    .event("contactAdvice")
                    .data(contact)
                    .build();
            sink.next(event);
        } else {
            ResponseException.notFound("Не найдено открытое соединение");
        }
    }

}
