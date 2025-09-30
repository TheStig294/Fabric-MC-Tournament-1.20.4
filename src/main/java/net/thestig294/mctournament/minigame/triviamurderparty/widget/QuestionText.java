package net.thestig294.mctournament.minigame.triviamurderparty.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.CachedMapper;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;
import java.util.OptionalInt;

public class QuestionText extends MultilineTextWidget implements QuestionWidget {
    private final int lineHeight;
    private final Identifier font;
    private MultilineText multilineText;
    private int updatedColor;
    private boolean textUpdated;
    private int intValue;
    private final int originalX;
    private final int originalY;
    private final int originalWidth;
    private final int originalHeight;

    private final int maxWidth;
//    Kinda forced to do this as Minecraft expects an Optional method parameter for a method we're using...
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final OptionalInt maxRows;
    private final CachedMapper<QuestionText.CacheKey, MultilineText> cacheKeyToText;

    public QuestionText(int x, int y, Text text, int lineHeight, Color color, int maxWidth, TextRenderer textRenderer){
        this(x, y, text.getString(), text.getStyle().getFont(), lineHeight, color.getRGB(), maxWidth, textRenderer, 0);
    }

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, Color color, int maxWidth, TextRenderer textRenderer){
        this(x, y, text, font, lineHeight, color.getRGB(), maxWidth, textRenderer, 0);
    }

    public QuestionText(int x, int y, String text, Identifier font, int lineHeight, int color, int maxWidth, TextRenderer textRenderer, int intValue) {
        super(x, y, Text.literal(text).styled(style -> style.withFont(font)), textRenderer);
        this.lineHeight = lineHeight;
        this.font = font;
        this.multilineText = MultilineText.EMPTY;
        this.updatedColor = color;
        this.textUpdated = false;
        this.intValue = intValue;
        this.originalX = x;
        this.originalY = y;
        this.originalWidth = this.width;
        this.originalHeight = this.height;

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
            this.multilineText.drawCenterWithShadow(context, this.getX(),
                    this.getY() - this.getHeight(), this.lineHeight, this.updatedColor);
        } else {
            MultilineText multilineText = this.cacheKeyToText.map(this.getCacheKey());
            multilineText.drawCenterWithShadow(context, this.getX(),
                    this.getY() - this.getHeight(), this.lineHeight, this.getTextColor());
        }

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {

    }

    public void setText(String translationString) {
        this.setText(translationString, this.updatedColor);
    }

    public void setText(String translationString, Color color) {
        this.setText(translationString, color.getRGB());
    }

    public void setText(String translationString, int color) {
        this.setText(Text.translatable(translationString).styled(style -> style.withFont(this.font)), color);
    }

    public void setText(Text text, int color) {
        this.updatedColor = color;
        this.multilineText = MultilineText.create(this.getTextRenderer(), text, this.maxWidth);
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

    @Override
    public int getOriginalWidth() {
        return this.originalWidth;
    }

    @Override
    public int getOriginalHeight() {
        return this.originalHeight;
    }

    private QuestionText.CacheKey getCacheKey() {
        return new QuestionText.CacheKey(this.getMessage(), this.maxWidth, this.maxRows);
    }

    @Override
    public float getAlpha() {
        return this.alpha;
    }

    @Environment(EnvType.CLIENT)
    record CacheKey(Text message, int maxWidth, OptionalInt maxRows) {
    }
}
