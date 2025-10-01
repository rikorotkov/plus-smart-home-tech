package ru.yandex.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.sensor.*;

@Component
public class SensorEventMapper {
    public SensorEventAvro toSensorEventAvro(SensorEvent sensorEvent) {
        return SensorEventAvro.newBuilder()
                .setId(sensorEvent.getId())
                .setHubId(sensorEvent.getHubId())
                .setTimestamp(sensorEvent.getTimestamp())
                .setPayload(toSensorEventPayloadAvro(sensorEvent))
                .build();
    }

    private SpecificRecordBase toSensorEventPayloadAvro(SensorEvent sensorEvent) {
        return switch (sensorEvent.getType()) {
            case CLIMATE_SENSOR_EVENT -> mapClimate((ClimateSensorEvent) sensorEvent);
            case LIGHT_SENSOR_EVENT -> mapLight((LightSensorEvent) sensorEvent);
            case MOTION_SENSOR_EVENT -> mapMotion((MotionSensorEvent) sensorEvent);
            case SWITCH_SENSOR_EVENT -> mapSwitch((SwitchSensorEvent) sensorEvent);
            case TEMPERATURE_SENSOR_EVENT -> mapTemperature((TemperatureSensorEvent) sensorEvent);
        };
    }

    private ClimateSensorAvro mapClimate(ClimateSensorEvent e) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(e.getTemperatureC())
                .setHumidity(e.getHumidity())
                .setCo2Level(e.getCo2Level())
                .build();
    }

    private LightSensorAvro mapLight(LightSensorEvent e) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setLuminosity(e.getLuminosity())
                .build();
    }

    private MotionSensorAvro mapMotion(MotionSensorEvent e) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(e.getLinkQuality())
                .setMotion(e.getMotion())
                .setVoltage(e.getVoltage())
                .build();
    }

    private SwitchSensorAvro mapSwitch(SwitchSensorEvent e) {
        return SwitchSensorAvro.newBuilder()
                .setState(e.getState())
                .build();
    }

    private TemperatureSensorAvro mapTemperature(TemperatureSensorEvent e) {
        return TemperatureSensorAvro.newBuilder()
                .setId(e.getId())
                .setHubId(e.getHubId())
                .setTimestamp(e.getTimestamp())
                .setTemperatureC(e.getTemperatureC())
                .setTemperatureF(e.getTemperatureF())
                .build();
    }
}
