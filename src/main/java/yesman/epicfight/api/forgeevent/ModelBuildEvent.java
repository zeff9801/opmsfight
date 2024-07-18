
package yesman.epicfight.api.forgeevent;

import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.lifecycle.IModBusEvent;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.Mesh.RawMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.Meshes.MeshContructor;
import yesman.epicfight.api.client.model.VertexIndicator;
import yesman.epicfight.api.client.model.VertexIndicator.AnimatedVertexIndicator;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.Armatures.ArmatureContructor;

import java.util.Map;

public abstract class ModelBuildEvent<T> extends Event implements IModBusEvent {
	protected final IResourceManager resourceManager;

	public ModelBuildEvent(IResourceManager resourceManager, Map<ResourceLocation, T> registerMap) {
		this.resourceManager = resourceManager;
	}

	public static class ArmatureBuild extends ModelBuildEvent<Armature> {
		public ArmatureBuild(IResourceManager resourceManager, Map<ResourceLocation, Armature> registerMap) {
			super(resourceManager, registerMap);
		}

		public <T extends Armature> T get(String modid, String path, ArmatureContructor<T> constructor) {
			return Armatures.getOrCreateArmature(this.resourceManager, new ResourceLocation(modid, path), constructor);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public static class MeshBuild extends ModelBuildEvent<Mesh<?>> {
		public MeshBuild(IResourceManager resourceManager, Map<ResourceLocation, Mesh<?>> registerMap) {
			super(resourceManager, registerMap);
		}

		public <M extends RawMesh> M getRaw(String modid, String path, MeshContructor<VertexIndicator, M> constructor) {
			return Meshes.getOrCreateRawMesh(this.resourceManager, new ResourceLocation(modid, path), constructor);
		}

		public <M extends AnimatedMesh> M getAnimated(String modid, String path, MeshContructor<AnimatedVertexIndicator, M> constructor) {
			return Meshes.getOrCreateAnimatedMesh(this.resourceManager, new ResourceLocation(modid, path), constructor);
		}
	}
}
