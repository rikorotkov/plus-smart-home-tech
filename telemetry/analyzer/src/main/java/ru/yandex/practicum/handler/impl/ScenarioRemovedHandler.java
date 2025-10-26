package ru.yandex.practicum.handler.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.entity.Scenario;
import ru.yandex.practicum.handler.HubEventHandler;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;

@Component
@RequiredArgsConstructor
public class ScenarioRemovedHandler implements HubEventHandler {

    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    @Override
    public Class<?> getPayloadClass() {
        return ScenarioRemovedEventAvro.class;
    }

    @Override
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro scenarioRemovedEventAvro = (ScenarioRemovedEventAvro) event.getPayload();
        String name = scenarioRemovedEventAvro.getName();

        Scenario scenario = scenarioRepository.findByHubIdAndName(event.getHubId(), name).orElseThrow();

        conditionRepository.deleteAll(scenario.getConditions().values());
        actionRepository.deleteAll(scenario.getActions().values());
        scenarioRepository.delete(scenario);
    }
}
