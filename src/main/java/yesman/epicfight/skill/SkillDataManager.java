
package yesman.epicfight.skill;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.Maps;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.client.CPModifySkillData;
import yesman.epicfight.network.server.SPAddOrRemoveSkillData;
import yesman.epicfight.network.server.SPModifySkillData;

public class SkillDataManager {
	private final Map<SkillDataKey<?>, Object> data = Maps.newHashMap();
	private final int slotIndex;
	private final SkillContainer container;

	public SkillDataManager(int slotIndex, SkillContainer container) {
		this.slotIndex = slotIndex;
		this.container = container;
	}

	public <T> void registerData(SkillDataKey<T> key) {
		if (this.hasData(key)) {
			throw new IllegalStateException("Skill dat key " + key + " already registered!");
		}

		this.data.put(key, key.defaultValue());

		if (key.syncronizeTrackingPlayers() && !this.container.getExecuter().isLogicalClient()) {
			PlayerEntity owner = this.container.getExecuter().getOriginal();

			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
					new SPAddOrRemoveSkillData(key, container.getSlot().universalOrdinal(), key.defaultValue(), SPAddOrRemoveSkillData.AddRemove.ADD, owner.getId()),
					owner);
		}
	}

	public <T> void removeData(SkillDataKey<T> key) {
		this.data.remove(key);

		if (key.syncronizeTrackingPlayers() && !this.container.getExecuter().isLogicalClient()) {
			PlayerEntity owner = this.container.getExecuter().getOriginal();

			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(
					new SPAddOrRemoveSkillData(key, container.getSlot().universalOrdinal(), null, SPAddOrRemoveSkillData.AddRemove.REMOVE, owner.getId()),
					owner);
		}
	}

	public Set<SkillDataKey<?>> keySet() {
		return this.data.keySet();
	}

	/**
	 * Use setData() or setDataSync() which is type-safe
	 */
	@Deprecated
	public void setDataRawtype(SkillDataKey<?> key, Object data) {
		this.data.computeIfPresent(key, (theKey, val) -> data);
	}

	public <T> void setData(SkillDataKey<T> key, T data) {
		this.setDataRawtype(key, data);
	}

	public <T> void setDataF(SkillDataKey<T> key, Function<T, T> dataManipulator) {
		this.setDataRawtype(key, dataManipulator.apply(this.getDataValue(key)));
	}

	public <T> void setDataSync(SkillDataKey<T> key, T data, ServerPlayerEntity player) {
		this.setData(key, data);
		this.syncData(key, player);
	}

	public <T> void setDataSyncF(SkillDataKey<T> key, Function<T, T> dataManipulator, ServerPlayerEntity player) {
		this.setDataF(key, dataManipulator);
		SPModifySkillData msg = new SPModifySkillData(key, this.slotIndex, this.getDataValue(key), player.getId());
		EpicFightNetworkManager.sendToPlayer(msg, player);

		if (key.syncronizeTrackingPlayers()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, player);
		}
	}

	public <T> void syncData(SkillDataKey<T> key, ServerPlayerEntity player) {
		SPModifySkillData msg = new SPModifySkillData(key, this.slotIndex, this.getDataValue(key), player.getId());
		EpicFightNetworkManager.sendToPlayer(msg, player);

		if (key.syncronizeTrackingPlayers()) {
			EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, player);
		}
	}

	@OnlyIn(Dist.CLIENT)
	public <T> void syncData(SkillDataKey<T> key, ClientPlayerEntity player) {
		CPModifySkillData msg = new CPModifySkillData(key, this.slotIndex, this.getDataValue(key));
		EpicFightNetworkManager.sendToServer(msg);
	}

	@OnlyIn(Dist.CLIENT)
	public <T> void setDataSync(SkillDataKey<T> key, T data, ClientPlayerEntity player) {
		this.setData(key, data);
		this.syncData(key, player);
	}

	@SuppressWarnings("unchecked")
	public <T> T getDataValue(SkillDataKey<T> key) {
		return this.hasData(key) ? (T)this.data.get(key) : null;
	}

	public boolean hasData(SkillDataKey<?> key) {
		return this.data.containsKey(key);
	}

	public void clearData() {
		this.data.clear();
	}
}
