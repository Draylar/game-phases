package draylar.gamephases.network;

import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ServerNetworking {

    public static final Identifier UNLOCKED_PHASE_SYNC = GamePhases.id("unlocked_phase_sync");

    public static void sendPhaseSync(ServerPlayerEntity player, Map<String, Boolean> phases) {
        PacketByteBuf packet = PacketByteBufs.create();

        // write size to packet
        packet.writeInt(phases.size());

        // write each pair to packet
        phases.forEach((phase, value) -> {
            packet.writeString(phase);
            packet.writeBoolean(value);
        });

        ServerPlayNetworking.send(player, UNLOCKED_PHASE_SYNC, packet);
    }
}
