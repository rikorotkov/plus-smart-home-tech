package ru.yandex.practicum.listener;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.entity.*;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SnapshotListener {

    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    @Transactional(readOnly = true)
    @KafkaListener(topics = "${topic.snapshot}", containerFactory = "snapshotListenerFactory")
    public void listenSnapshots(SensorsSnapshotAvro sensorsSnapshotAvro) {
        String hubId = sensorsSnapshotAvro.getHubId();
        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        Map<String, SensorStateAvro> sensorsState = sensorsSnapshotAvro.getSensorsState();

        scenarios.forEach(
                scenario -> {
                    boolean allMatch = scenario.getConditions().entrySet().stream()
                            .allMatch(entry -> {
                                String sensorId = entry.getKey();
                                Condition condition = entry.getValue();
                                SensorStateAvro sensorStateAvro = sensorsState.get(sensorId);
                                return sensorsState != null && ifConditionMatch(condition, sensorStateAvro);
                            });
                    if (allMatch) {
                        Map<String, Action> actions = scenario.getActions();
                        String scenarioName = scenario.getName();
                        sendActionRequest(actions, hubId, scenarioName);
                    }
                });
    }

    private void sendActionRequest(Map<String, Action> actions, String hubId, String scenarioName) {
        actions.forEach((sensorId, action) -> {
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(DeviceActionProto.newBuilder()
                            .setSensorId(sensorId)
                            .setType(ActionTypeProto.valueOf(action.getType().name()))
                            .setValue(action.getValue())
                            .build())
                    .build();
            hubRouterClient.handleDeviceAction(request);
        });

    }

    private boolean ifConditionMatch(Condition condition, SensorStateAvro sensorsState) {
        if (sensorsState == null) {
            return false;
        }
        ConditionType type = condition.getType();
        Integer value = condition.getValue();
        ConditionOperation operation = condition.getOperation();

        Object data = sensorsState.getData();

        switch (type) {
            case MOTION -> {
                Boolean motion = extractMotion(data);
                return compareValues(motion ? 1 : 0, operation, value);
            }
            case LUMINOSITY -> {
                Integer luminosity = extractLuminosity(data);
                return compareValues(luminosity, operation, value);
            }
            case SWITCH -> {
                Boolean switchState = extractSwitchState(data);
                return compareValues(switchState ? 1 : 0, operation, value);
            }
            case TEMPERATURE -> {
                Integer temperature = extractTemperature(data);
                return compareValues(temperature, operation, value);
            }
            case HUMIDITY -> {
                Integer humidity = extractHumidity(data);
                return compareValues(humidity, operation, value);
            }
            case CO2LEVEL -> {
                Integer co2Level = extractCo2Level(data);
                return compareValues(co2Level, operation, value);
            }
        }
        return false;
    }

    private Integer extractCo2Level(Object data) {
        if (data instanceof ClimateSensorAvro) {
            return ((ClimateSensorAvro) data).getCo2Level();
        } else {
            return null;
        }
    }

    private Integer extractHumidity(Object data) {
        if (data instanceof ClimateSensorAvro) {
            return ((ClimateSensorAvro) data).getHumidity();
        } else {
            return null;
        }
    }

    private Integer extractTemperature(Object data) {
        if (data instanceof TemperatureSensorAvro) {
            return ((TemperatureSensorAvro) data).getTemperatureC();
        } else if (data instanceof ClimateSensorAvro) {
            return ((ClimateSensorAvro) data).getTemperatureC();
        } else {
            return null;
        }
    }

    private Boolean extractSwitchState(Object data) {
        if (data instanceof SwitchSensorAvro) {
            return ((SwitchSensorAvro) data).getState();
        } else {
            return null;
        }
    }

    private Integer extractLuminosity(Object data) {
        if (data instanceof LightSensorAvro) {
            return ((LightSensorAvro) data).getLuminosity();
        } else {
            return null;
        }
    }

    private Boolean extractMotion(Object data) {
        if (data instanceof MotionSensorAvro) {
            return ((MotionSensorAvro) data).getMotion();
        } else {
            return null;
        }
    }

    private boolean compareValues(Integer actual, ConditionOperation operation, Integer expected) {
        if (actual == null) return false;
        return switch (operation) {
            case EQUALS -> Objects.equals(actual, expected);
            case LOWER_THAN -> actual < expected;
            case GREATER_THAN -> actual > expected;
        };
    }
}
