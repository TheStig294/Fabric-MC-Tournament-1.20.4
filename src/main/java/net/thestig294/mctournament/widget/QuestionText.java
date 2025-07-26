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
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.OptionalInt;

public class QuestionText extends MultilineTextWidget {
    private final Screen screen;
    private final int lineHeight;
    private Text updatedText;
    private int updatedColor;
    private int intValue;

    private final int maxWidth;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt maxRows;
    private final CachedMapper<QuestionText.CacheKey, MultilineText> cacheKeyToText;

    public QuestionText(Screen screen, int x, int y, String text, Identifier font, int lineHeight, int color, TextRenderer textRenderer){
        this(screen, x, y, text, font, lineHeight, color, textRenderer, 0);
    }

    public QuestionText(Screen screen, int x, int y, String text, Identifier font, int lineHeight, int color, TextRenderer textRenderer, int intValue) {
        super(x, y, Text.literal(text).styled(style -> style.withFont(font)), textRenderer);
        this.screen = screen;
        this.lineHeight = lineHeight;
        this.updatedText = Text.empty();
        this.updatedColor = 0;
        this.intValue = intValue;

        this.cacheKeyToText = Util.cachedMapper(
                cacheKey -> cacheKey.maxRows.isPresent()
                        ? MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt())
                        : MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth)
        );

        this.maxWidth = this.screen.width / 2;
        this.maxRows = OptionalInt.of(10);

        this.setTextColor(color);
        this.setMaxWidth(this.maxWidth);
        this.setMaxRows(this.maxRows.getAsInt());
        this.setCentered(true);
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
        if (updatedText.equals(Text.empty())) {
            MultilineText multilineText = this.cacheKeyToText.map(this.getCacheKey());
            multilineText.drawCenterWithShadow(context, this.getX() + this.getWidth() / 2, this.getY(), this.lineHeight, this.getTextColor());
        } else {
            context.drawCenteredTextWithShadow(this.getTextRenderer(), this.updatedText, this.getX() + this.getWidth() / 2, this.getY(), this.updatedColor);
        }
    }

    public void updateText(Text text, int color) {
        this.updatedText = text;
        this.updatedColor = color;
    }

    public int getIntValue() {
        return this.intValue;
    }

    public void setIntValue(int intValue){
        this.intValue = intValue;
    }

    private QuestionText.CacheKey getCacheKey() {
        return new QuestionText.CacheKey(this.getMessage(), this.maxWidth, this.maxRows);
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}
