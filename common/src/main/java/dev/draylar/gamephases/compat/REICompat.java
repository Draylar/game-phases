package dev.draylar.gamephases.compat;

import dev.draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class REICompat implements REIClientPlugin {

    private static BasicFilteringRule.MarkDirty filter;

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        filter = rule.hide(() -> {
            EntryIngredient.Builder builder = EntryIngredient.builder();
            for (Item item : Registry.ITEM) {

                // Check all registered Phases.
                // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
                boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
                if(!allowed) {
                    builder.add(EntryStacks.of(item));
                }
            }

            return builder.build();
        });
    }

    public static void refreshHiddenItems() {
        filter.markDirty();
    }
}
