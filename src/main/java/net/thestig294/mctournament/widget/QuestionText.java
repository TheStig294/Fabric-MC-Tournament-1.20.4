package net.thestig294.mctournament.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;
import java.util.OptionalInt;

public class QuestionText extends MultilineTextWidget {
    private final int lineHeight;
    private Text updatedText;
    private int updatedColor;
    private int intValue;

    private final int maxWidth;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt maxRows;
    private final CachedMapper<QuestionText.CacheKey, MultilineText> cacheKeyToText;

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, Color color, int maxWidth, TextRenderer textRenderer){
        this(x, y, text, font, lineHeight, color.getRGB(), maxWidth, textRenderer, 0);
    }

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, int color, int maxWidth, TextRenderer textRenderer, int intValue) {
        super(x, y, Text.literal(text).styled(style -> style.withFont(font)), textRenderer);
        this.lineHeight = lineHeight;
        this.updatedText = Text.empty();
        this.updatedColor = 0;
        this.intValue = intValue;

        this.cacheKeyToText = Util.cachedMapper(
                cacheKey -> cacheKey.maxRows.isPresent()
                        ? MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth, cacheKey.maxRows.getAsInt())
                        : MultilineText.create(textRenderer, cacheKey.message, cacheKey.maxWidth)
        );

        this.maxRows = OptionalInt.of(10);
        this.maxWidth = maxWidth;

        this.setTextColor(color);
        this.setMaxWidth(maxWidth);
        this.setMaxRows(this.maxRows.getAsInt());
        this.setCentered(true);
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

    @SuppressWarnings("unused")
    public void updateText(Text text, int color) {
        this.updatedText = text;
        this.updatedColor = color;
    }

    @SuppressWarnings("unused")
    public int getIntValue() {
        return this.intValue;
    }

    @SuppressWarnings("unused")
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
