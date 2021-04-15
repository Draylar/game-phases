package draylar.gamephases.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PhaseComponent implements ComponentV3, AutoSyncedComponent {

    private final String PHASES_KEY = "Phases";
    private final Map<Identifier, Boolean> phases = new HashMap<>();
    private final PlayerEntity player;

    public PhaseComponent(PlayerEntity player) {
        this.player = player;
    }

    /**
     * @param phase phase to check for unlock status
     * @return {@code true} if the {@link net.minecraft.entity.player.PlayerEntity} this component is associated with has unlocked the given phase.
     */
    public boolean has(Identifier phase) {
        return phases.getOrDefault(phase, false);
    }

    /**
     * Configures the status of the specified phase for the {@link net.minecraft.entity.player.PlayerEntity} this phase tracker is associated with.
     * @param phase phase to configure
     * @param status new status of the phase
     */
    public void set(Identifier phase, boolean status) {
        phases.put(phase, status);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }
}
