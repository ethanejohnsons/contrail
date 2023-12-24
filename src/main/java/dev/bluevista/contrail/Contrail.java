package dev.bluevista.contrail;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * This class represents a single contrail rendered in the sky.
 */
@Environment(EnvType.CLIENT)
public class Contrail {

	public static final Identifier TEXTURE = new Identifier(ContrailMod.MODID, "textures/contrail.png");

	private final Vec2f direction;

	private Vec2f position;
	private Vec2f prevPosition;
	private double length;
	private double prevLength;

	public Contrail() {
		this.position = getRandomOrigin();
		this.direction = getDirectionTowardsPlayer();

		if (ContrailMod.isDev()) {
			ContrailMod.LOGGER.info(String.format("Spawned contrail at (%f, %f).", position.x, position.y));
		}
	}

	public void tick() {
		var config = ContrailConfig.getInstance();

		// check if this contrail is out of range, and if so, remove it
		int viewDistance = MinecraftClient.getInstance().options.getClampedViewDistance() * 16 * 2;
		if (getHorizontalDistanceFromPlayer() > config.length + viewDistance) {
			ContrailMod.CONTRAILS.remove(this);
			return;
		}

		// update interpolation values for position
		prevPosition = position;
		position = position.add(new Vec2f(direction.x * (float) config.speed * 0.01f * -1.0f, direction.y * (float) config.speed * 0.01f * -1.0f));

		// update interpolation values for length
		prevLength = length;
		length = Math.min(config.length, length + config.speed * 0.01);
	}

	public void render(WorldRenderContext ctx) {
		var config = ContrailConfig.getInstance();
		if (ctx.world() == null) return; // just in case :P
		if (ctx.world().getDimensionEffects().getSkyType() != DimensionEffects.SkyType.NORMAL) return;

		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		RenderSystem.setShaderTexture(0, TEXTURE);

		// interpolated values
		var position = new Vec2f(MathHelper.lerp(ctx.tickDelta(), prevPosition.x, this.position.x), MathHelper.lerp(ctx.tickDelta(), prevPosition.y, this.position.y));
		double length = MathHelper.lerp(ctx.tickDelta(), prevLength, this.length);

		double increment = 20.0;
		long time = ctx.world().getTimeOfDay();
		float timeFactor = MathHelper.clamp(Math.abs(18000 - time) / 18000f, 0.25f, 0.75f);

		ctx.matrixStack().push();
		ctx.matrixStack().translate(position.x - ctx.camera().getPos().x, config.height - ctx.camera().getPos().y, position.y - ctx.camera().getPos().z);

		var buffer = Tessellator.getInstance().getBuffer();

		for (double i = 0.0; i < length; i += increment) {
			float alpha = (1.0f - (1.0f / (float) length) * (float) i) * timeFactor;
			RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

			ctx.matrixStack().push();
			ctx.matrixStack().translate(direction.x * i, 0.0, direction.y * i); // contrail moves in negative direction, so we extend the tail in the positive direction
			ctx.matrixStack().multiply(RotationAxis.NEGATIVE_Y.rotation((float) Math.atan2(direction.y, direction.x)));
			ctx.matrixStack().multiply(RotationAxis.NEGATIVE_X.rotationDegrees(180));

			// draw a square
			var pos = ctx.matrixStack().peek().getPositionMatrix();
			buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			buffer.vertex(pos, 0.0f, 0.0f, 0.0f).texture(0, 0).next();
			buffer.vertex(pos, 0.0f, 0.0f, (float) config.thickness * 0.1f).texture(0, 1).next();
			buffer.vertex(pos, (float) increment, 0.0f, (float) config.thickness * 0.1f).texture(1, 1).next();
			buffer.vertex(pos, (float) increment, 0.0f, 0.0f).texture(1, 0).next();

			Tessellator.getInstance().draw();
			ctx.matrixStack().pop();
		}

		ctx.matrixStack().pop();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
	}

	private double getHorizontalDistanceFromPlayer() {
		var player = MinecraftClient.getInstance().player;
		if (player == null) return 0.0; // just in case :P
		return player.getPos().distanceTo(new Vec3d(position.x, player.getY(), position.y));
	}

	private Vec2f getDirectionTowardsPlayer() {
		var rand = new Random();
		var player = MinecraftClient.getInstance().player;
		if (player == null) return new Vec2f(1.0f, 0.0f); // just in case :P
		return new Vec2f(
			position.x - (float) player.getX() + (rand.nextFloat() - 0.5f) * 256,
			position.y - (float) player.getZ() + (rand.nextFloat() - 0.5f) * 256
		).normalize();
	}

	private Vec2f getRandomOrigin() {
		var rand = new Random();
		var client = MinecraftClient.getInstance();
		if (client.player == null) return new Vec2f(0.0f, 0.0f); // just in case :P
		int viewDistance = client.options.getClampedViewDistance() * 16 * 2;
		return new Vec2f(
			(float) (client.player.getX() + (viewDistance * Math.cos(rand.nextDouble() * 2 * Math.PI))),
			(float) (client.player.getZ() + (viewDistance * Math.sin(rand.nextDouble() * 2 * Math.PI)))
		);
	}

}
