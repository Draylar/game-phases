package draylar.gamephases;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.latvian.kubejs.script.ScriptType;
import draylar.gamephases.command.PhaseCommand;
import draylar.gamephases.impl.PlayerDataProvider;
import draylar.gamephases.kube.GamePhasesEventJS;
import draylar.gamephases.network.ServerNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class GamePhases implements ModInitializer {

    public static final Identifier ALL_PHASE_SYNC_ID = id("all_phase_sync");

    @Override
    public void onInitialize() {
        PhaseCommand.init();

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            new GamePhasesEventJS().post(ScriptType.SERVER, "gamephases.initialize");
            GamePhasesEventJS.sync(server);
        });

        // When resource packs are reloaded, load Game Phase data.
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, n) -> {
            new GamePhasesEventJS().post(ScriptType.SERVER, "gamephases.initialize");
            GamePhasesEventJS.sync(server);
        });

        // Use KubeJS events for item handling
        registerItemHandlers();
    }

    public void registerItemHandlers() {
        // Block Item pickups if the player has not unlocked all required phases for the Item.
        PlayerEvent.PICKUP_ITEM_PRE.register((player, itemEntity, stack) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? EventResult.interruptFalse() : EventResult.pass();
        });

        // Block Item use
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? TypedActionResult.fail(stackInHand) : TypedActionResult.pass(stackInHand);
        });

        // Prevent attacking with blocked items
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? ActionResult.FAIL : ActionResult.PASS;
        });

        // Prevent breaking with blocked items
        AttackBlockCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? ActionResult.FAIL : ActionResult.PASS;
        });

        // When a player ticks, check their held item. If the held item is blocked, drop it.
        TickEvent.PLAYER_PRE.register(player -> {
            for (Hand hand : Hand.values()) {
                ItemStack stack = player.getStackInHand(hand);
                Item item = stack.getItem();
                boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
                if(!allowed) {
                    player.dropStack(stack.copy());
                    stack.decrement(stack.getCount());
                }
            }
        });

        // When a player first joins, sync server-side phase data to the client.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            GamePhases.getPhaseData(handler.player).sync();
        });
    }

    public static Identifier id(String name) {
        return new Identifier("gamephases", name);
    }

    public static PlayerDataProvider getPhaseData(PlayerEntity player) {
        return ((PlayerDataProvider) player);
    }
}
