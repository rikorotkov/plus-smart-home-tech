package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;

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
                                .setConditions(scenarioAdded.getConditionList().stream()
                                        .map(sc -> ScenarioConditionAvro.newBuilder()
                                                .setSensorId(sc.getSensorId())
                                                .setType(ConditionTypeAvro.valueOf(sc.getType().name()))
                                                .setValue(sc.getValueCase() == ScenarioConditionProto.ValueCase.INT_VALUE ? sc.getIntValue() :
                                                        sc.getValueCase() == ScenarioConditionProto.ValueCase.BOOL_VALUE ? sc.getBoolValue() : null)
                                                .setOperation(ConditionOperationAvro.valueOf(sc.getOperation().name()))
                                                .build())
                                        .toList())
                                .setActions(scenarioAdded.getActionList().stream()
                                        .map(da -> DeviceActionAvro.newBuilder()
                                                .setSensorId(da.getSensorId())
                                                .setType(ActionTypeAvro.valueOf(da.getType().name()))
                                                .setValue(da.getValue())
                                                .build())
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
            case TEMPERATURE_SENSOR_EVENT -> {
                TemperatureSensorProto temperatureSensorEvent = sensorEvent.getTemperatureSensorEvent();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(TemperatureSensorAvro.newBuilder()
                                .setTemperatureC(temperatureSensorEvent.getTemperatureC())
                                .setTemperatureF(temperatureSensorEvent.getTemperatureF())
                                .build())
                        .build();
            }
            case MOTION_SENSOR_EVENT -> {
                MotionSensorProto motionSensorEvent = sensorEvent.getMotionSensorEvent();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(MotionSensorAvro.newBuilder()
                                .setLinkQuality(motionSensorEvent.getLinkQuality())
                                .setMotion(motionSensorEvent.getMotion())
                                .setVoltage(motionSensorEvent.getVoltage())
                                .build())
                        .build();
            }
            case CLIMATE_SENSOR_EVENT -> {
                ClimateSensorProto climateSensorEvent = sensorEvent.getClimateSensorEvent();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(ClimateSensorAvro.newBuilder()
                                .setTemperatureC(climateSensorEvent.getTemperatureC())
                                .setHumidity(climateSensorEvent.getHumidity())
                                .setCo2Level(climateSensorEvent.getCo2Level())
                                .build())
                        .build();
            }
            case LIGHT_SENSOR_EVENT -> {
                LightSensorProto lightSensorEvent = sensorEvent.getLightSensorEvent();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(LightSensorAvro.newBuilder()
                                .setLinkQuality(lightSensorEvent.getLinkQuality())
                                .setLuminosity(lightSensorEvent.getLuminosity())
                                .build())
                        .build();
            }
            case SWITCH_SENSOR_EVENT -> {
                SwitchSensorProto switchSensorEvent = sensorEvent.getSwitchSensorEvent();
                yield SensorEventAvro.newBuilder()
                        .setId(sensorEvent.getId())
                        .setHubId(sensorEvent.getHubId())
                        .setTimestamp(Instant.ofEpochSecond(sensorEvent.getTimestamp().getSeconds(), sensorEvent.getTimestamp().getNanos()))
                        .setPayload(SwitchSensorAvro.newBuilder()
                                .setState(switchSensorEvent.getState())
                                .build())
                        .build();
            }
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Payload is not set");
        };
    }
}
