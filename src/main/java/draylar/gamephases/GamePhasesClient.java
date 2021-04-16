package draylar.gamephases;

import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.architectury.event.events.TooltipEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class GamePhasesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        registerItemHandlers();
    }

    private void registerItemHandlers() {
        // Render a blank tooltip when the user does not have permission to use the item.
        TooltipEvent.ITEM.register((stack, list, context) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean b = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
            if(!b) {
                list.clear();
                list.add(new LiteralText("Locked").formatted(Formatting.RED));
            }
        });
    }
}
