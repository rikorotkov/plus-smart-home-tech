package ru.yandex.practicum.mapper;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.*;

@Component
public class HubEventMapper {
    public HubEventAvro toHubEventAvro(HubEvent hubEvent) {
        return HubEventAvro.newBuilder()
                .setHubId(hubEvent.getHubId())
                .setTimestamp(hubEvent.getTimestamp())
                .setPayload(toHubEventPayloadAvro(hubEvent))
                .build();
    }

    private SpecificRecordBase toHubEventPayloadAvro(HubEvent hubEvent) {
        return switch (hubEvent.getType()) {
            case DEVICE_ADDED -> {
                DeviceAddedEvent event = (DeviceAddedEvent) hubEvent;
                yield DeviceAddedEventAvro.newBuilder()
                        .setId(event.getId())
                        .setType(DeviceTypeAvro.valueOf(event.getDeviceType().name()))
                        .build();
            }
            case DEVICE_REMOVED -> {
                DeviceRemovedEvent event = (DeviceRemovedEvent) hubEvent;
                yield DeviceRemovedEventAvro.newBuilder()
                        .setId(event.getId())
                        .build();
            }
            case SCENARIO_ADDED -> {
                ScenarioAddedEvent event = (ScenarioAddedEvent) hubEvent;
                yield ScenarioAddedEventAvro.newBuilder()
                        .setName(event.getName())
                        .setConditions(event.getConditions().stream()
                                .map(this::toScenarioConditionAvro)
                                .toList())
                        .setActions(event.getActions().stream()
                                .map(this::toDeviceActionAvro)
                                .toList())
                        .build();
            }
            case SCENARIO_REMOVED -> {
                ScenarioRemovedEvent event = (ScenarioRemovedEvent) hubEvent;
                yield ScenarioRemovedEventAvro.newBuilder()
                        .setName(event.getName())
                        .build();
            }
        };
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition scenarioCondition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(scenarioCondition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(scenarioCondition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name()))
                .setValue(scenarioCondition.getValue())
                .build();
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceAction deviceAction) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(deviceAction.getSensorId())
                .setType(ActionTypeAvro.valueOf(deviceAction.getType().name()))
                .setValue(deviceAction.getValue())
                .build();
    }
}
