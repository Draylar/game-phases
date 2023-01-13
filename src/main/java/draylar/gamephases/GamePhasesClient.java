package draylar.gamephases;

import dev.architectury.event.events.client.ClientTooltipEvent;
import draylar.gamephases.impl.PlayerDataProvider;
import draylar.gamephases.kube.GamePhasesEventJS;
import draylar.gamephases.network.ClientNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class GamePhasesClient implements ClientModInitializer {

    public static Map<String, Boolean> cachedPhasedata;

    @Override
    public void onInitializeClient() {
        ClientNetworking.initialize();
        registerItemTooltipHandler();
    }

    private void registerItemTooltipHandler() {
        // Render a blank tooltip when the user does not have permission to use the item.
        ClientTooltipEvent.ITEM.register((stack, list, context) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean b = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
            if (!b) {
                list.clear();
                list.add(new LiteralText("Locked").formatted(Formatting.RED));
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                ClientPlayerEntity player = client.player;

                if(cachedPhasedata != null && player != null) {
                    ((PlayerDataProvider) player).phases$set(cachedPhasedata);
                    cachedPhasedata = null;
                }
            });
        });
    }
}
