package yesman.epicfight.api.client.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public abstract class ModelPart<T extends VertexBuilder> {
	protected final List<T> verticies;
	protected final Supplier<OpenMatrix4f> vanillaPartTracer;
	protected boolean isHidden;
	
	public ModelPart(List<T> vertices, @Nullable Supplier<OpenMatrix4f> vanillaPartTracer) {
		this.verticies = vertices;
		this.vanillaPartTracer = vanillaPartTracer;
	}
	
	public abstract void draw(MatrixStack poseStack, IVertexBuilder builder, Mesh.DrawingFunction drawingFunction, int packedLight, float r, float g, float b, float a, int overlay);
	
	public void setHidden(boolean hidden) {
		this.isHidden = hidden;
	}
	
	public boolean isHidden() {
		return this.isHidden;
	}
	
	public List<T> getVertices() {
		return this.verticies;
	}
}