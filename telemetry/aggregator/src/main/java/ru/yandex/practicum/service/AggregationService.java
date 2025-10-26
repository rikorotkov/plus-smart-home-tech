package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
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
    public void listen(SensorEventAvro event) {
        SensorsSnapshotAvro sensorsSnapshotAvro = snapshots.computeIfAbsent(event.getHubId(), key -> SensorsSnapshotAvro.newBuilder()
                .setHubId(key)
                .setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getEpochSecond(), event.getTimestamp().getNano()))
                .setSensorsState(new ConcurrentHashMap<>())
                .build());
        SensorStateAvro sensorStateAvro = sensorsSnapshotAvro.getSensorsState().get(event.getId());

        if (sensorStateAvro != null && sensorStateAvro.getData().equals(event.getPayload())) {
            return;
        }

        SensorStateAvro stateAvro = SensorStateAvro.newBuilder()
                .setTimestamp(event.getTimestamp())
                .setData(event.getPayload())
                .build();

        sensorsSnapshotAvro.getSensorsState().put(event.getId(), stateAvro);
        sensorsSnapshotAvro.setTimestamp(Instant.ofEpochSecond(event.getTimestamp().getEpochSecond(), event.getTimestamp().getNano()));
        kafkaTemplate.send(snapshotTopic, sensorsSnapshotAvro.getHubId(), sensorsSnapshotAvro);
    }
}