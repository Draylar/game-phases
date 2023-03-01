package dev.draylar.gamephases;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.*;
import dev.architectury.registry.ReloadListenerRegistry;
import dev.draylar.gamephases.api.Phase;
import dev.draylar.gamephases.arch.AttackEvent;
import dev.draylar.gamephases.command.PhaseCommand;
import dev.draylar.gamephases.impl.PlayerDataProvider;
import dev.draylar.gamephases.kube.GamePhasesEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class GamePhases {

    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

    public static void initialize() {
        PhaseCommand.init();

        LifecycleEvent.SERVER_STARTED.register(server -> {
            new GamePhasesEventJS(server).post(ScriptType.SERVER, "gamephases.initialize");
            GamePhasesEventJS.sync(server);
        });

        // When resource packs are reloaded, load Game Phase data.
        LifecycleEvent.SERVER_STARTED.register(server -> {
            ReloadListenerRegistry.register(ResourceType.SERVER_DATA, (synchronizer, manager, prepareProfiler, applyProfiler, prepareExecutor, applyExecutor) -> {
                return CompletableFuture.runAsync(() -> {
                    new GamePhasesEventJS(server).post(ScriptType.SERVER, "gamephases.initialize");
                    GamePhasesEventJS.sync(server);
                });
            });
        });

        // When a ServerPlayerEntity is re-spawned (and cloned), sync the new player data to the client.
        PlayerEvent.PLAYER_RESPAWN.register((newPlayer, wonGame) -> {
            ((PlayerDataProvider) newPlayer).phases$sync();
        });

        // Use KubeJS events for item handling
        registerItemHandlers();
    }

    public static void registerItemHandlers() {
        // Block Item pickups if the player has not unlocked all required phases for the Item.
        PlayerEvent.PICKUP_ITEM_PRE.register((player, itemEntity, stack) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = allowed(item, player);
            return !allowed ? EventResult.interruptFalse() : EventResult.pass();
        });

        // Block Item use
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = allowed(item, player);
            return !allowed ? CompoundEventResult.interruptFalse(stackInHand) : CompoundEventResult.pass();
        });

        // Prevent attacking with blocked items
        AttackEvent.ATTACK_ENTITY.register((player, world, hand, entity, hitResult) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = allowed(item, player);
            return !allowed ? EventResult.interruptFalse() : EventResult.pass();
        });

        // Prevent breaking with blocked items
        InteractionEvent.LEFT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = allowed(item, player);
            return !allowed ? EventResult.interruptFalse() : EventResult.pass();
        });

        // When a player ticks, check their held item. If the held item is blocked, drop it.
        TickEvent.PLAYER_PRE.register(player -> {

            // only check once per 10 ticks
            if(player.age % 10 != 0) {
                return;
            }

            for (Hand hand : Hand.values()) {
                ItemStack stack = player.getStackInHand(hand);
                Item item = stack.getItem();
                boolean allowed = allowed(item, player);
                if(!allowed) {
                    player.dropStack(stack.copy());
                    stack.decrement(stack.getCount());
                }
            }
        });

        // When a player first joins, sync server-side phase data to the client.
        PlayerEvent.PLAYER_JOIN.register(player -> {
            GamePhases.getPhaseData(player).phases$sync();
            GamePhasesEventJS.sync(player);
        });
    }

    public static boolean allowed(Item item, PlayerEntity player) {
        ArrayList<Phase> phases = new ArrayList<>(GamePhasesEventJS.getPhases().values());
        for (Phase phase : phases) {
            if(phase.restricts(item) && !phase.hasUnlocked(player)) {
                return false;
            }
        }

        return true;
    }

    public static Identifier id(String name) {
        return new Identifier("gamephases", name);
    }

    public static PlayerDataProvider getPhaseData(PlayerEntity player) {
        return ((PlayerDataProvider) player);
    }
}
