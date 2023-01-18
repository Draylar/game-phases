package draylar.gamephases.mixin.loot;

import draylar.gamephases.api.Phase;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin {

    @Unique
    private LootContext context;

    @Inject(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
    private void gamePhases$storeLootContext(LootContext context, Consumer<ItemStack> lootConsumer, CallbackInfo ci) {
        this.context = context;
    }

    @Redirect(method = "generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/loot/LootTable;processStacks(Ljava/util/function/Consumer;)Ljava/util/function/Consumer;"))
    private Consumer<ItemStack> gamePhases$preventLockedLootDrops(Consumer<ItemStack> lootConsumer) {
        return stack -> {
            Entity source = null;
            if(context.hasParameter(LootContextParameters.DIRECT_KILLER_ENTITY)) {
                source = context.get(LootContextParameters.DIRECT_KILLER_ENTITY);
            } else if(context.hasParameter(LootContextParameters.KILLER_ENTITY)) {
                source = context.get(LootContextParameters.KILLER_ENTITY);
            } else if(context.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER)) {
                source = context.get(LootContextParameters.LAST_DAMAGE_PLAYER);
            } else if(context.hasParameter(LootContextParameters.THIS_ENTITY)) {
                source = context.get(LootContextParameters.THIS_ENTITY);
            }

            if(source instanceof PlayerEntity player) {
                Item dropped = stack.getItem();
                for (Phase phase : GamePhasesEventJS.getPhases().values()) {
                    if(phase.restricts(dropped) && !phase.hasUnlocked(player)) {
                        return;
                    }
                }
            }

            lootConsumer.accept(stack);
        };
    }
}
