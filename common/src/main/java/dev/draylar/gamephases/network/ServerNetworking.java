package dev.draylar.gamephases.network;

import dev.architectury.networking.NetworkManager;
import dev.draylar.gamephases.GamePhases;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

public class ServerNetworking {

    public static final Identifier UNLOCKED_PHASE_SYNC = GamePhases.id("unlocked_phase_sync");
    public static final Identifier ALL_PHASE_SYNC_ID = GamePhases.id("all_phase_sync");

    public static void sendPhaseSync(ServerPlayerEntity player, Map<String, Boolean> phases) {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

        // write size to packet
        packet.writeInt(phases.size());

        // write each pair to packet
        phases.forEach((phase, value) -> {
            packet.writeString(phase);
            packet.writeBoolean(value);
        });

        NetworkManager.sendToPlayer(player, UNLOCKED_PHASE_SYNC, packet);
    }
}
