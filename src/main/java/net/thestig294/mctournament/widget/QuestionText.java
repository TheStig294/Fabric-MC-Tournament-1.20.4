package net.thestig294.mctournament.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Colors;
import net.minecraft.util.Util;
import net.thestig294.mctournament.font.ModFonts;

import java.util.OptionalInt;

public class QuestionText extends MultilineTextWidget {
    private final Screen screen;
    private final int lineHeight;

    private final int maxWidth;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt maxRows;
    private final CachedMapper<QuestionText.CacheKey, MultilineText> cacheKeyToText;

    public QuestionText(Screen screen, int x, int y, String text, TextRenderer textRenderer) {
        super(x, y, Text.literal(text).styled(style -> style.withFont(ModFonts.QUESTION)), textRenderer);
        this.screen = screen;
        this.lineHeight = 20;
        this.setTextColor(Colors.WHITE);

        this.cacheKeyToText = Util.cachedMapper(
                cacheKey -> cacheKey.maxRows.isPresent()
                        ? MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt())
                        : MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth)
        );

        this.maxWidth = this.screen.width / 2;
        this.maxRows = OptionalInt.of(10);

        this.setMaxWidth(this.maxWidth);
        this.setMaxRows(this.maxRows.getAsInt());
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            player.sendMessage(Text.literal("You clicked the question text?"), true);
            this.screen.close();
        }
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MultilineText multilineText = this.cacheKeyToText.map(this.getCacheKey());
        multilineText.drawCenterWithShadow(context, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, this.getTextColor());
    }

    private QuestionText.CacheKey getCacheKey() {
        return new QuestionText.CacheKey(this.getMessage(), this.maxWidth, this.maxRows);
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}
