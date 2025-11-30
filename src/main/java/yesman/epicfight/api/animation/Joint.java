package yesman.epicfight.api.animation;

import com.google.common.collect.Lists;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

import java.util.*;

public class Joint {
	public static final Joint EMPTY = new Joint("empty", -1, new OpenMatrix4f());

	private final List<Joint> subJoints = Lists.newArrayList();
	private final int jointId;
	private final String jointName;
	private final OpenMatrix4f localTransform;
	private final OpenMatrix4f toOrigin = new OpenMatrix4f();

	public Joint(String name, int jointId, OpenMatrix4f localTransform) {
		this.jointId = jointId;
		this.jointName = name;
		this.localTransform = localTransform;
	}

	public void addSubJoint(Joint... joints) {
		Collections.addAll(this.subJoints, joints);
		// If subJoints are immutable after construction, consider wrapping with Collections.unmodifiableList
	}

	public List<Joint> getAllJoints() {
		List<Joint> list = Lists.newArrayList();
		this.getAllJoints(list);

		return list;
	}

	private void getAllJoints(List<Joint> list) {
		// Recursive; could be replaced with iterative for deep hierarchies
		list.add(this);
		for (Joint joint : this.subJoints) {
			joint.getAllJoints(list);
		}
	}

	public void initOriginTransform(OpenMatrix4f parentTransform) {
		OpenMatrix4f modelTransform = OpenMatrix4f.mul(parentTransform, this.localTransform, null);
		OpenMatrix4f.invert(modelTransform, this.toOrigin);

		for (Joint joint : this.subJoints) {
			joint.initOriginTransform(modelTransform);
		}
	}

	public OpenMatrix4f getLocalTransform() {
		return this.localTransform;
	}

	public OpenMatrix4f getToOrigin() {
		return this.toOrigin;
	}

	public List<Joint> getSubJoints() {
		return this.subJoints;
	}

	public String getName() {
		return this.jointName;
	}

	@Override
	public String toString() {
		return this.jointName;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Joint joint) {
			return this.jointName.equals(joint.jointName) && this.jointId == joint.jointId;
		} else {
			return super.equals(o);
		}
	}

	@Override
	public int hashCode() {
		return this.jointName.hashCode() + this.jointId;
	}

	public int getId() {
		return this.jointId;
	}

	public String searchPath(String path, String joint) {
		if (joint.equals(this.getName())) {
			return path;
		} else {
			int i = 1;
			for (Joint subJoint : this.subJoints) {
				String str = subJoint.searchPath(i + path, joint);
				i++;
				if (str != null) {
					return str;
				}
			}
			return null;
		}
	}
	public static class HierarchicalJointAccessor {
		private Queue<Integer> indicesToTerminal;
		private final String signature;

		private HierarchicalJointAccessor(Builder builder) {
			this.indicesToTerminal = builder.indicesToTerminal;
			this.signature = builder.signature;
		}

		public AccessTicket createAccessTicket(Joint rootJoint) {
			return new AccessTicket(this.indicesToTerminal, rootJoint);
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof HierarchicalJointAccessor accessor) {
				this.signature.equals(accessor.signature);
			}

			return super.equals(o);
		}

		@Override
		public int hashCode() {
			return this.signature.hashCode();
		}

		public static Builder builder() {
			return new Builder(new LinkedList<>(), "");
		}

		public static class Builder {
			private Queue<Integer> indicesToTerminal;
			private String signature;

			private Builder(Queue<Integer> indicesToTerminal, String signature) {
				this.indicesToTerminal = indicesToTerminal;
				this.signature = signature;
			}

			public Builder append(int index) {
				String signatureNext;

				if (this.indicesToTerminal.isEmpty()) {
					signatureNext = this.signature + String.valueOf(index);
				} else {
					signatureNext = this.signature + "-" + String.valueOf(index);
				}

				Queue<Integer> nextQueue = new LinkedList<> (this.indicesToTerminal);
				nextQueue.add(index);

				return new Builder(nextQueue, signatureNext);
			}

			public HierarchicalJointAccessor build() {
				return new HierarchicalJointAccessor(this);
			}
		}
	}

	public static class AccessTicket implements Iterator<Joint> {
		Queue<Integer> accecssStack;
		Joint joint;

		private AccessTicket(Queue<Integer> indicesToTerminal, Joint rootJoint) {
			this.accecssStack = new LinkedList<> (indicesToTerminal);
			this.joint = rootJoint;
		}

		public boolean hasNext() {
			return !this.accecssStack.isEmpty();
		}

		public Joint next() {
			if (this.hasNext()) {
				int nextIndex = this.accecssStack.poll();
				this.joint = this.joint.subJoints.get(nextIndex);
			} else {
				throw new NoSuchElementException();
			}

			return this.joint;
		}
	}
}
