package yesman.epicfight.data.conditions.entity;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.nbt.CompoundNBT;
import yesman.epicfight.data.conditions.Condition.MobPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.MobPatch;

public class TargetInPov extends MobPatchCondition {
	protected double min;
	protected double max;
	
	public TargetInPov() {
	}
	
	public TargetInPov(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public TargetInPov read(CompoundNBT tag) {
		if (!tag.contains("min")) {
			throw new IllegalArgumentException("TargetInPov condition error: min degree not specified!");
		}
		
		if (!tag.contains("max")) {
			throw new IllegalArgumentException("TargetInPov condition error: max degree not specified!");
		}
		
		this.min = tag.getDouble("min");
		this.max = tag.getDouble("max");
		
		return this;
	}
	
	@Override
	public CompoundNBT serializePredicate() {
		CompoundNBT tag = new CompoundNBT();
		tag.putDouble("min", this.min);
		tag.putDouble("max", this.max);
		
		return tag;
	}
	
	@Override
	public boolean predicate(MobPatch<?> entitypatch) {
		double degree = entitypatch.getAngleTo(entitypatch.getTarget());
		return this.min < degree && degree < this.max;
	}

	@Override
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return List.of();
	}

	public static class TargetInPovHorizontal extends TargetInPov {
		public TargetInPovHorizontal() {
		}
		
		public TargetInPovHorizontal(double min, double max) {
			super(min, max);
		}
		
		@Override
		public boolean predicate(MobPatch<?> entitypatch) {
			double degree = entitypatch.getAngleToHorizontal(entitypatch.getTarget());
			return this.min < degree && degree < this.max;
		}
	}
}