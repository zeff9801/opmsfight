package yesman.epicfight.api.client.model;


import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AnimatedVertexBuilder extends VertexBuilder {
	public final Vector3i joint;
	public final Vector3i weight;
	public final int count;
	
	public AnimatedVertexBuilder(int position, int uv, int normal, Vector3i joint, Vector3i weight, int count) {
		super(position, uv, normal);
		
		this.joint = joint;
		this.weight = weight;
		this.count = count;
	}
	
	public int getJointId(int index) {
		switch (index) {
		case 0:
			return this.joint.getX();
		case 1:
			return this.joint.getY();
		case 2:
			return this.joint.getZ();
		default:
			return -1;
		}
	}
	
	public int getWeightIndex(int index) {
		switch (index) {
		case 0:
			return this.weight.getX();
		case 1:
			return this.weight.getY();
		case 2:
			return this.weight.getZ();
		default:
			return -1;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof AnimatedVertexBuilder vb) {
			return this.position == vb.position && this.uv == vb.uv && this.normal == vb.normal && this.count == vb.count && this.joint.getX() == vb.joint.getX() && this.joint.getY() == vb.joint.getY() && this.joint.getZ() == vb.joint.getZ()
					&& this.weight.getX() == vb.weight.getX() && this.weight.getY() == vb.weight.getY() && this.weight.getZ() == vb.weight.getZ();
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
        final int prime = 31;
        int result = 1;
        
        result = prime * result + this.position;
        result = prime * result + this.uv;
        result = prime * result + this.normal;
        result = prime * result + this.count;
        result = prime * result + this.joint.getX();
        result = prime * result + this.joint.getY();
        result = prime * result + this.joint.getZ();
        result = prime * result + this.weight.getX();
        result = prime * result + this.weight.getY();
        result = prime * result + this.weight.getZ();
        
        return result;
    }
}