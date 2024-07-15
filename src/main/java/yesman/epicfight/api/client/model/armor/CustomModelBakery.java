package yesman.epicfight.api.client.model.armor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.ClientModel;
import yesman.epicfight.api.client.model.ClientModels;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.main.EpicFightMod;

@OnlyIn(Dist.CLIENT)
public class CustomModelBakery {
	static int indexCount = 0;
	
	static final Map<ResourceLocation, ClientModel> BAKED_MODELS = Maps.newHashMap();
	static final ModelBaker HEAD = new SimpleBaker(9);
	static final ModelBaker LEFT_FEET = new SimpleBaker(5);
	static final ModelBaker RIGHT_FEET = new SimpleBaker(2);
	static final ModelBaker LEFT_ARM = new Limb(16, 17, 19, 19.0F, false);
	static final ModelBaker LEFT_ARM_CHILD = new SimpleSeparateBaker(16, 17, 19.0F);
	static final ModelBaker RIGHT_ARM = new Limb(11, 12, 14, 19.0F, false);
	static final ModelBaker RIGHT_ARM_CHILD = new SimpleSeparateBaker(11, 12, 19.0F);
	static final ModelBaker LEFT_LEG = new Limb(4, 5, 6, 6.0F, true);
	static final ModelBaker LEFT_LEG_CHILD = new SimpleSeparateBaker(4, 5, 6.0F);
	static final ModelBaker RIGHT_LEG = new Limb(1, 2, 3, 6.0F, true);
	static final ModelBaker RIGHT_LEG_CHILD = new SimpleSeparateBaker(1, 2, 6.0F);
	static final ModelBaker CHEST = new Chest();
	static final ModelBaker CHEST_CHILD = new SimpleSeparateBaker(8, 7, 18.0F);
	
	public static void exportModels(File resourcePackDirectory) throws IOException {
		File zipFile = new File(resourcePackDirectory, "epicfight_custom_armors.zip");
		
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
		for (Map.Entry<ResourceLocation, ClientModel> entry : BAKED_MODELS.entrySet()) {
			ZipEntry zipEntry = new ZipEntry(String.format("assets/%s/%s", entry.getValue().getLocation().getNamespace(), entry.getValue().getLocation().getPath()));
			Gson gson = new GsonBuilder().create();
			out.putNextEntry(zipEntry);
			out.write(gson.toJson(entry.getValue().getMesh().toJsonObject()).getBytes());
			out.closeEntry();
            EpicFightMod.LOGGER.info("Exported custom armor model : {}", entry.getKey());
		}
		
		ZipEntry zipEntry = new ZipEntry("pack.mcmeta");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonObject root = new JsonObject();
		JsonObject pack = new JsonObject();
		pack.addProperty("description", "epicfight_custom_armor_models");
		pack.addProperty("pack_format", Minecraft.getInstance().getGame().getVersion().getPackVersion());
		root.add("pack", pack);
		out.putNextEntry(zipEntry);
		out.write(gson.toJson(root).getBytes());
		out.closeEntry();
		out.close();
	}
	
	static void resetRotation(ModelRenderer modelRenderer) {
		modelRenderer.xRot = 0.0F;
		modelRenderer.yRot = 0.0F;
		modelRenderer.zRot = 0.0F;
	}
	
	public static ClientModel bakeBipedCustomArmorModel(BipedModel<?> model, ArmorItem armorItem, EquipmentSlotType slot, boolean debuggingMode) {
		List<ModelPartition> boxes = Lists.<ModelPartition>newArrayList();
		
		resetRotation(model.head);
		resetRotation(model.hat);
		resetRotation(model.body);
		resetRotation(model.rightArm);
		resetRotation(model.leftArm);
		resetRotation(model.rightLeg);
		resetRotation(model.leftLeg);
		
		switch (slot) {
		case HEAD:
			boxes.add(new ModelPartition(HEAD, HEAD, model.head));
			boxes.add(new ModelPartition(HEAD, HEAD, model.hat));
			break;
		case CHEST:
			boxes.add(new ModelPartition(CHEST, CHEST_CHILD, model.body));
			boxes.add(new ModelPartition(RIGHT_ARM, RIGHT_ARM_CHILD, model.rightArm));
			boxes.add(new ModelPartition(LEFT_ARM, LEFT_ARM_CHILD, model.leftArm));
			break;
		case LEGS:
			boxes.add(new ModelPartition(CHEST, CHEST_CHILD, model.body));
			boxes.add(new ModelPartition(LEFT_LEG, LEFT_LEG_CHILD, model.leftLeg));
			boxes.add(new ModelPartition(RIGHT_LEG, RIGHT_LEG_CHILD, model.rightLeg));
			break;
		case FEET:
			boxes.add(new ModelPartition(LEFT_FEET, LEFT_FEET, model.leftLeg));
			boxes.add(new ModelPartition(RIGHT_FEET, RIGHT_FEET, model.rightLeg));
			break;
		default:
			return null;
		}
		
		ResourceLocation rl = new ResourceLocation(armorItem.getRegistryName().getNamespace(), "armor/" + armorItem.getRegistryName().getPath());
		ClientModel customModel = new ClientModel(rl, bakeMeshFromCubes(boxes, debuggingMode));
		ClientModels.LOGICAL_CLIENT.register(rl, customModel);
		BAKED_MODELS.put(armorItem.getRegistryName(), customModel);
		return customModel;
	}
	
	private static Mesh bakeMeshFromCubes(List<ModelPartition> partitions, boolean debuggingMode) {
		List<CustomArmorVertex> vertices = Lists.newArrayList();
		List<Integer> indices = Lists.newArrayList();
		MatrixStack poseStack = new MatrixStack();
		indexCount = 0;
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
		poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
		poseStack.translate(0, -24, 0);
		
		for (ModelPartition modelpartition : partitions) {
			bake(poseStack, modelpartition, modelpartition.part, modelpartition.partBaker, vertices, indices, debuggingMode);
		}
		
		return CustomArmorVertex.loadVertexInformation(vertices, ArrayUtils.toPrimitive(indices.toArray(new Integer[0])));
	}
	
	private static void bake(MatrixStack poseStack, ModelPartition modelpartition, ModelRenderer part, ModelBaker partBaker, List<CustomArmorVertex> vertices, List<Integer> indices, boolean debuggingMode) {
		poseStack.pushPose();
		poseStack.translate(part.x, part.y, part.z);
		
		if (part.zRot != 0.0F) {
			poseStack.mulPose(Vector3f.ZP.rotation(part.zRot));
		}
		
		if (part.yRot != 0.0F) {
			poseStack.mulPose(Vector3f.YP.rotation(part.yRot));
		}
		
		if (part.xRot != 0.0F) {
			poseStack.mulPose(Vector3f.XP.rotation(part.xRot));
		}
		
		for (ModelRenderer.ModelBox cube : part.cubes) {
			partBaker.bakeCube(poseStack, cube, vertices, indices);
		}
		
		for (ModelRenderer childParts : part.children) {
			bake(poseStack, modelpartition, childParts, modelpartition.childBaker, vertices, indices, debuggingMode);
		}
		
		poseStack.popPose();
	}
	
	@OnlyIn(Dist.CLIENT)
	static class ModelPartition {
		final ModelBaker partBaker;
		final ModelBaker childBaker;
		final ModelRenderer part;
		
		private ModelPartition(ModelBaker partedBaker, ModelBaker childBaker, ModelRenderer modelRenderer) {
			this.partBaker = partedBaker;
			this.childBaker = childBaker;
			this.part = modelRenderer;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	abstract static class ModelBaker {
		public abstract void bakeCube(MatrixStack poseStack, ModelRenderer.ModelBox cube, List<CustomArmorVertex> vertices, List<Integer> indices);
	}
	
	static void putIndexCount(List<Integer> indices, int value) {
		for (int i = 0; i < 3; i++) {
			indices.add(value);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	static class SimpleBaker extends ModelBaker {
		final int jointId;
		
		public SimpleBaker (int jointId) {
			this.jointId = jointId;
		}
		
		public void bakeCube(MatrixStack poseStack, ModelRenderer.ModelBox cube, List<CustomArmorVertex> vertices, List<Integer> indices) {
			for (ModelRenderer.TexturedQuad quad : cube.polygons) {
				Vector3f norm = quad.normal.copy();
				norm.transform(poseStack.last().normal());
				
				for (ModelRenderer.PositionTextureVertex vertex : quad.vertices) {
					Vector4f pos = new Vector4f(vertex.pos);
					pos.transform(poseStack.last().pose());
					vertices.add(new CustomArmorVertex()
						.setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
						.setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
						.setTextureCoordinate(new Vec2f(vertex.u, vertex.v))
						.setEffectiveJointIDs(new Vec3f(this.jointId, 0, 0))
						.setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
						.setEffectiveJointNumber(1)
					);
				}
				
				putIndexCount(indices, indexCount);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 2);
				indexCount+=4;
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	static class SimpleSeparateBaker extends ModelBaker {
		final SimpleBaker upperBaker;
		final SimpleBaker lowerBaker;
		final float yClipCoord;
		
		public SimpleSeparateBaker(int upperJoint, int lowerJoint, float yClipCoord) {
			this.upperBaker = new SimpleBaker(upperJoint);
			this.lowerBaker = new SimpleBaker(lowerJoint);
			this.yClipCoord = yClipCoord;
		}
		
		@Override
		public void bakeCube(MatrixStack poseStack, ModelRenderer.ModelBox cube, List<CustomArmorVertex> vertices, List<Integer> indices) {
			Vector4f cubeCenter = new Vector4f(cube.minX + (cube.maxX - cube.minX) * 0.5F, cube.minY + (cube.maxY - cube.minY) * 0.5F, cube.minZ + (cube.maxZ - cube.minZ) * 0.5F, 1.0F);
			cubeCenter.transform(poseStack.last().pose());
			
			if (cubeCenter.y() > this.yClipCoord) {
				this.upperBaker.bakeCube(poseStack, cube, vertices, indices);
			} else {
				this.lowerBaker.bakeCube(poseStack, cube, vertices, indices);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	static class Chest extends ModelBaker {
		static final float X_PLANE = 0.0F;
		static final VertexWeight[] WEIGHT_ALONG_Y = { new VertexWeight(13.6666F, 0.230F, 0.770F), new VertexWeight(15.8333F, 0.254F, 0.746F), new VertexWeight(18.0F, 0.5F, 0.5F), new VertexWeight(20.1666F, 0.744F, 0.256F), new VertexWeight(22.3333F, 0.770F, 0.230F)};
		
		@Override
		public void bakeCube(MatrixStack poseStack, ModelRenderer.ModelBox cube, List<CustomArmorVertex> vertices, List<Integer> indices) {
			List<AnimatedPolygon> xClipPolygons = Lists.<AnimatedPolygon>newArrayList();
			List<AnimatedPolygon> xyClipPolygons = Lists.<AnimatedPolygon>newArrayList();
			
			for (ModelRenderer.TexturedQuad polygon : cube.polygons) {
				Matrix4f matrix = poseStack.last().pose();
				
				ModelRenderer.PositionTextureVertex pos0 = getTranslatedVertex(polygon.vertices[0], matrix);
				ModelRenderer.PositionTextureVertex pos1 = getTranslatedVertex(polygon.vertices[1], matrix);
				ModelRenderer.PositionTextureVertex pos2 = getTranslatedVertex(polygon.vertices[2], matrix);
				ModelRenderer.PositionTextureVertex pos3 = getTranslatedVertex(polygon.vertices[3], matrix);
				Direction direction = getDirectionFromVector(polygon.normal);
				
				VertexWeight pos0Weight = getYClipWeight(pos0.pos.y());
				VertexWeight pos1Weight = getYClipWeight(pos1.pos.y());
				VertexWeight pos2Weight = getYClipWeight(pos2.pos.y());
				VertexWeight pos3Weight = getYClipWeight(pos3.pos.y());
				
				if (pos1.pos.x() > X_PLANE != pos2.pos.x() > X_PLANE) {
					float distance = pos2.pos.x() - pos1.pos.x();
					float textureU = pos1.u + (pos2.u - pos1.u) * ((X_PLANE - pos1.pos.x()) / distance);
					ModelRenderer.PositionTextureVertex pos4 = new ModelRenderer.PositionTextureVertex(X_PLANE, pos0.pos.y(), pos0.pos.z(), textureU, pos0.v);
					ModelRenderer.PositionTextureVertex pos5 = new ModelRenderer.PositionTextureVertex(X_PLANE, pos1.pos.y(), pos1.pos.z(), textureU, pos1.v);
					
					xClipPolygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0),
						new AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0),
						new AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0),
						new AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0)
					}, direction));
					xClipPolygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos4, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0),
						new AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0),
						new AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0),
						new AnimatedVertex(pos5, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0)
					}, direction));
				} else {
					xClipPolygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos0, 8, 7, 0, pos0Weight.chestWeight, pos0Weight.torsoWeight, 0),
						new AnimatedVertex(pos1, 8, 7, 0, pos1Weight.chestWeight, pos1Weight.torsoWeight, 0),
						new AnimatedVertex(pos2, 8, 7, 0, pos2Weight.chestWeight, pos2Weight.torsoWeight, 0),
						new AnimatedVertex(pos3, 8, 7, 0, pos3Weight.chestWeight, pos3Weight.torsoWeight, 0)
					}, direction));
				}
			}
			
			for (AnimatedPolygon polygon : xClipPolygons) {
				boolean upsideDown = polygon.animatedVertexPositions[1].pos.y() > polygon.animatedVertexPositions[2].pos.y();
				AnimatedVertex pos0 = upsideDown ? polygon.animatedVertexPositions[2] : polygon.animatedVertexPositions[0];
				AnimatedVertex pos1 = upsideDown ? polygon.animatedVertexPositions[3] : polygon.animatedVertexPositions[1];
				AnimatedVertex pos2 = upsideDown ? polygon.animatedVertexPositions[0] : polygon.animatedVertexPositions[2];
				AnimatedVertex pos3 = upsideDown ? polygon.animatedVertexPositions[1] : polygon.animatedVertexPositions[3];
				Direction direction = getDirectionFromVector(polygon.normal);
				List<VertexWeight> vertexWeights = getMiddleYClipWeights(pos1.pos.y(), pos2.pos.y());
				List<AnimatedVertex> animatedVertices = Lists.<AnimatedVertex>newArrayList();
				animatedVertices.add(pos0);
				animatedVertices.add(pos1);
				
				if (!vertexWeights.isEmpty()) {
					for (VertexWeight vertexWeight : vertexWeights) {
						float distance = pos2.pos.y() - pos1.pos.y();
						float textureV = pos1.v + (pos2.v - pos1.v) * ((vertexWeight.yClipCoord - pos1.pos.y()) / distance);
						ModelRenderer.PositionTextureVertex pos4 = new ModelRenderer.PositionTextureVertex(pos0.pos.x(), vertexWeight.yClipCoord, pos0.pos.z(), pos0.u, textureV);
						ModelRenderer.PositionTextureVertex pos5 = new ModelRenderer.PositionTextureVertex(pos1.pos.x(), vertexWeight.yClipCoord, pos1.pos.z(), pos1.u, textureV);
						animatedVertices.add(new AnimatedVertex(pos4, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0));
						animatedVertices.add(new AnimatedVertex(pos5, 8, 7, 0, vertexWeight.chestWeight, vertexWeight.torsoWeight, 0));
					}
				}
				
				animatedVertices.add(pos3);
				animatedVertices.add(pos2);
				
				for (int i = 0; i < (animatedVertices.size() - 2) / 2; i++) {
					int start = i*2;
					AnimatedVertex p0 = animatedVertices.get(start);
					AnimatedVertex p1 = animatedVertices.get(start + 1);
					AnimatedVertex p2 = animatedVertices.get(start + 3);
					AnimatedVertex p3 = animatedVertices.get(start + 2);
					xyClipPolygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(p0, 8, 7, 0, p0.weight.x, p0.weight.y, 0),
						new AnimatedVertex(p1, 8, 7, 0, p1.weight.x, p1.weight.y, 0),
						new AnimatedVertex(p2, 8, 7, 0, p2.weight.x, p2.weight.y, 0),
						new AnimatedVertex(p3, 8, 7, 0, p3.weight.x, p3.weight.y, 0)
					}, direction));
				}
			}
			
			for (AnimatedPolygon polygon : xyClipPolygons) {
				Vector3f norm = polygon.normal.copy();
				norm.transform(poseStack.last().normal());
				
				for (AnimatedVertex vertex : polygon.animatedVertexPositions) {
					Vector4f pos = new Vector4f(vertex.pos);
					float weight1 = vertex.weight.x;
					float weight2 = vertex.weight.y;
					int joint1 = vertex.jointId.getX();
					int joint2 = vertex.jointId.getY();
					int count = weight1 > 0.0F && weight2 > 0.0F ? 2 : 1;
					
					if (weight1 <= 0.0F) {
						joint1 = joint2;
						weight1 = weight2;
					}
					
					vertices.add(new CustomArmorVertex()
						.setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
						.setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
						.setTextureCoordinate(new Vec2f(vertex.u, vertex.v))
						.setEffectiveJointIDs(new Vec3f(joint1, joint2, 0))
						.setEffectiveJointWeights(new Vec3f(weight1, weight2, 0.0F))
						.setEffectiveJointNumber(count)
					);
				}
				
				putIndexCount(indices, indexCount);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 2);
				indexCount+=4;
			}
		}
		
		static VertexWeight getYClipWeight(float y) {
			if (y < WEIGHT_ALONG_Y[0].yClipCoord) {
				return new VertexWeight(y, 0.0F, 1.0F);
			}
			
			int index = -1;
			for (int i = 0; i < WEIGHT_ALONG_Y.length; i++) {
				
			}
			
			if (index > 0) {
				VertexWeight pair = WEIGHT_ALONG_Y[index];
				return new VertexWeight(y, pair.chestWeight, pair.torsoWeight);
			}
			
			return new VertexWeight(y, 1.0F, 0.0F);
		}
		
		static List<VertexWeight> getMiddleYClipWeights(float minY, float maxY) {
			List<VertexWeight> cutYs = Lists.<VertexWeight>newArrayList();
			for (VertexWeight vertexWeight : WEIGHT_ALONG_Y) {
				if (vertexWeight.yClipCoord > minY && maxY >= vertexWeight.yClipCoord) {
					cutYs.add(vertexWeight);
				}
			}
			return cutYs;
		}
		
		static class VertexWeight {
			final float yClipCoord;
			final float chestWeight;
			final float torsoWeight;
			
			public VertexWeight(float yClipCoord, float chestWeight, float torsoWeight) {
				this.yClipCoord = yClipCoord;
				this.chestWeight = chestWeight;
				this.torsoWeight = torsoWeight;
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	static class Limb extends ModelBaker {
		final int upperJoint;
		final int lowerJoint;
		final int middleJoint;
		final float yClipCoord;
		final boolean bendInFront;
		
		public Limb(int upperJoint, int lowerJoint, int middleJoint, float yClipCoord, boolean bendInFront) {
			this.upperJoint = upperJoint;
			this.lowerJoint = lowerJoint;
			this.middleJoint = middleJoint;
			this.yClipCoord = yClipCoord;
			this.bendInFront = bendInFront;
		}
		
		@Override
		public void bakeCube(MatrixStack poseStack, ModelRenderer.ModelBox cube, List<CustomArmorVertex> vertices, List<Integer> indices) {
			List<AnimatedPolygon> polygons = Lists.<AnimatedPolygon>newArrayList();
			
			for (ModelRenderer.TexturedQuad quad : cube.polygons) {
				Matrix4f matrix = poseStack.last().pose();
				ModelRenderer.PositionTextureVertex pos0 = getTranslatedVertex(quad.vertices[0], matrix);
				ModelRenderer.PositionTextureVertex pos1 = getTranslatedVertex(quad.vertices[1], matrix);
				ModelRenderer.PositionTextureVertex pos2 = getTranslatedVertex(quad.vertices[2], matrix);
				ModelRenderer.PositionTextureVertex pos3 = getTranslatedVertex(quad.vertices[3], matrix);
				Direction direction = getDirectionFromVector(quad.normal);
				
				if (pos1.pos.y() > this.yClipCoord != pos2.pos.y() > this.yClipCoord) {
					float distance = pos2.pos.y() - pos1.pos.y();
					float textureV = pos1.v + (pos2.v - pos1.v) * ((this.yClipCoord - pos1.pos.y()) / distance);
					ModelRenderer.PositionTextureVertex pos4 = new ModelRenderer.PositionTextureVertex(pos0.pos.x(), this.yClipCoord, pos0.pos.z(), pos0.u, textureV);
					ModelRenderer.PositionTextureVertex pos5 = new ModelRenderer.PositionTextureVertex(pos1.pos.x(), this.yClipCoord, pos1.pos.z(), pos1.u, textureV);
					
					int upperId, lowerId;
					if (distance > 0) {
						upperId = this.lowerJoint;
						lowerId = this.upperJoint;
					} else {
						upperId = this.upperJoint;
						lowerId = this.lowerJoint;
					}
					
					polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos0, upperId), new AnimatedVertex(pos1, upperId),
						new AnimatedVertex(pos5, upperId), new AnimatedVertex(pos4, upperId)
					}, direction));
					polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos4, lowerId), new AnimatedVertex(pos5, lowerId),
						new AnimatedVertex(pos2, lowerId), new AnimatedVertex(pos3, lowerId)
					}, direction));
					
					boolean hasSameZ = pos4.pos.z() < 0.0F == pos5.pos.z() < 0.0F;
					boolean isFront = hasSameZ && (pos4.pos.z() < 0.0F == this.bendInFront);
					
					if (isFront) {
						polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
							new AnimatedVertex(pos4, this.middleJoint), new AnimatedVertex(pos5, this.middleJoint),
							new AnimatedVertex(pos5, this.upperJoint), new AnimatedVertex(pos4, this.upperJoint)
						}, 0.001F, direction));
						polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
							new AnimatedVertex(pos4, this.lowerJoint), new AnimatedVertex(pos5, this.lowerJoint),
							new AnimatedVertex(pos5, this.middleJoint), new AnimatedVertex(pos4, this.middleJoint)
						}, 0.001F, direction));
					} else if (!hasSameZ) {
						boolean startFront = pos4.pos.z() > 0;
						int firstJoint = this.lowerJoint;
						int secondJoint = this.lowerJoint;
						int thirdJoint = startFront ? this.upperJoint : this.middleJoint;
						int fourthJoint = startFront ? this.middleJoint : this.upperJoint;
						int fifthJoint = this.upperJoint;
						int sixthJoint = this.upperJoint;
						
						polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
							new AnimatedVertex(pos4, firstJoint), new AnimatedVertex(pos5, secondJoint),
							new AnimatedVertex(pos5, thirdJoint), new AnimatedVertex(pos4, fourthJoint)
						}, 0.001F, direction));
						polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
							new AnimatedVertex(pos4, fourthJoint), new AnimatedVertex(pos5, thirdJoint),
							new AnimatedVertex(pos5, fifthJoint), new AnimatedVertex(pos4, sixthJoint)
						}, 0.001F, direction));
					}
				} else {
					int jointId = pos0.pos.y() > this.yClipCoord ? this.upperJoint : this.lowerJoint;
					polygons.add(new AnimatedPolygon(new AnimatedVertex[] {
						new AnimatedVertex(pos0, jointId), new AnimatedVertex(pos1, jointId),
						new AnimatedVertex(pos2, jointId), new AnimatedVertex(pos3, jointId)
					}, direction));
				}
			}
			
			for (AnimatedPolygon quad : polygons) {
				Vector3f norm = quad.normal.copy();
				norm.transform(poseStack.last().normal());
				
				for (AnimatedVertex vertex : quad.animatedVertexPositions) {
					Vector4f pos = new Vector4f(vertex.pos);
					vertices.add(new CustomArmorVertex()
						.setPosition(new Vec3f(pos.x(), pos.y(), pos.z()).scale(0.0625F))
						.setNormal(new Vec3f(norm.x(), norm.y(), norm.z()))
						.setTextureCoordinate(new Vec2f(vertex.u, vertex.v))
						.setEffectiveJointIDs(new Vec3f(vertex.jointId.getX(), 0, 0))
						.setEffectiveJointWeights(new Vec3f(1.0F, 0.0F, 0.0F))
						.setEffectiveJointNumber(1)
					);
				}
				
				putIndexCount(indices, indexCount);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 3);
				putIndexCount(indices, indexCount + 1);
				putIndexCount(indices, indexCount + 2);
				indexCount+=4;
			}
		}
	}
	
	static Direction getDirectionFromVector(Vector3f directionVec) {
		for (Direction direction : Direction.values()) {
			Vector3f direcVec = new Vector3f(Float.compare(directionVec.x(), -0.0F) == 0 ? 0.0F : directionVec.x(), directionVec.y(), directionVec.z());
			if (direcVec.equals(direction.step())) {
				return direction;
			}
		}
		
		return null;
	}
	
	static ModelRenderer.PositionTextureVertex getTranslatedVertex(ModelRenderer.PositionTextureVertex original, Matrix4f matrix) {
		Vector4f translatedPosition = new Vector4f(original.pos);
		translatedPosition.transform(matrix);
		
		return new ModelRenderer.PositionTextureVertex(translatedPosition.x(), translatedPosition.y(), translatedPosition.z(), original.u, original.v);
	}
	
	@OnlyIn(Dist.CLIENT)
	static class AnimatedVertex extends ModelRenderer.PositionTextureVertex {
		final Vector3i jointId;
		final Vec3f weight;
		
		public AnimatedVertex(ModelRenderer.PositionTextureVertex posTexVertx, int jointId) {
			this(posTexVertx, jointId, 0, 0, 1.0F, 0.0F, 0.0F);
		}
		
		public AnimatedVertex(ModelRenderer.PositionTextureVertex posTexVertx, int jointId1, int jointId2, int jointId3, float weight1, float weight2, float weight3) {
			this(posTexVertx, new Vector3i(jointId1, jointId2, jointId3), new Vec3f(weight1, weight2, weight3));
		}
		
		public AnimatedVertex(ModelRenderer.PositionTextureVertex posTexVertx, Vector3i ids, Vec3f weights) {
			this(posTexVertx, posTexVertx.u, posTexVertx.v, ids, weights);
		}
		
		public AnimatedVertex(ModelRenderer.PositionTextureVertex posTexVertx, float u, float v, Vector3i ids, Vec3f weights) {
			super(posTexVertx.pos.x(), posTexVertx.pos.y(), posTexVertx.pos.z(), u, v);
			this.jointId = ids;
			this.weight = weights;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	static class AnimatedPolygon {
		public final AnimatedVertex[] animatedVertexPositions;
		public final Vector3f normal;
		
		public AnimatedPolygon(AnimatedVertex[] positionsIn, Direction directionIn) {
			this.animatedVertexPositions = positionsIn;
			this.normal = directionIn.step();
		}
		
		public AnimatedPolygon(AnimatedVertex[] positionsIn, float cor, Direction directionIn) {
			this.animatedVertexPositions = positionsIn;
			positionsIn[0] = new AnimatedVertex(positionsIn[0], positionsIn[0].u, positionsIn[0].v + cor, positionsIn[0].jointId, positionsIn[0].weight);
			positionsIn[1] = new AnimatedVertex(positionsIn[1], positionsIn[1].u, positionsIn[1].v + cor, positionsIn[1].jointId, positionsIn[1].weight);
			positionsIn[2] = new AnimatedVertex(positionsIn[2], positionsIn[2].u, positionsIn[2].v - cor, positionsIn[2].jointId, positionsIn[2].weight);
			positionsIn[3] = new AnimatedVertex(positionsIn[3], positionsIn[3].u, positionsIn[3].v - cor, positionsIn[3].jointId, positionsIn[3].weight);
			this.normal = directionIn.step();
		}
	}
}