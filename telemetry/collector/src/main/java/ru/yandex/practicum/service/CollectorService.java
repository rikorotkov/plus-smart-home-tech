package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectorService {

    @Value("${topic.sensor-event}")
    private String sensorTopic = "telemetry.sensors.v1";
    @Value("${topic.hub-event}")
    private String hubTopic = "telemetry.hubs.v1";

    private final KafkaTemplate<String, SpecificRecordBase> kafkaTemplate;

    public void sendSensorEvent(SensorEventProto sensorEvent) {
        kafkaTemplate.send(sensorTopic, sensorEvent.getHubId(), mapToRecord(sensorEvent));
    }

    public void sendHubEvent(HubEventProto hubEvent) {
        kafkaTemplate.send(hubTopic, hubEvent.getHubId(), mapToRecord(hubEvent));
    }

    private SpecificRecordBase mapToRecord(HubEventProto hubEvent) {
        HubEventProto.PayloadCase payloadCase = hubEvent.getPayloadCase();
        return switch (payloadCase) {
            case DEVICE_ADDED -> {
                DeviceAddedEventProto deviceAdded = hubEvent.getDeviceAdded();
                yield HubEventAvro.newBuilder()
                        .setHubId(hubEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(hubEvent.getTimestamp().getSeconds(), hubEvent.getTimestamp().getNanos()))
                        .setPayload(DeviceAddedEventAvro.newBuilder()
                                .setId(deviceAdded.getId())
                                .setType(DeviceTypeAvro.valueOf(deviceAdded.getType().name()))
                                .build())
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEventProto deviceRemoved = hubEvent.getDeviceRemoved();
                yield HubEventAvro.newBuilder()
                        .setHubId(hubEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(hubEvent.getTimestamp().getSeconds(), hubEvent.getTimestamp().getNanos()))
                        .setPayload(DeviceRemovedEventAvro.newBuilder()
                                .setId(deviceRemoved.getId())
                                .build())
                        .build();
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEventProto scenarioAdded = hubEvent.getScenarioAdded();
                yield HubEventAvro.newBuilder()
                        .setHubId(hubEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(hubEvent.getTimestamp().getSeconds(), hubEvent.getTimestamp().getNanos()))
                        .setPayload(ScenarioAddedEventAvro.newBuilder()
                                .setName(scenarioAdded.getName())
                                .setConditions(scenarioAdded.getConditionsList().stream()
                                        .map(sc -> {
                                            ScenarioConditionAvro.Builder conditionBuilder = ScenarioConditionAvro.newBuilder()
                                                    .setSensorId(sc.getSensorId())
                                                    .setType(ConditionTypeAvro.valueOf(sc.getType().name()))
                                                    .setOperation(ConditionOperationAvro.valueOf(sc.getOperation().name()));

                                            switch (sc.getValueCase()) {
                                                case INT_VALUE:
                                                    conditionBuilder.setValue(sc.getIntValue());
                                                    break;
                                                case BOOL_VALUE:
                                                    conditionBuilder.setValue(sc.getBoolValue());
                                                    break;
                                                default:
                                                    throw new IllegalArgumentException("Unknown value type in condition");
                                            }

                                            return conditionBuilder.build();
                                        })
                                        .toList())
                                .setActions(scenarioAdded.getActionsList().stream()
                                        .map(da -> {
                                            DeviceActionAvro.Builder actionBuilder = DeviceActionAvro.newBuilder()
                                                    .setSensorId(da.getSensorId())
                                                    .setType(ActionTypeAvro.valueOf(da.getType().name()));

                                            if (da.hasValue()) {
                                                actionBuilder.setValue(da.getValue());
                                            }

                                            return actionBuilder.build();
                                        })
                                        .toList())
                                .build())
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEventProto scenarioRemoved = hubEvent.getScenarioRemoved();
                yield HubEventAvro.newBuilder()
                        .setHubId(hubEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(hubEvent.getTimestamp().getSeconds(), hubEvent.getTimestamp().getNanos()))
                        .setPayload(ScenarioRemovedEventAvro.newBuilder()
                                .setName(scenarioRemoved.getName())
                                .build())
                        .build();
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload is not set");
        };
    }

    private SpecificRecordBase mapToRecord(SensorEventProto sensorEvent) {
        SensorEventProto.PayloadCase payloadCase = sensorEvent.getPayloadCase();
        return switch (payloadCase) {
            case TEMPERATURE_SENSOR -> {
                TemperatureSensorProto temperatureSensor = sensorEvent.getTemperatureSensor();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(TemperatureSensorAvro.newBuilder()
                                .setTemperatureC(temperatureSensor.getTemperatureC())
                                .setTemperatureF(temperatureSensor.getTemperatureF())
                                .build())
                        .build();
            }
            case MOTION_SENSOR -> {
                MotionSensorProto motionSensor = sensorEvent.getMotionSensor();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(MotionSensorAvro.newBuilder()
                                .setLinkQuality(motionSensor.getLinkQuality())
                                .setMotion(motionSensor.getMotion())
                                .setVoltage(motionSensor.getVoltage())
                                .build())
                        .build();
            }
            case CLIMATE_SENSOR -> {
                ClimateSensorProto climateSensor = sensorEvent.getClimateSensor();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(ClimateSensorAvro.newBuilder()
                                .setTemperatureC(climateSensor.getTemperatureC())
                                .setHumidity(climateSensor.getHumidity())
                                .setCo2Level(climateSensor.getCo2Level())
                                .build())
                        .build();
            }
            case LIGHT_SENSOR -> {
                LightSensorProto lightSensor = sensorEvent.getLightSensor();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(LightSensorAvro.newBuilder()
                                .setLinkQuality(lightSensor.getLinkQuality())
                                .setLuminosity(lightSensor.getLuminosity())
                                .build())
                        .build();
            }
            case SWITCH_SENSOR -> {
                SwitchSensorProto switchSensor = sensorEvent.getSwitchSensor();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(SwitchSensorAvro.newBuilder()
                                .setState(switchSensor.getState())
                                .build())
                        .build();
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload is not set");
        };
    }
}