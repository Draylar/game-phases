package draylar.gamephases.api;

import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class Phase {

    private final String id;
    private final List<Item> blacklistedItems = new ArrayList<>();
    private final List<String> blacklistedDimensions = new ArrayList<>();

    public Phase(String id) {
        this.id = id;
    }

    public Phase item(Item item) {
        blacklistedItems.add(item);
        return this;
    }

    public Phase dimension(String dimension) {
        blacklistedDimensions.add(dimension);
        return this;
    }

    public boolean disallows(Item item) {
        return blacklistedItems.contains(item);
    }

    public boolean disallows(ServerWorld world) {
        return blacklistedDimensions.contains(world.getRegistryKey().getValue().toString());
    }

    public boolean hasUnlocked(PlayerEntity player) {
        return GamePhases.getPhaseData(player).has(this);
    }
}
