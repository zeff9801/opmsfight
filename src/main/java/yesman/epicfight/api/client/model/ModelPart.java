package yesman.epicfight.api.client.model;

import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ModelPart<T extends VertexIndicator> {
	private final ModelRenderer vanillaModelPart;
	private final List<T> vertices;
	public boolean hidden;

	public ModelPart(List<T> vertices) {
		this(vertices, null);
	}

	public ModelPart(List<T> vertices, ModelRenderer vanillaModelPart) {
		this.vertices = vertices;
		this.vanillaModelPart = vanillaModelPart;
	}

	public void setVanillaTransform() {
		if (this.vanillaModelPart != null) {
			// Add any transformation logic for vanillaModelPart here
		}
	}

	public List<T> getVertices() {
		return this.vertices;
	}
}
