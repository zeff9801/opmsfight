package yesman.epicfight.api.animation.types.procedural;

import java.util.Map;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.animation.JointTransform;
import yesman.epicfight.api.animation.Keyframe;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationProperty.MoveCoordFunctions;
import yesman.epicfight.api.animation.types.ActionAnimation;
import yesman.epicfight.api.model.Model;
import yesman.epicfight.api.utils.math.FABRIK;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderTypes;
import yesman.epicfight.client.renderer.RenderingTool;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.boss.enderdragon.EnderDragonPatch;

public class EnderDragonActionAnimation extends ActionAnimation implements ProceduralAnimation {
	private final IKInfo[] ikInfos;
	private Map<String, TransformSheet> tipPointTransforms;
	
	public EnderDragonActionAnimation(float convertTime, String path, Model model, IKInfo[] ikInfos) {
		super(convertTime, path, model);
		this.ikInfos = ikInfos;
	}
	
	@Override
	public void loadAnimation(IResourceManager resourceManager) {
		loadBothSide(resourceManager, this);
		this.tipPointTransforms = Maps.newHashMap();
		this.setIKInfo(this.ikInfos, this.getTransfroms(), this.tipPointTransforms, this.getModel().getArmature(), this.getProperty(MoveCoordFunctions.MOVE_VERTICAL).orElse(false), true);
		this.onLoaded();
	}
	
	@Override
	public Pose getPoseByTime(LivingEntityPatch<?> entitypatch, float time, float partialTicks) {
		Pose pose = super.getPoseByTime(entitypatch, time, partialTicks);
		
		if (entitypatch instanceof EnderDragonPatch) {
			EnderDragonPatch enderdragonpatch = (EnderDragonPatch)entitypatch;
	    	float x = (float)entitypatch.getOriginal().getX();
	    	float y = (float)entitypatch.getOriginal().getY();
	    	float z = (float)entitypatch.getOriginal().getZ();
	    	float xo = (float)entitypatch.getOriginal().xo;
	    	float yo = (float)entitypatch.getOriginal().yo;
	    	float zo = (float)entitypatch.getOriginal().zo;
	    	OpenMatrix4f toModelPos = OpenMatrix4f.mul(OpenMatrix4f.translate(new Vec3f(xo + (x - xo) * partialTicks, yo + (y - yo) * partialTicks, zo + (z - zo) * partialTicks), new OpenMatrix4f(), null), entitypatch.getModelMatrix(partialTicks), null).invert();
	    	this.correctRootRotation(pose.getJointTransformData().get("Root"), enderdragonpatch, partialTicks);
	    	
	    	for (IKInfo ikInfo : this.ikInfos) {
	    		TipPointAnimation tipAnim = enderdragonpatch.getTipPointAnimation(ikInfo.endJoint);
	    		JointTransform jt = tipAnim.getTipTransform(partialTicks);
		    	Vec3f jointModelpos = OpenMatrix4f.transform3v(toModelPos, jt.translation(), null);
		    	this.applyFabrikToJoint(jointModelpos.multiply(-1.0F, 1.0F, -1.0F), pose, this.getModel().getArmature(), ikInfo.startJoint, ikInfo.endJoint, jt.rotation());
	    	}
		}
		
		return pose;
	}
	
	@Override
	public void begin(LivingEntityPatch<?> entitypatch) {
		super.begin(entitypatch);
		
		if (entitypatch instanceof EnderDragonPatch) {
			EnderDragonPatch enderdragonpatch = (EnderDragonPatch)entitypatch;
			Vector3d entitypos = enderdragonpatch.getOriginal().position();
			OpenMatrix4f toWorld = OpenMatrix4f.mul(OpenMatrix4f.createTranslation((float)entitypos.x, (float)entitypos.y, (float)entitypos.z), enderdragonpatch.getModelMatrix(1.0F), null);
			
			for (IKInfo ikInfo : this.ikInfos) {
				TransformSheet tipAnim = this.getFirstPart(this.tipPointTransforms.get(ikInfo.endJoint));
				Keyframe[] keyframes = tipAnim.getKeyframes();
				JointTransform firstposeTransform = keyframes[0].transform();
				firstposeTransform.translation().multiply(-1.0F, 1.0F, -1.0F);
				
				if (!ikInfo.clipAnimation || ikInfo.touchingGround[0]) {
					Vec3f rayResultPosition = this.getRayCastedTipPosition(firstposeTransform.translation().add(0.0F, 2.5F, 0.0F), toWorld, enderdragonpatch, 8.0F, ikInfo.rayLeastHeight);
					firstposeTransform.translation().set(rayResultPosition);
				} else {
					firstposeTransform.translation().set(OpenMatrix4f.transform3v(toWorld, firstposeTransform.translation(), null));
				}
				
				for (Keyframe keyframe : keyframes) {
					keyframe.transform().translation().set(firstposeTransform.translation());
				}
				
				enderdragonpatch.addTipPointAnimation(ikInfo.endJoint, firstposeTransform.translation(), tipAnim, ikInfo);
			}
		}
	}
	
	@Override
	public void tick(LivingEntityPatch<?> entitypatch) {
		super.tick(entitypatch);
		
		if (entitypatch instanceof EnderDragonPatch) {
			EnderDragonPatch enderdragonpatch = (EnderDragonPatch)entitypatch;
			Vector3d entitypos = enderdragonpatch.getOriginal().position();
			OpenMatrix4f toWorld = OpenMatrix4f.mul(OpenMatrix4f.createTranslation((float)entitypos.x, (float)entitypos.y, (float)entitypos.z), enderdragonpatch.getModelMatrix(1.0F), null);
			float elapsedTime = entitypatch.getAnimator().getPlayerFor(this).getElapsedTime();
			
			for (IKInfo ikInfo : this.ikInfos) {
				if (ikInfo.clipAnimation) {
					Keyframe[] keyframes = this.getTransfroms().get(ikInfo.endJoint).getKeyframes();
					float startTime = keyframes[ikInfo.startFrame].time();
					float endTime = keyframes[ikInfo.endFrame - 1].time();
					
					if (startTime <= elapsedTime && elapsedTime < endTime) {
						TipPointAnimation tipAnim = enderdragonpatch.getTipPointAnimation(ikInfo.endJoint);
						Vec3f clipStart = ikInfo.endpos.copy().add(0.0F, 2.5F, 0.0F).multiply(-1.0F, 1.0F, -1.0F);
						Vec3f finalTargetpos = this.getRayCastedTipPosition(clipStart, toWorld, enderdragonpatch, 8.0F, ikInfo.rayLeastHeight);
						
						if (tipAnim.isOnWorking()) {
							tipAnim.newTargetPosition(finalTargetpos);
						} else {
							this.startPartAnimation(ikInfo, tipAnim, this.clipAnimation(this.tipPointTransforms.get(ikInfo.endJoint), ikInfo), finalTargetpos);
						}
					}
				}
			}
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void renderDebugging(MatrixStack poseStack, IRenderTypeBuffer buffer, LivingEntityPatch<?> entitypatch, float playTime, float partialTicks) {
		if (entitypatch instanceof EnderDragonPatch) {
			EnderDragonPatch enderdragonpatch = ((EnderDragonPatch)entitypatch);
			OpenMatrix4f modelmat = enderdragonpatch.getModelMatrix(partialTicks);
			LivingEntity originalEntity = entitypatch.getOriginal();
			Vector3d entitypos = originalEntity.position();
			float x = (float)entitypos.x;
	       	float y = (float)entitypos.y;
	       	float z = (float)entitypos.z;
	       	float xo = (float)originalEntity.xo;
	       	float yo = (float)originalEntity.yo;
	       	float zo = (float)originalEntity.zo;
	       	OpenMatrix4f toModelPos = OpenMatrix4f.mul(OpenMatrix4f.createTranslation(xo + (x - xo) * partialTicks, yo + (y - yo) * partialTicks, zo + (z - zo) * partialTicks), modelmat, null).invert();
	       	
			for (IKInfo ikInfo : this.ikInfos) {
				IVertexBuilder vertexBuilder = buffer.getBuffer(EpicFightRenderTypes.debugQuads());
				Vec3f worldtargetpos = enderdragonpatch.getTipPointAnimation(ikInfo.endJoint).getTargetPosition();
				Vec3f modeltargetpos = OpenMatrix4f.transform3v(toModelPos, worldtargetpos, null).multiply(-1.0F, 1.0F, -1.0F);
				
				RenderingTool.drawQuad(poseStack, vertexBuilder, modeltargetpos, 0.5F, 1.0F, 0.0F, 0.0F);
		       	Vec3f jointWorldPos = enderdragonpatch.getTipPointAnimation(ikInfo.endJoint).getTipPosition(partialTicks);
		       	Vec3f jointModelpos = OpenMatrix4f.transform3v(toModelPos, jointWorldPos, null);
		       	RenderingTool.drawQuad(poseStack, vertexBuilder, jointModelpos.multiply(-1.0F, 1.0F, -1.0F), 0.4F, 0.0F, 0.0F, 1.0F);
		       	
		       	Pose pose = new Pose();
		       	
				for (String jointName : this.jointTransforms.keySet()) {
					pose.putJointData(jointName, this.jointTransforms.get(jointName).getInterpolatedTransform(playTime));
				}
				
				FABRIK fabrik = new FABRIK(pose, this.getModel().getArmature(), ikInfo.startJoint, ikInfo.endJoint);
			   	fabrik.run(jointModelpos, 10);
		       	for (Vec3f vec : fabrik.getChainingPosition()) {
		       		RenderingTool.drawCube(poseStack, vertexBuilder, vec, 0.3F, 0.0F, 1.0F, 0.0F);
		       	}
			}
		}
	}
}