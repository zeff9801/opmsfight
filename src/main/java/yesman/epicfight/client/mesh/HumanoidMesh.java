package yesman.epicfight.client.mesh;

import java.util.Map;

import net.minecraft.inventory.EquipmentSlotType;
import yesman.epicfight.api.client.model.AnimatedMesh;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.api.client.model.ModelPart;
import yesman.epicfight.api.client.model.VertexIndicator.AnimatedVertexIndicator;

public class HumanoidMesh extends AnimatedMesh {
    public final ModelPart<AnimatedVertexIndicator> head;
    public final ModelPart<AnimatedVertexIndicator> torso;
    public final ModelPart<AnimatedVertexIndicator> leftArm;
    public final ModelPart<AnimatedVertexIndicator> rightArm;
    public final ModelPart<AnimatedVertexIndicator> leftLeg;
    public final ModelPart<AnimatedVertexIndicator> rightLeg;
    public final ModelPart<AnimatedVertexIndicator> hat;
    public final ModelPart<AnimatedVertexIndicator> jacket;
    public final ModelPart<AnimatedVertexIndicator> leftSleeve;
    public final ModelPart<AnimatedVertexIndicator> rightSleeve;
    public final ModelPart<AnimatedVertexIndicator> leftPants;
    public final ModelPart<AnimatedVertexIndicator> rightPants;

    public HumanoidMesh(Map<String, float[]> arrayMap, AnimatedMesh parent, RenderProperties properties, Map<String, ModelPart<AnimatedVertexIndicator>> parts) {
        super(arrayMap, parent, properties, parts);

        this.head = this.getOrLogException(parts, "head");
        this.torso = this.getOrLogException(parts, "torso");
        this.leftArm = this.getOrLogException(parts, "leftArm");
        this.rightArm = this.getOrLogException(parts, "rightArm");
        this.leftLeg = this.getOrLogException(parts, "leftLeg");
        this.rightLeg = this.getOrLogException(parts, "rightLeg");

        this.hat = this.getOrLogException(parts, "hat");
        this.jacket = this.getOrLogException(parts, "jacket");
        this.leftSleeve = this.getOrLogException(parts, "leftSleeve");
        this.rightSleeve = this.getOrLogException(parts, "rightSleeve");
        this.leftPants = this.getOrLogException(parts, "leftPants");
        this.rightPants = this.getOrLogException(parts, "rightPants");
    }

    public AnimatedMesh getHumanoidArmorModel(EquipmentSlotType slot) {
        switch (slot) {
            case HEAD:
                return Meshes.HELMET;
            case CHEST:
                return Meshes.CHESTPLATE;
            case LEGS:
                return Meshes.LEGGINS;
            case FEET:
                return Meshes.BOOTS;
            default:
                return null;
        }
    }
}