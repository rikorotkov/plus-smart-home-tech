package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationService {

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;
    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    @Value("${topic.snapshot}")
    private String snapshotTopic;

    @KafkaListener(
            topics = "${topic.sensor-event}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void listen(SensorEventAvro event, Acknowledgment ack) {
        try {
            SensorsSnapshotAvro sensorsSnapshotAvro = snapshots.computeIfAbsent(event.getHubId(), key ->
                    SensorsSnapshotAvro.newBuilder()
                            .setHubId(key)
                            .setTimestamp(event.getTimestamp())
                            .setSensorsState(new ConcurrentHashMap<>())
                            .build()
            );

            SensorStateAvro currentState = sensorsSnapshotAvro.getSensorsState().get(event.getId());
            Instant eventTs = event.getTimestamp();
            Instant storedTs = currentState != null ? currentState.getTimestamp() : null;

            if (storedTs != null && eventTs.isBefore(storedTs)) {
                log.debug("Ignoring old event {} (eventTs={}, storedTs={})", event.getId(), eventTs, storedTs);
                ack.acknowledge();
                return;
            }

            if (currentState != null && currentState.getData().equals(event.getPayload())) {
                log.debug("Ignoring duplicate event {}", event.getId());
                ack.acknowledge();
                return;
            }

            SensorStateAvro newState = SensorStateAvro.newBuilder()
                    .setTimestamp(event.getTimestamp())
                    .setData(event.getPayload())
                    .build();

            sensorsSnapshotAvro.getSensorsState().put(event.getId(), newState);
            sensorsSnapshotAvro.setTimestamp(event.getTimestamp());

            kafkaTemplate.send(snapshotTopic, sensorsSnapshotAvro.getHubId(), sensorsSnapshotAvro).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Snapshot for hub {} sent successfully", sensorsSnapshotAvro.getHubId());
                    ack.acknowledge();
                } else {
                    log.error("Failed to send snapshot for hub {}: {}", sensorsSnapshotAvro.getHubId(), ex.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("Error processing event: {}", e.getMessage(), e);
        }
    }
}