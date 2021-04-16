package draylar.gamephases.kube;

import dev.latvian.kubejs.event.EventJS;
import draylar.gamephases.api.Phase;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePhasesEventJS extends EventJS {

    private static final Map<String, Phase> PHASES = new HashMap<>();

    public GamePhasesEventJS() {
        PHASES.clear();
    }

    public void phase(String id) {
        System.out.println("Registering phase: " + id);
        PHASES.put(id, new Phase(id));
    }

    public void item(String phase, String id) {
        PHASES.get(phase).disallow(Registry.ITEM.get(new Identifier(id)));
    }

    public static Map<String, Phase> getPhases() {
        return PHASES;
    }
}
