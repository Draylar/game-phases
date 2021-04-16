package draylar.gamephases.kube;

import dev.latvian.kubejs.event.EventJS;
import draylar.gamephases.api.Phase;

import java.util.HashMap;
import java.util.Map;

public class GamePhasesEventJS extends EventJS {

    private static final Map<String, Phase> PHASES = new HashMap<>();

    public GamePhasesEventJS() {
        PHASES.clear();
    }

    public Phase phase(String id) {
        Phase phase = new Phase(id);
        PHASES.put(id, phase);
        return phase;
    }

    public static Map<String, Phase> getPhases() {
        return PHASES;
    }
}
