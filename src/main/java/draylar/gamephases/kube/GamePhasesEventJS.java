package draylar.gamephases.kube;

import dev.latvian.kubejs.event.EventJS;
import draylar.gamephases.GamePhases;
import draylar.gamephases.api.Phase;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;

import java.util.HashMap;
import java.util.Map;

public class GamePhasesEventJS extends EventJS {

    private static final Map<String, Phase> PHASES = new HashMap<>();

    public GamePhasesEventJS() {
        PHASES.clear();
    }

    public Phase phase(String id) {
        Phase phase = new Phase(id);
        PHASES.put(id, phase);
        return phase;
    }

    public static Map<String, Phase> getPhases() {
        return PHASES;
    }

    public static void sync(MinecraftServer server) {
        PacketByteBuf packet = PacketByteBufs.create();

        // Save phases to packet
        CompoundTag tag = new CompoundTag();
        ListTag l = new ListTag();
        PHASES.forEach((id, phase) -> {
            CompoundTag inner = new CompoundTag();
            inner.putString("ID", id);
            inner.put("PhaseData", phase.toTag());
            l.add(inner);
        });

        tag.put("Phases", l);
        packet.writeCompoundTag(tag);

        // Send packet to all players
        server.getPlayerManager().getPlayerList().forEach(player -> {
            player.networkHandler.sendPacket(ServerPlayNetworking.createS2CPacket(GamePhases.PHASE_SYNC_ID,  packet));
        });
    }
}
