package net.thestig294.mctournament.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface MinecraftClientInvoker {
    @Invoker("openChatScreen")
    void callOpenChatScreen(String text);
}
