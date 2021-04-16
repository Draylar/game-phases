package draylar.gamephases;

import dev.latvian.kubejs.item.ItemTooltipEventJS;
import dev.latvian.kubejs.script.ScriptType;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistryV3;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import draylar.gamephases.cca.PhaseComponent;
import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.architectury.event.events.TooltipEvent;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;

import java.util.Map;

public class GamePhases implements ModInitializer, EntityComponentInitializer {

    public static final ComponentKey<PhaseComponent> PHASES = ComponentRegistryV3.INSTANCE.getOrCreate(id("phases"), PhaseComponent.class);

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            new GamePhasesEventJS().post(ScriptType.SERVER, "gamephases.initialize");
        });

        // When resource packs are reloaded, load Game Phase data.
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, n) -> {
            new GamePhasesEventJS().post(ScriptType.SERVER, "gamephases.initialize");
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
            boolean b = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !b ? ActionResult.FAIL : ActionResult.PASS;
        });

        // Block Item use
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stackInHand = player.getStackInHand(hand);
            Item item = stackInHand.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean b = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.disallows(item)).allMatch(phase -> phase.hasUnlocked(player));
            return !b ? TypedActionResult.fail(stackInHand) : TypedActionResult.pass(stackInHand);
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
