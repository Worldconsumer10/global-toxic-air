package com.ubunifu.toxicair;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.ubunifu.toxicair.blocks.ModBlockEntities;
import com.ubunifu.toxicair.blocks.ModBlocks;
import com.ubunifu.toxicair.effect.ModEffects;
import com.ubunifu.toxicair.itemgroups.ModItemGroups;
import com.ubunifu.toxicair.potions.ModPotionRecipe;
import com.ubunifu.toxicair.potions.ModPotions;
import com.ubunifu.toxicair.toxins.AirHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ToxicAir implements ModInitializer {
	public static final String MOD_ID = "toxic-air";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModEffects.Register();
		ModPotions.Register();
		ModPotionRecipe.Register();
		ModBlocks.Register();
		ModBlockEntities.Register();
		ModItemGroups.Register();
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds())
				AirHandler.tick(server,world);
		});
		HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.hudHidden) return;

			float toxicity = AirHandler.getToxicity(
					BlockPos.ofFloored(client.player.getEyePos()),
					client.player.getWorld()
			);

			if (toxicity <= 0) return;

			renderToxicVignette(toxicity / 100f);
		});
	}
	private static final Identifier GREEN_VIGNETTE_TEXTURE = new Identifier(MOD_ID,"textures/misc/hud_vignette.png");

	private static void renderToxicVignette(float intensity) {
		MinecraftClient client = MinecraftClient.getInstance();
		RenderSystem.disableDepthTest();
		RenderSystem.depthMask(false);
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SrcFactor.ZERO, GlStateManager.DstFactor.ONE);
		RenderSystem.setShaderColor(0.1f, 0.089f, 0f, MathHelper.clamp(intensity, 0f, 1f)); // GREEN TINT

		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, GREEN_VIGNETTE_TEXTURE);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		buffer.vertex(0, client.getWindow().getScaledHeight(), -90).texture(0f, 1f).next();
		buffer.vertex(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), -90).texture(1f, 1f).next();
		buffer.vertex(client.getWindow().getScaledWidth(), 0, -90).texture(1f, 0f).next();
		buffer.vertex(0, 0, -90).texture(0f, 0f).next();
		tessellator.draw();

		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.enableDepthTest();
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
}