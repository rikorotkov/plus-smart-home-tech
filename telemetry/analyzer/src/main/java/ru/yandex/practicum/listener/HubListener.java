package ru.yandex.practicum.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HubListener {
    private final Map<Class<?>, HubEventHandler> hubEventHandlers;

    public HubListener(Set<HubEventHandler> hubEventHandlers) {
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getPayloadClass, Function.identity()));
    }

    @KafkaListener(topics = "${topic.hub-event}", containerFactory = "hubEventListenerFactory")
    public void listenHubEvents(HubEventAvro hubEventAvro) {
        Class<?> key = hubEventAvro.getPayload().getClass();
        HubEventHandler hubEventHandler = hubEventHandlers.get(key);
        hubEventHandler.handle(hubEventAvro);
    }

}
