package dev.draylar.gamephases.mixin.item;

import dev.draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class ScreenHandlerMixin {

    @Shadow public abstract ItemStack getStack();

    @Inject(
            method = "canTakeItems",
            at = @At("HEAD"), cancellable = true)
    private void denySlotInteraction(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        Item item = getStack().getItem();

        // Check all registered Phases.
        // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
        boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
        if(!allowed) {
            cir.setReturnValue(false);
        }
    }
}
