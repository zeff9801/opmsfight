package yesman.epicfight.client.renderer.shader;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface AnimationShaderInstance {
	public ShaderUniform getModelViewMatrixShaderUniform();
	
	public ShaderUniform getProjectionMatrixShaderUniform();
	
	public ShaderUniform getInverseViewRotationMatrixShaderUniform();
	
	public ShaderUniform getColorModulatorShaderUniform();
	
	public ShaderUniform getGlintAlphaShaderUniform();
	
	public ShaderUniform getFogStartShaderUniform();
	
	public ShaderUniform getFogEndShaderUniform();
	
	public ShaderUniform getFogColorShaderUniform();
	
	public ShaderUniform getFogShapeShaderUniform();
	
	public ShaderUniform getTextureMatrixShaderUniform();
	
	public ShaderUniform getGameTimeShaderUniform();
	
	public ShaderUniform getScreenSizeShaderUniform();
	
	public ShaderUniform getColorShaderUniform();
	
	public ShaderUniform getOverlayShaderUniform();
	
	public ShaderUniform getLightShaderUniform();
	
	public ShaderUniform getNormalMatrixShaderUniform();
	
	public ShaderUniform getPoses(int i);
	
	public void _setSampler(String samplerName, Object texture);

	public String _getName();
	
	public void _apply();
	
	public void _clear();
	
	public void _close();
	
	public VertexFormat _getVertexFormat();
}