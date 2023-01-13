package draylar.gamephases.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class PhaseCommand {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("phase")
                    .requires(source -> source.hasPermissionLevel(2))
                    .build();

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
