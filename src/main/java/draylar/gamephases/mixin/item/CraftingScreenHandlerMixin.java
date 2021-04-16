package draylar.gamephases.mixin.item;

import draylar.gamephases.api.Phase;
import draylar.gamephases.kube.GamePhasesEventJS;
import draylar.gamephases.mixin.IngredientAccessor;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.Map;

@Mixin(CraftingScreenHandler.class)
public class CraftingScreenHandlerMixin {

    @Redirect(
            method = "updateResult",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingResultInventory;shouldCraftRecipe(Lnet/minecraft/world/World;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/recipe/Recipe;)Z"))
    private static boolean checkPhase(CraftingResultInventory craftingResultInventory, World world, ServerPlayerEntity player, Recipe<?> recipe) {
        if(!craftingResultInventory.shouldCraftRecipe(world, player, recipe)) {
            return false;
        }

        ItemStack output = recipe.getOutput();
        DefaultedList<Ingredient> ingredients = recipe.getPreviewInputs();

        // If the output or ingredient contains a recipe the player has not unlocked, prevent the craft.
        for (Map.Entry<String, Phase> entry : GamePhasesEventJS.getPhases().entrySet()) {
            Phase phase = entry.getValue();

            // check output
            if (phase.restricts(output.getItem())) {
                if(!phase.hasUnlocked(player)) {
                    return false;
                }
            }

            // Check if any ingredient is disallowed
            boolean disallowed = ingredients.stream().map(ingredient -> Arrays.asList(((IngredientAccessor) (Object) ingredient).getMatchingStacks())).anyMatch(itemStacks -> {
                for (ItemStack stack : itemStacks) {
                    if(phase.restricts(stack.getItem())) {
                        return true;
                    }
                }

                return false;
            });

            if(disallowed) {
                if(!phase.hasUnlocked(player)) {
                    return false;
                }
            }
        }

        return true;
    }
}
