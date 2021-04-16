package draylar.gamephases.cca;

import dev.onyxstudios.cca.api.v3.component.ComponentV3;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import draylar.gamephases.GamePhases;
import draylar.gamephases.compat.REICompat;
import net.fabricmc.fabric.api.util.NbtType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class PhaseComponent implements ComponentV3, AutoSyncedComponent {

    private final String PHASES_KEY = "Phases";
    private final Map<String, Boolean> phases = new HashMap<>();
    private final PlayerEntity player;

    public PhaseComponent(PlayerEntity player) {
        this.player = player;
    }

    /**
     * @param phase phase to check for unlock status
     * @return {@code true} if the {@link net.minecraft.entity.player.PlayerEntity} this component is associated with has unlocked the given phase.
     */
    public boolean has(String phase) {
        return phases.getOrDefault(phase, false);
    }

    /**
     * Configures the status of the specified phase for the {@link net.minecraft.entity.player.PlayerEntity} this phase tracker is associated with.
     * @param phase phase to configure
     * @param status new status of the phase
     */
    public void set(String phase, boolean status) {
        phases.put(phase, status);
        GamePhases.PHASES.sync(player);
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        ListTag list = tag.getList("Phases", NbtType.COMPOUND);
        phases.clear();

        list.forEach(element -> {
            CompoundTag c = (CompoundTag) element;
            phases.put(c.getString("Phase"), c.getBoolean("Unlocked"));
        });
    }

    @Override
    public void writeToNbt(CompoundTag tag) {
        ListTag list = new ListTag();

        phases.forEach((phase, unlocked) -> {
            CompoundTag c = new CompoundTag();
            c.putString("Phase", phase);
            c.putBoolean("Unlocked", unlocked);
            list.add(c);
        });

        tag.put("Phases", list);
    }

    @Override
    public void applySyncPacket(PacketByteBuf buf) {
        AutoSyncedComponent.super.applySyncPacket(buf);

        // REI Compatibility
        if(FabricLoader.getInstance().isModLoaded("rei")) {
            REICompat.hideBlockedItems();
        }
    }
}
