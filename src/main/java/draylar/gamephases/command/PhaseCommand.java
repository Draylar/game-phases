package draylar.gamephases.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import draylar.gamephases.GamePhases;
import draylar.gamephases.kube.GamePhasesEventJS;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public class PhaseCommand {

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("phase")
                    .requires(source -> source.hasPermissionLevel(2))
                    .build();

            LiteralCommandNode<ServerCommandSource> grant = CommandManager.literal("grant")
                    .then(CommandManager.argument("phase", IdentifierArgumentType.identifier())
                            .suggests((context, builder) -> {
                                GamePhasesEventJS.getPhases().keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                String phase = IdentifierArgumentType.getIdentifier(context, "phase").toString();
                                GamePhases.getPhaseData(context.getSource().getPlayer()).set(new Identifier(phase), true);
                                return 1;
                            }))
                    .build();

            LiteralCommandNode<ServerCommandSource> revoke = CommandManager.literal("revoke")
                    .then(CommandManager.argument("phase", IdentifierArgumentType.identifier())
                            .suggests((context, builder) -> {
                                GamePhasesEventJS.getPhases().keySet().forEach(builder::suggest);
                                return builder.buildFuture();
                            })
                            .executes(context -> {
                                String phase = IdentifierArgumentType.getIdentifier(context, "phase").toString();
                                GamePhases.getPhaseData(context.getSource().getPlayer()).set(new Identifier(phase), false);
                                return 1;
                            }))
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
