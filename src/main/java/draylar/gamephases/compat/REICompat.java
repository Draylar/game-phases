package draylar.gamephases.compat;

import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.rei.api.EntryRegistry;
import me.shedaniel.rei.api.REIHelper;
import me.shedaniel.rei.api.REIOverlay;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;

@Environment(EnvType.CLIENT)
public class REICompat {

    public static void hideBlockedItems() {
        EntryRegistry.getInstance().removeEntryIf(entry -> {
            Item item = entry.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
            return !allowed;
        });

        EntryRegistry.getInstance().refilter();
        REIHelper.getInstance().getOverlay().ifPresent(REIOverlay::queueReloadOverlay);
    }
}
