package dev.draylar.gamephases;

import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.draylar.gamephases.impl.PlayerDataProvider;
import dev.draylar.gamephases.network.ClientNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Map;

public class GamePhasesClient {

    public static Map<String, Boolean> cachedPhaseData;

    public static void initialize() {
        ClientNetworking.initialize();
        registerItemTooltipHandler();
    }

    private static void registerItemTooltipHandler() {
        // Render a blank tooltip when the user does not have permission to use the item.
        ClientTooltipEvent.ITEM.register((stack, list, flag) -> {
            Item item = stack.getItem();

            // prevent rare NPE where player is null when rendering tooltips
            if(MinecraftClient.getInstance().player == null) {
                return;
            }

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean isItemAllowed = GamePhases.allowed(item, MinecraftClient.getInstance().player);
            if (!isItemAllowed) {
                list.clear();
                list.add(new LiteralText("Locked").formatted(Formatting.RED));
            }
        });
    }

    public static void loadCachedPhaseData() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        // When a player loads into a world, there is a chance we have recieved data from the server for phases from a packet
        // at a poor time. To allow this data to load on the joining player, we cache it and apply it later if needed.
        if(cachedPhaseData != null && player != null) {
            ((PlayerDataProvider) player).phases$set(cachedPhaseData);
            cachedPhaseData = null;
        }
    }
}
