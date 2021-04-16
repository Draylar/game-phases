package draylar.gamephases;

import dev.latvian.kubejs.script.ScriptType;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import draylar.gamephases.cca.PhaseComponent;
import draylar.gamephases.command.PhaseCommand;
import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TickEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

public class GamePhases implements ModInitializer, EntityComponentInitializer {

    public static final Identifier PHASE_SYNC_ID = id("phase_sync");
    public static final ComponentKey<PhaseComponent> PHASES = ComponentRegistryV3.INSTANCE.getOrCreate(id("phases"), PhaseComponent.class);

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
        // Block Item pickups if the player has not unlocked all required stages for the Item.
        PlayerEvent.PICKUP_ITEM_PRE.register((player, itemEntity, stack) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? ActionResult.FAIL : ActionResult.PASS;
        });

        // Block Item use
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? TypedActionResult.fail(stackInHand) : TypedActionResult.pass(stackInHand);
        });

        // Prevent attacking with blocked items
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? ActionResult.FAIL : ActionResult.PASS;
        });

        // Prevent breaking with blocked items
        AttackBlockCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !allowed ? ActionResult.FAIL : ActionResult.PASS;
        });

        // When a player ticks, check their held item. If the held item is blocked, drop it.
        TickEvent.PLAYER_PRE.register(player -> {
            for(Hand hand : Hand.values()) {
                ItemStack stack = player.getStackInHand(hand);
                Item item = stack.getItem();
                boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
                if(!allowed) {
                    player.dropStack(stack.copy());
                    stack.decrement(stack.getCount());
                }
            }
        });
    }

    public static Identifier id(String name) {
        return new Identifier("gamephases", name);
    }

    public static PhaseComponent getPhaseData(PlayerEntity player) {
        return PHASES.get(player);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(PlayerEntity.class, PHASES, PhaseComponent::new);
    }
}
