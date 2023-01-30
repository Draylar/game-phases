package dev.draylar.gamephases.arch;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public interface AttackEvent {

    /**
     * @see AttackEvent.PickupItem#attack(PlayerEntity, ItemEntity, ItemStack)
     */
    Event<AttackEvent.PickupItem> ATTACK_ENTITY = EventFactory.createLoop();

    interface PickupItem {

        /**
         * Invoked when a player has picked up an {@link ItemEntity}.
         * Equivalent to Forge's {@code ItemPickupEvent} event.
         *
         * @param player The player.
         * @param entity The {@link ItemEntity} that the player picked up.
         * @param stack  The content of the {@link ItemEntity}.
         */
        EventResult attack(PlayerEntity player, World world, Hand hand, Entity target, HitResult result);
    }
}
