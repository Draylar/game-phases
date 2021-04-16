package draylar.gamephases.api;

import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;

import java.util.ArrayList;
import java.util.List;

public class Phase {

    private final String id;
    private final List<Item> blacklistedItems = new ArrayList<>();

    public Phase(String id) {
        this.id = id;
    }

    public void blacklist(Item item) {
        blacklistedItems.add(item);
    }

    public boolean disallows(Item item) {
        return blacklistedItems.contains(item);
    }

    public void disallow(Item item) {
        blacklistedItems.add(item);
    }

    public boolean hasUnlocked(PlayerEntity player) {
        return GamePhases.getPhaseData(player).has(this);
    }
}
