package com.svreg.blue.look;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.svreg.blue.SvregBlue;
import com.svreg.blue.config.BlueConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Blue Grading: the whole world view (terrain, sky, mobs) is re-coloured toward the blue-hour palette of the
 * references: navy shadows instead of black, ultramarine mid tones, icy pale highlights. Colour only (no depth),
 * so it works on phones. One scene copy + one full-screen pass, only while the blue level is above zero.
 */
public final class BlueGrade {
	private static RenderPipeline copyPipeline, gradePipeline;
	private static GpuBuffer quad;
	private static GpuTexture sceneTex;
	private static GpuTextureView sceneView;
	private static boolean broken, logged;
	private static long lastFrame;

	private BlueGrade() {
	}

	private static RenderPipeline pipeline(String name) {
		Identifier sh = Identifier.fromNamespaceAndPath(SvregBlue.MOD_ID, "core/" + name);
		return RenderPipeline.builder(RenderPipeline.builder().withBindGroupLayout(BindGroupLayouts.MATRICES_PROJECTION).buildSnippet())
				.withLocation(Identifier.fromNamespaceAndPath(SvregBlue.MOD_ID, "pipeline/" + name))
				.withVertexShader(sh)
				.withFragmentShader(sh)
				.withVertexBinding(0, DefaultVertexFormat.POSITION)
				.withPrimitiveTopology(PrimitiveTopology.QUADS)
				.withCull(false)
				.withDepthStencilState(Optional.empty())
				.withColorTargetState(ColorTargetState.DEFAULT)
				.withBindGroupLayout(BindGroupLayouts.SAMPLER0)
				.build();
	}

	private static void init() {
		if (copyPipeline == null) {
			copyPipeline = pipeline("blue_copy");
			gradePipeline = pipeline("blue_grade");
		}
		if (quad == null) {
			try (ByteBufferBuilder bytes = new ByteBufferBuilder(DefaultVertexFormat.POSITION.getVertexSize() * 4)) {
				BufferBuilder b = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION);
				b.addVertex(-1, -1, 0);
				b.addVertex(1, -1, 0);
				b.addVertex(1, 1, 0);
				b.addVertex(-1, 1, 0);
				try (MeshData mesh = b.buildOrThrow()) {
					quad = RenderSystem.getDevice().createBuffer(() -> "SvregBlue quad", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX, mesh.vertexBuffer());
				}
			}
		}
	}

	/** draws the grade over the main target; safe to call more than once a frame (only the first call draws) */
	public static void render() {
		if (broken) {
			return;
		}
		float level = BlueTime.level();
		if (level <= 0.001F) {
			return;
		}
		// both hooks (SvregSky's screen look and the cloud pass) can fire in one frame: grade once
		long now = System.nanoTime();
		if (now - lastFrame < 1_500_000L) {
			return;
		}
		lastFrame = now;
		try {
			init();
			if (!logged) {
				logged = true;
				SvregBlue.LOG.info("SvregBlue: blue grade on {}", RenderSystem.getDevice().getDeviceInfo());
			}
			RenderTarget main = Minecraft.getInstance().gameRenderer.mainRenderTarget();
			int w = main.width, h = main.height;
			if (sceneTex == null || sceneTex.getWidth(0) != w || sceneTex.getHeight(0) != h) {
				if (sceneView != null) sceneView.close();
				if (sceneTex != null) sceneTex.close();
				sceneTex = RenderSystem.getDevice().createTexture(() -> "SvregBlue scene copy", 15, main.getColorTexture().getFormat(), w, h, 1, 1);
				sceneView = RenderSystem.getDevice().createTextureView(sceneTex);
			}
			Matrix4f params = new Matrix4f(
					level, BlueConfig.lift, BlueConfig.warmLights ? 1 : 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 0);
			GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(new Matrix4f(), new Vector4f(1, 1, 1, 1), new Vector3f(), params);
			var sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
			try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "SvregBlue scene copy", sceneView, Optional.empty(), null, OptionalDouble.empty())) {
				pass.setPipeline(copyPipeline);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("DynamicTransforms", transforms);
				pass.bindTexture("Sampler0", main.getColorTextureView(), sampler);
				pass.setVertexBuffer(0, quad.slice());
				pass.setIndexBuffer(indices.getBuffer(6), indices.type());
				pass.drawIndexed(6, 1, 0, 0, 0);
			}
			try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "SvregBlue grade", main.getColorTextureView(), Optional.empty(), null, OptionalDouble.empty())) {
				pass.setPipeline(gradePipeline);
				RenderSystem.bindDefaultUniforms(pass);
				pass.setUniform("DynamicTransforms", transforms);
				pass.bindTexture("Sampler0", sceneView, sampler);
				pass.setVertexBuffer(0, quad.slice());
				pass.setIndexBuffer(indices.getBuffer(6), indices.type());
				pass.drawIndexed(6, 1, 0, 0, 0);
			}
		} catch (Throwable t) {
			broken = true;
			SvregBlue.LOG.error("SvregBlue: blue grade failed, turning it off", t);
		}
	}

	/** distance fog toward the blue-hour haze colour (ARGB in, ARGB out) */
	public static int tintFog(int argb) {
		if (!BlueConfig.fog) {
			return argb;
		}
		float k = BlueTime.level() * 0.85F;
		if (k <= 0.001F) {
			return argb;
		}
		int a = argb >>> 24, r = (argb >> 16) & 255, g = (argb >> 8) & 255, b = argb & 255;
		// keep some of the original brightness so day-ish fog in "always" mode isn't a dark wall
		float lum = (0.30F * r + 0.59F * g + 0.11F * b) / 255F;
		float tr = 0.07F + 0.16F * lum, tg = 0.16F + 0.30F * lum, tb = 0.38F + 0.45F * lum;
		r = Math.round(r + (tr * 255 - r) * k);
		g = Math.round(g + (tg * 255 - g) * k);
		b = Math.round(b + (tb * 255 - b) * k);
		return (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
	}

	private static int clamp(int v) {
		return Math.max(0, Math.min(255, v));
	}
}
