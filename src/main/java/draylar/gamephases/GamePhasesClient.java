package draylar.gamephases;

import draylar.gamephases.api.Phase;
import draylar.gamephases.kube.GamePhasesEventJS;
import me.shedaniel.architectury.event.events.TooltipEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class GamePhasesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GamePhases.PHASE_SYNC_ID, (client, handler, buf, responseSender) -> {
            // clear existing phases
            GamePhasesEventJS.getPhases().clear();

            // read phases and add to local collection
            CompoundTag compoundTag = buf.readCompoundTag();
            ListTag phases = compoundTag.getList("Phases", NbtType.COMPOUND);
            phases.forEach(phaseTag -> {
                CompoundTag inner = (CompoundTag) phaseTag;
                String id = inner.getString("ID");
                Phase phase = Phase.fromTag(inner.getCompound("PhaseData"));
                GamePhasesEventJS.getPhases().put(id, phase);
            });
        });

        registerItemHandlers();
    }

    private void registerItemHandlers() {
        // Render a blank tooltip when the user does not have permission to use the item.
        TooltipEvent.ITEM.register((stack, list, context) -> {
            Item item = stack.getItem();

            // Check all registered Phases.
            // If a phase blacklists the given item and the player does not have it unlocked, stop the interaction.
            boolean b = GamePhasesEventJS.getPhases().values().stream().filter(phase -> phase.restricts(item)).allMatch(phase -> phase.hasUnlocked(MinecraftClient.getInstance().player));
            if (!b) {
                list.clear();
                list.add(new LiteralText("Locked").formatted(Formatting.RED));
            }
        });
    }
}
