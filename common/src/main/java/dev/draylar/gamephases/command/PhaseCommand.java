package dev.draylar.gamephases.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.draylar.gamephases.GamePhases;
import dev.draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhaseCommand {

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, selection) -> {
            LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("phase")
                    .requires(source -> source.hasPermissionLevel(2))
                    .build();

            var revokeAll = CommandManager.literal("revoke_all")
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                            .executes(context -> {
                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                for (ServerPlayerEntity player : players) {
                                    GamePhasesEventJS.getPhases().forEach((key, phase) -> {
                                        GamePhases.getPhaseData(player).phases$set(key, false);
                                    });
                                }

                                context.getSource().sendFeedback(new LiteralText(String.format("Revoked all phases to %d players.", players.size())).formatted(Formatting.GRAY), false);
                                return 1;
                            })).build();

            var grantAll = CommandManager.literal("grant_all")
                    .then(CommandManager.argument("players", EntityArgumentType.players())
                            .executes(context -> {
                                Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
                                for (ServerPlayerEntity player : players) {
                                    GamePhasesEventJS.getPhases().forEach((key, phase) -> {
                                        GamePhases.getPhaseData(player).phases$set(key, true);
                                    });
                                }

                                context.getSource().sendFeedback(new LiteralText(String.format("Granted all phases to %d players.", players.size())).formatted(Formatting.GRAY), false);
                                return 1;
                            })).build();

            var grant = CommandManager.literal("grant")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("phase", StringArgumentType.word())
                                    .suggests((context, builder) -> {
                                        GamePhasesEventJS.getPhases().keySet().forEach(builder::suggest);
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        PlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        String phase = StringArgumentType.getString(context, "phase");
                                        GamePhases.getPhaseData(target).phases$set(phase, true);
                                        context.getSource().sendFeedback(new LiteralText(String.format("Granted phase \"%s\" to %s.", phase, target.getName().asString())).formatted(Formatting.GRAY), false);
                                        return 1;
                                    })))
                    .build();

            var revoke = CommandManager.literal("revoke")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .then(CommandManager.argument("phase", StringArgumentType.word())
                                    .suggests(PhaseCommand::suggestObtainedPhases)
                                    .executes(context -> {
                                        PlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                        String phase = StringArgumentType.getString(context, "phase");
                                        GamePhases.getPhaseData(target).phases$set(phase, false);
                                        context.getSource().sendFeedback(new LiteralText(String.format("Revoked phase \"%s\" from %s.", phase, target.getName().asString())).formatted(Formatting.GRAY), false);
                                        return 1;
                                    })))
                    .build();

            var status = CommandManager.literal("status")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                            .executes(context -> {
                                ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                List<String> unlocked = GamePhases.getPhaseData(target)
                                        .phase$getUnlocked()
                                        .entrySet()
                                        .stream()
                                        .filter(Map.Entry::getValue)
                                        .map(Map.Entry::getKey)
                                        .collect(Collectors.toList());

                                String playerName = target.getEntityName();
                                context.getSource().sendFeedback(new LiteralText(
                                        "%s has unlocked the following phases: [ %s ]".formatted(playerName, String.join(", ", unlocked))
                                ).formatted(Formatting.GRAY), false);

                                return 1;
                            })).build();

            root.addChild(revoke);
            root.addChild(grant);
            root.addChild(status);
            root.addChild(revokeAll);
            root.addChild(grantAll);
            dispatcher.getRoot().addChild(root);
        });
    }

    private static CompletableFuture<Suggestions> suggestObtainedPhases(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        try {
            for (String phase : GamePhasesEventJS.getPhases().keySet()) {
                if(GamePhases.getPhaseData(EntityArgumentType.getPlayer(context, "player")).phases$has(phase)) {
                    builder.suggest(phase);
                }
            }
        } catch (CommandSyntaxException exception) {
            throw new RuntimeException(exception);
        }


        return builder.buildFuture();
    }

    private PhaseCommand() {
        // NO-OP
    }
}
