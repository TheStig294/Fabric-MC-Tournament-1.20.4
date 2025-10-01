package net.thestig294.mctournament.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import net.thestig294.mctournament.minigame.Minigames;

import java.util.concurrent.CompletableFuture;

public class ModSuggestionProviders {
    public static final MinigameSuggestionProvider MINIGAME_IDS = new MinigameSuggestionProvider();

    public static class MinigameSuggestionProvider implements SuggestionProvider<ServerCommandSource> {
        @Override
        public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
            Minigames.getMinigameIds().forEach(builder::suggest);
            return builder.buildFuture();
        }
    }
}
