package dev.draylar.gamephases.kube;

import dev.architectury.networking.NetworkManager;
import dev.draylar.gamephases.api.Phase;
import dev.draylar.gamephases.network.ServerNetworking;
import dev.latvian.mods.kubejs.event.EventJS;
import io.netty.buffer.Unpooled;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class GamePhasesEventJS extends EventJS {

    private static final Map<String, Phase> PHASES = new HashMap<>();
    private final MinecraftServer server;

    public GamePhasesEventJS(MinecraftServer server) {
        this.server = server;
        PHASES.clear();
    }

    public Phase phase(String id) {
        Phase phase = new Phase(id, server.getRecipeManager());
        PHASES.put(id, phase);
        return phase;
    }

    public static Map<String, Phase> getPhases() {
        return PHASES;
    }

    public static void sync(ServerPlayerEntity player) {
        sync(player, createBuffer());
    }

    public static void sync(MinecraftServer server) {
        NetworkManager.sendToPlayers(
                server.getPlayerManager().getPlayerList(),
                ServerNetworking.ALL_PHASE_SYNC_ID,
                createBuffer()
        );
    }

    private static void sync(ServerPlayerEntity player, PacketByteBuf buffer) {
        NetworkManager.sendToPlayer(
                player,
                ServerNetworking.ALL_PHASE_SYNC_ID,
                buffer
        );
    }

    private static PacketByteBuf createBuffer() {
        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());

        // Save phases to packet
        NbtCompound tag = new NbtCompound();
        NbtList l = new NbtList();
        PHASES.forEach((id, phase) -> {
            NbtCompound inner = new NbtCompound();
            inner.putString("ID", id);
            inner.put("PhaseData", phase.toTag());
            l.add(inner);
        });

        tag.put("Phases", l);
        packet.writeNbt(tag);
        return packet;
    }
}
