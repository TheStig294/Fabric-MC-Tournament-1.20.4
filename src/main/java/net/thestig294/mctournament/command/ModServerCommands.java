package net.thestig294.mctournament.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.thestig294.mctournament.MCTournament;
import net.thestig294.mctournament.minigame.Minigame;
import net.thestig294.mctournament.minigame.MinigameVariants;
import net.thestig294.mctournament.minigame.Minigames;
import net.thestig294.mctournament.network.ModNetworking;
import net.thestig294.mctournament.tournament.Tournament;
import net.thestig294.mctournament.tournament.TournamentSettings;

public class ModServerCommands {
    public static void registerServerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher,
                                                    registryAccess, environment) ->
                dispatcher.register(CommandManager.literal(MCTournament.MOD_ID)
                                .then(CommandManager.literal("start")
                                                .requires(source -> source.hasPermissionLevel(2))
                                                .then(CommandManager.argument("minigame", StringArgumentType.word())
                                                                .suggests(ModSuggestionProviders.MINIGAME_IDS)
                                                                .executes(context ->
                                                                        executeStart(context.getSource(), StringArgumentType.getString(context, "minigame")))
                                                )
                                )
                                .then(CommandManager.literal("clearscreen")
                                                .requires(ServerCommandSource::isExecutedByPlayer)
                                                .executes(context -> executeClearScreen(context.getSource()))
                                )
                ));
    }

    private static int executeStart(ServerCommandSource source, String id) {
        Minigame minigame = Minigames.get(id);
        if (minigame == null) {
            source.sendError(Text.translatable("commands.mctournament.start.error"));
            return 0;
        }

        Vec3d position = source.getPosition();
        BlockPos blockPos = new BlockPos((int) position.x, (int) position.y, (int) position.z);

        Tournament.inst().serverSetup(new TournamentSettings()
                .minigames(minigame)
                .variants(MinigameVariants.DEFAULT)
                .position(blockPos)
        );

        Tournament.inst().endCurrentMinigame(false);

        source.sendFeedback(() -> Text.translatable("commands.mctournament.start", minigame.getName()), true);
        return 1;
    }

    private static int executeClearScreen(ServerCommandSource source) {
        ModNetworking.send(ModNetworking.COMMAND_CLEAR_SCREEN, source.getPlayer());
        return 1;
    }
}
