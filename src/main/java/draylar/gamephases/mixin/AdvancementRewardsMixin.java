package draylar.gamephases.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import draylar.gamephases.GamePhases;
import draylar.gamephases.impl.AdvancementRewardsManipulator;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AdvancementRewards.class)
public class AdvancementRewardsMixin implements AdvancementRewardsManipulator {

    private List<String> phases = new ArrayList<>();

    @Inject(
            method = "fromJson",
            at = @At("RETURN"), cancellable = true)
    private static void onReturn(JsonObject json, CallbackInfoReturnable<AdvancementRewards> cir) {
        AdvancementRewards ret = cir.getReturnValue();

        // apply phrase array if it was found
        if(json.has("phases")) {
            List<String> phases = new ArrayList<>();
            JsonArray array = (JsonArray) json.get("phases");
            array.forEach(element -> phases.add(element.toString().replace("\"", "")));
            ((AdvancementRewardsManipulator) ret).setPhase(phases);
        }

        cir.setReturnValue(ret);
    }

    @Inject(
            method = "apply",
            at = @At("HEAD"))
    private void applyPhase(ServerPlayerEntity player, CallbackInfo ci) {
        phases.forEach(entry -> {
            GamePhases.getPhaseData(player).phases$set(entry, true);
        });
    }

    @Unique
    @Override
    public void setPhase(List<String> phases) {
        this.phases = phases;
    }
}
