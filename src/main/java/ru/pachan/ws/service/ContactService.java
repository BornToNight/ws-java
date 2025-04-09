package ru.pachan.ws.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import ru.pachan.ws.util.ResponseException;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class ContactService {

    private final ConcurrentMap<String, SseEmitter> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::sendHeartbeats, 0, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        subscriptions.forEach((connectionId, emitter) -> {
            try {
                emitter.complete();
            } catch (IllegalArgumentException ignored) {
            }
        });
        subscriptions.clear();
        scheduler.shutdownNow();
    }

    public void addSubscription(String connectionId, SseEmitter emitter) {
        subscriptions.put(connectionId, emitter);
    }

    public void removeSubscription(String connectionId) {
        var emitter = subscriptions.remove(connectionId);
        if (emitter != null) {
            try {
                emitter.complete();
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    public void sendContact(String connectionId, String contact) {
        var emitter = subscriptions.get(connectionId);

        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("contactAdvice")
                        .data(contact));
            } catch (IOException | IllegalArgumentException e) {
                removeSubscription(connectionId);
                ResponseException.badRequest("Ошибка при отправке SSE");
            }
        } else {
            ResponseException.notFound("Не найдено открытое соединение");
        }
    }

    private void sendHeartbeats() {
        subscriptions.forEach((connectionId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .comment("heartbeat")
                        .reconnectTime(5000));
            } catch (IOException | IllegalArgumentException e) {
                removeSubscription(connectionId);
            }
        });
    }

}
