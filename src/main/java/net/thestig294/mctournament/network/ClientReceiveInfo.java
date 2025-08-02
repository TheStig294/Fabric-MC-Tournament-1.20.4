package net.thestig294.mctournament.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public record ClientReceiveInfo(
        MinecraftClient client,
        ClientPlayNetworkHandler handler,
        PacketByteBuf buffer,
        PacketSender responseSender
) {}
