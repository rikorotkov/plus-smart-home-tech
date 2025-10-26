package ru.yandex.practicum.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;

    @Override
    public Class<?> getPayloadClass() {
        return ScenarioAddedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        String hubId = event.getHubId();
        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) event.getPayload();
        String scenarioName = scenarioAddedEventAvro.getName();

        Map<String, Action> actions = scenarioAddedEventAvro.getActions().stream()
                .collect(toMap(DeviceActionAvro::getSensorId, action -> Action.builder()
                        .type(ActionType.valueOf(action.getType().name()))
                        .value(action.getValue())
                        .build()));

        Map<String, Condition> conditions = scenarioAddedEventAvro.getConditions().stream()
                .collect(toMap(ScenarioConditionAvro::getSensorId, condition -> {
                    Object value = condition.getValue();
                    return Condition.builder()
                            .operation(ConditionOperation.valueOf(condition.getOperation().name()))
                            .type(ConditionType.valueOf(condition.getType().name()))
                            .value(value instanceof Integer ? (Integer) value : value.equals(Boolean.TRUE) ? 1 : 0)
                            .build();
                }));

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(scenarioName)
                .actions(actions)
                .conditions(conditions)
                .build();

        scenarioRepository.saveAndFlush(scenario);
    }
}
