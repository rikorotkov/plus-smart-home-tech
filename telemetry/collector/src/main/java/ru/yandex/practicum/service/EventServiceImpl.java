package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.HubEventMapper;
import ru.yandex.practicum.mapper.SensorEventMapper;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

import java.util.Properties;

@Log4j2
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private Producer<String, SpecificRecordBase> producer;

    private final HubEventMapper hubEventMapper;
    private final SensorEventMapper sensorEventMapper;

    private void initProducer() {
        if (producer == null) {
            Properties props = new Properties();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "ru.yandex.practicum.serializer.GeneralAvroSerializer");

            producer = new KafkaProducer<>(props);
        }
    }

    @Override
    public void collectSensorEvent(SensorEvent event) {
        log.info("Collecting SensorEvent: {}", event);
        SensorEventAvro message = sensorEventMapper.toSensorEventAvro(event);
        sendToKafka("telemetry.sensors.v1", message.getHubId(), message.getTimestamp().toEpochMilli(), message);
    }

    @Override
    public void collectHubEvent(HubEvent event) {
        log.info("Collecting HubEvent: {}", event);
        HubEventAvro message = hubEventMapper.toHubEventAvro(event);
        sendToKafka("telemetry.hubs.v1", message.getHubId(), message.getTimestamp().toEpochMilli(), message);
    }

    private void sendToKafka(String topic, String key, long timestamp, SpecificRecordBase message) {
        initProducer();
        ProducerRecord<String, SpecificRecordBase> msg = new ProducerRecord<>(topic, null, timestamp, key, message);
        producer.send(msg, (metadata, exception) -> {
            if (exception != null) {
                log.error("Ошибка отправки сообщения в топик {}: {}", topic, exception.getMessage(), exception);
            } else {
                log.info("Сообщение отправлено в топик {} партиция {} оффсет {}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            }
        });
    }
}