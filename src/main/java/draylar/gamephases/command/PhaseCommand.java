package draylar.gamephases.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class PhaseCommand {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("phase")
                    .requires(source -> source.hasPermissionLevel(2))
                    .build();

            LiteralCommandNode<ServerCommandSource> grant = CommandManager.literal("grant")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("phase", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                GamePhasesEventJS.getPhases().keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                PlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                String phase = StringArgumentType.getString(context, "phase");
                                GamePhases.getPhaseData(target).set(phase, true);
                                return 1;
                            })))
                    .build();

            LiteralCommandNode<ServerCommandSource> revoke = CommandManager.literal("revoke")
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                    .then(CommandManager.argument("phase", StringArgumentType.word())
                            .suggests((context, builder) -> {
                                GamePhasesEventJS.getPhases().keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                PlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                String phase = StringArgumentType.getString(context, "phase");
                                GamePhases.getPhaseData(target).set(phase, false);
                                return 1;
                            })))
                    .build();

            root.addChild(revoke);
            root.addChild(grant);
            dispatcher.getRoot().addChild(root);
        });
    }

    private PhaseCommand() {
        // NO-OP
    }
}
