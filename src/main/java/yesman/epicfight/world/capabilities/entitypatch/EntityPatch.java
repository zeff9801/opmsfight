package yesman.epicfight.world.capabilities.entitypatch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

public abstract class EntityPatch<T extends Entity> {
	protected T original;
	protected boolean initialized = false;

	public void tick(LivingUpdateEvent event) {
		if (this.isLogicalClient()) {
			this.clientTick(event);
		} else {
			this.serverTick(event);
		}
	}

	protected void clientTick(LivingUpdateEvent event) {}
	protected void serverTick(LivingUpdateEvent event) {}

	public abstract boolean overrideRender();

	public void onStartTracking(ServerPlayerEntity trackingPlayer) {
	}

	@OnlyIn(Dist.CLIENT)
	public void processSpawnData(ByteBuf buf) {
	}

	public void onConstructed(T entityIn) {
		this.original = entityIn;
	}

	public void onJoinWorld(T entityIn, EntityJoinWorldEvent event) {
		this.initialized = true;
	}

	public void onDeath(LivingDeathEvent event) {
	}

	public final T getOriginal() {
		return this.original;
	}

	public boolean isInitialized() {
		return this.initialized;
	}

	public boolean isLogicalClient() {
		return this.original.level.isClientSide();
	}

	public OpenMatrix4f getMatrix(float partialTicks) {
		return MathUtils.getModelMatrixIntegral(0, 0, 0, 0, 0, 0, this.original.xRotO, this.original.xRot, this.original.yRotO, this.original.yRot, partialTicks, 1, 1, 1);
	}

	public double getAngleTo(Entity entityIn) {
		Vector3d a = this.original.getLookAngle();
		Vector3d b = new Vector3d(entityIn.getX() - this.original.getX(), entityIn.getY() - this.original.getY(), entityIn.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);

		return Math.toDegrees(Math.acos(cos));
	}

	public double getAngleToHorizontal(Entity entityIn) {
		Vector3d a = this.original.getLookAngle();
		Vector3d b = new Vector3d(entityIn.getX() - this.original.getX(), 0.0D, entityIn.getZ() - this.original.getZ()).normalize();
		double cos = (a.x * b.x + a.y * b.y + a.z * b.z);

		return Math.toDegrees(Math.acos(cos));
	}

	public abstract OpenMatrix4f getModelMatrix(float partialTicks);
}