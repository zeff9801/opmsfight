package yesman.epicfight.api.utils.math;

public class ValueModifier {
	private float adders;
	private float multipliers;
	private float setters;
	
	public ValueModifier(float adder, float multiplier, float setter) {
		this.adders = adder;
		this.multipliers = multiplier;
		this.setters = setter;
	}
	
	public void merge(ValueModifier valueModifier) {
		this.adders += valueModifier.adders;
		this.multipliers *= valueModifier.multipliers;
		this.setters += valueModifier.setters;
	}
	
	public float getTotalValue(float value) {
		return this.setters == 0 ? (value * this.multipliers) + this.adders : this.setters;
	}
	
	@Override
	public String toString() {
		return this.setters == 0
				? String.format("%.0f%%", this.multipliers * 100.0F) + (this.adders == 0 ? "" : String.format(" + %.1f", this.adders))
				: String.format("%.0f", this.setters);
	}
	
	public static ValueModifier empty() {
		return new ValueModifier(0.0F, 1.0F, 0.0F);
	}
	
	public static ValueModifier adder(float arg) {
		return new ValueModifier(arg, 1.0F, 0.0F);
	}
	
	public static ValueModifier multiplier(float arg) {
		return new ValueModifier(0.0F, arg, 0.0F);
	}
	
	public static ValueModifier setter(float arg) {
		return new ValueModifier(0.0F, 1.0F, arg);
	}
}