package draylar.gamephases.compat;

import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.overlay.ScreenOverlay;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Environment(EnvType.CLIENT)
public class REICompat {

    private static final List<EntryStack<?>> hidden = new ArrayList<>();

    public static void hideBlockedItems() {
        // Re-add hidden entries for reload system
        List<EntryStack<?>> currentEntries = EntryRegistry.getInstance().getEntryStacks().toList();
        hidden.forEach(entry -> {
            if(!currentEntries.contains(entry)) {
                EntryRegistry.getInstance().addEntry(entry);
            }
        });

        hidden.clear();

        // Iterate over REI entries, removing & storing entries that the player can't see.
        Iterator<EntryStack<?>> iterator = EntryRegistry.getInstance().getEntryStacks().iterator();
        while (iterator.hasNext()) {
            EntryStack<?> next = iterator.next();
            if(next.getValue() instanceof ItemStack stack) {

                // Check all registered Phases.
                // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
                boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(stack.getItem())).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
                if(!allowed) {
                    EntryRegistry.getInstance().removeEntry(next);
                    hidden.add(next);
                }
            }
        }

        EntryRegistry.getInstance().refilter();
        REIRuntime.getInstance().getOverlay().ifPresent(ScreenOverlay::queueReloadOverlay);
    }
}
