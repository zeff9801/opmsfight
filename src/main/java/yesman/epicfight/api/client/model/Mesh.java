package yesman.epicfight.api.client.model;

import java.util.Collection;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public abstract class Mesh<T extends VertexIndicator> {
	public static class RenderProperties {
		protected String customTexturePath;
		protected boolean isTransparent;
		protected Object2BooleanMap<String> parentPartVisualizer;

		public String getCustomTexturePath() {
			return this.customTexturePath;
		}

		public boolean isTransparent() {
			return this.isTransparent;
		}

		public Object2BooleanMap<String> getParentPartVisualizer() {
			return this.parentPartVisualizer;
		}

		public RenderProperties customTexturePath(String path) {
			this.customTexturePath = path;
			return this;
		}

		public RenderProperties transparency(boolean isTransparent) {
			this.isTransparent = isTransparent;
			return this;
		}

		public RenderProperties newPartVisualizer(String partName, boolean setVisible) {
			if (this.parentPartVisualizer == null) {
				this.parentPartVisualizer = new Object2BooleanOpenHashMap<>();
			}

			this.parentPartVisualizer.put(partName, setVisible);

			return this;
		}

		public static RenderProperties create() {
			return new RenderProperties();
		}
	}

	final float[] positions;
	final float[] uvs;
	final float[] normals;
	final int totalVertices;
	final Map<String, ModelPart<T>> parts;
	final RenderProperties renderProperties;

	public Mesh(Map<String, float[]> arrayMap, Mesh<T> parent, RenderProperties renderProperties, Map<String, ModelPart<T>> parts) {
		this.positions = (parent == null) ? arrayMap.get("positions") : parent.positions;
		this.normals = (parent == null) ? arrayMap.get("normals") : parent.normals;
		this.uvs = (parent == null) ? arrayMap.get("uvs") : parent.uvs;
		this.parts = (parent == null) ? parts : parent.parts;
		this.renderProperties = renderProperties;

		int totalV = 0;

		for (ModelPart<T> meshpart : parts.values()) {
			totalV += meshpart.getVertices().size();
		}

		this.totalVertices = totalV;
	}

	protected abstract ModelPart<T> getOrLogException(Map<String, ModelPart<T>> parts, String name);

	public boolean hasPart(String part) {
		return this.parts.containsKey(part);
	}

	public ModelPart<T> getPart(String part) {
		return this.parts.get(part);
	}

	public Collection<ModelPart<T>> getAllParts() {
		return this.parts.values();
	}

	public RenderProperties getRenderProperty() {
		return this.renderProperties;
	}

	public void initialize() {
		this.parts.values().forEach((part) -> part.hidden = false);
	}

	public void drawRawModel(MatrixStack poseStack, IVertexBuilder builder, int packedLightIn, float r, float g, float b, float a, int overlayCoord) {
		this.draw(poseStack, builder, DrawingFunction.ENTITY_TRANSLUCENT, packedLightIn, r, g, b, a, overlayCoord);
	}

	public void drawRawModelNoLighting(MatrixStack poseStack, IVertexBuilder builder, int packedLightIn, float r, float g, float b, float a, int overlayCoord) {
		this.draw(poseStack, builder, DrawingFunction.ENTITY_NO_LIGHTING, packedLightIn, r, g, b, a, overlayCoord);
	}

	public void draw(MatrixStack poseStack, IVertexBuilder builder, DrawingFunction drawingFunction, int packedLightIn, float r, float g, float b, float a, int overlayCoord) {
		Matrix4f matrix4f = poseStack.last().pose();
		Matrix3f matrix3f = poseStack.last().normal();

		//TODO This code used Vector4f and Vector3f comign from Joml.
		//Ported the methods directly in the classes. If stuff doesn't work fine, could also be a cause

		for (ModelPart<T> part : this.parts.values()) {
			if (!part.hidden) {
				for (VertexIndicator vi : part.getVertices()) {
					int pos = vi.position * 3;
					int norm = vi.normal * 3;
					int uv = vi.uv * 2;
					Vector4f posVec = new Vector4f(this.positions[pos], this.positions[pos + 1], this.positions[pos + 2], 1.0F);
					Vector3f normVec = new Vector3f(this.normals[norm], this.normals[norm + 1], this.normals[norm + 2]);
					posVec.mul(matrix4f);
					normVec.mul(matrix3f);
					drawingFunction.draw(builder, posVec, normVec, packedLightIn, r, g, b, a, this.uvs[uv], this.uvs[uv + 1], overlayCoord);
				}
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class RawMesh extends Mesh<VertexIndicator> {
		public static final ModelPart<VertexIndicator> EMPTY = new ModelPart<>(null, null);

		public RawMesh(Map<String, float[]> arrayMap, Mesh<VertexIndicator> parent, RenderProperties properties, Map<String, ModelPart<VertexIndicator>> parts) {
			super(arrayMap, parent, properties, parts);
		}

		protected ModelPart<VertexIndicator> getOrLogException(Map<String, ModelPart<VertexIndicator>> parts, String name) {
			if (!parts.containsKey(name)) {
				EpicFightMod.LOGGER.debug("Can not find the mesh part named " + name + " in " + this.getClass().getCanonicalName());
				return EMPTY;
			}

			return parts.get(name);
		}
	}

	@FunctionalInterface
	public interface DrawingFunction {
		public static final DrawingFunction ENTITY_TRANSLUCENT = (builder, posVec, normalVec, packedLightIn, r, g, b, a, u, v, overlay) -> {
			builder.vertex(posVec.x(), posVec.y(), posVec.z(), r, g, b, a, u, v, overlay, packedLightIn, normalVec.x(), normalVec.y(), normalVec.z());
		};

		public static final DrawingFunction ENTITY_PARTICLE = (builder, posVec, normalVec, packedLightIn, r, g, b, a, u, v, overlay) -> {
			builder.vertex(posVec.x(), posVec.y(), posVec.z());
			builder.color(r, g, b, a);
			builder.uv2(packedLightIn);
			builder.endVertex();
		};

		public static final DrawingFunction ENTITY_SOLID = (builder, posVec, normalVec, packedLightIn, r, g, b, a, u, v, overlay) -> {
			builder.vertex(posVec.x(), posVec.y(), posVec.z());
			builder.color(r, g, b, a);
			builder.normal(normalVec.x(), normalVec.y(), normalVec.z());
			builder.endVertex();
		};

		public static final DrawingFunction ENTITY_NO_LIGHTING = (builder, posVec, normalVec, packedLightIn, r, g, b, a, u, v, overlay) -> {
			builder.vertex(posVec.x(), posVec.y(), posVec.z());
			builder.color(r, g, b, a);
			builder.uv(u, v);
			builder.uv2(packedLightIn);
			builder.endVertex();
		};

		public void draw(IVertexBuilder builder, Vector4f posVec, Vector3f normalVec, int packedLightIn, float r, float g, float b, float a, float u, float v, int overlay);
	}
}