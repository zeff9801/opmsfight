package yesman.epicfight.api.client.model;

import com.google.common.collect.Lists;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class VertexBuilder {
	public static List<VertexBuilder> createVertexIndicator(int[] drawingIndices) {
		List<VertexBuilder> vertexIndicators = Lists.newArrayList();
		
		for (int i = 0; i < drawingIndices.length / 3; i++) {
			int k = i * 3;
			int position = drawingIndices[k];
			int uv = drawingIndices[k + 1];
			int normal = drawingIndices[k + 2];
			VertexBuilder vi = new VertexBuilder(position, uv, normal);
			vertexIndicators.add(vi);
		}
		
		return vertexIndicators;
	}
	
	public static List<AnimatedVertexBuilder> createAnimated(int[] drawingIndices, int[] affectingJointCount, int[] animationIndices) {
		List<AnimatedVertexBuilder> vertexIndicators = Lists.newArrayList();
		Vector3i[] aJointId = new Vector3i[affectingJointCount.length];
		Vector3i[] aWeights = new Vector3i[affectingJointCount.length];
		int[] counts = new int[affectingJointCount.length];
		int indexPointer = 0;
		
		for (int i = 0; i < affectingJointCount.length; i++) {
			int count = affectingJointCount[i];
			Vector3i jointId = new Vector3i(-1, -1, -1);
			Vector3i weights = new Vector3i(-1, -1, -1);
			
			for (int j = 0; j < count; j++) {
				switch (j) {
				case 0 -> {
					jointId = new Vector3i(animationIndices[indexPointer * 2], jointId.getY(), jointId.getZ());
					weights = new Vector3i(animationIndices[indexPointer * 2 + 1], weights.getY(), weights.getZ());
				}
					case 1 -> {
						jointId = new Vector3i(jointId.getX(), animationIndices[indexPointer * 2], jointId.getZ());
						weights = new Vector3i(weights.getX(), animationIndices[indexPointer * 2 + 1], weights.getZ());
					}
					case 2 -> {
						jointId = new Vector3i(jointId.getX(), jointId.getY(), animationIndices[indexPointer * 2]);
						weights = new Vector3i(weights.getX(), weights.getY(), animationIndices[indexPointer * 2 + 1]);
					}
				}
				
				indexPointer++;
			}
			
			counts[i] = count;
			aJointId[i] = jointId;
			aWeights[i] = weights;
		}
		
		for (int i = 0; i < drawingIndices.length / 3; i++) {
			int k = i * 3;
			int position = drawingIndices[k];
			int uv = drawingIndices[k + 1];
			int normal = drawingIndices[k + 2];
			AnimatedVertexBuilder vi = new AnimatedVertexBuilder(position, uv, normal, aJointId[position], aWeights[position], counts[position]);
			vertexIndicators.add(vi);
		}
		
		return vertexIndicators;
	}
	
	public final int position;
	public final int uv;
	public final int normal;
	
	public VertexBuilder(int position, int uv, int normal) {
		this.position = position;
		this.uv = uv;
		this.normal = normal;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof VertexBuilder vb) {
			return this.position == vb.position && this.uv == vb.uv && this.normal == vb.normal;
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
        
        return result;
    }
}