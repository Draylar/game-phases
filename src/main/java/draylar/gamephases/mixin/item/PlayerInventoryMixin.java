package draylar.gamephases.mixin.item;

import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.TypedActionResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {

    @Shadow @Final public PlayerEntity player;
    @Shadow private ItemStack cursorStack;

    @Inject(
            method = "setCursorStack",
            at = @At("HEAD"), cancellable = true)
    private void dropCursorStack(ItemStack stack, CallbackInfo ci) {
        Item item = stack.getItem();

        // Check all registered Phases.
        // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
        boolean allowed = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(player));
        if(!allowed) {
            player.dropStack(stack);
            this.cursorStack = ItemStack.EMPTY;
            ci.cancel();
        }
    }
}
