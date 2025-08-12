package net.thestig294.mctournament.minigame.triviamurderparty.widget;

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
    private final Identifier font;
    private Text updatedText;
    private int updatedColor;
    private boolean textUpdated;
    private int intValue;
    private final int originalX;
    private final int originalY;

    private final int maxWidth;
    private final OptionalInt maxRows;
    private final CachedMapper<QuestionText.CacheKey, MultilineText> cacheKeyToText;

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, Color color, int maxWidth, TextRenderer textRenderer){
        this(x, y, text, font, lineHeight, color.getRGB(), maxWidth, textRenderer, 0);
    }

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, int color, int maxWidth, TextRenderer textRenderer, int intValue) {
        super(x, y, Text.literal(text).styled(style -> style.withFont(font)), textRenderer);
        this.lineHeight = lineHeight;
        this.font = font;
        this.updatedText = Text.empty();
        this.updatedColor = color;
        this.textUpdated = false;
        this.intValue = intValue;
        this.originalX = x;
        this.originalY = y;

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
        context.setShaderColor(1.0f, 1.0f, 1.0f, this.alpha);
        if (this.textUpdated) {
            context.drawCenteredTextWithShadow(this.getTextRenderer(), this.updatedText,
                    this.getX(), this.getY() - this.getHeight(), this.updatedColor);
        } else {
            MultilineText multilineText = this.cacheKeyToText.map(this.getCacheKey());
            multilineText.drawCenterWithShadow(context, this.getX(),
                    this.getY() - this.getHeight(), this.lineHeight, this.getTextColor());
        }
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void setText(String string) {
        this.setText(string, this.updatedColor);
    }

    public void setText(String string, Color color) {
        this.setText(string, color.getRGB());
    }

    public void setText(String string, int color) {
        this.setText(Text.literal(string).styled(style -> style.withFont(this.font)), color);
    }

    public void setText(Text text, Color color) {
        setText(text, color.getRGB());
    }

    public void setText(Text text, int color) {
        this.updatedText = text;
        this.updatedColor = color;
        this.textUpdated = true;
    }

    public int getInt() {
        return this.intValue;
    }

    public void setInt(int intValue){
        this.intValue = intValue;
        this.setText(Integer.toString(intValue));
    }

    public int getOriginalX() {
        return this.originalX;
    }

    public int getOriginalY() {
        return this.originalY;
    }

    private QuestionText.CacheKey getCacheKey() {
        return new QuestionText.CacheKey(this.getMessage(), this.maxWidth, this.maxRows);
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}
