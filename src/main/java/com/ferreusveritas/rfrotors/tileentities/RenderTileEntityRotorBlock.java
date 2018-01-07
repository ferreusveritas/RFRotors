package com.ferreusveritas.rfrotors.tileentities;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.rfrotors.blocks.BlockRotor;
import com.google.common.collect.ImmutableMap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.Attributes;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.model.TRSRTransformation;

/**
 * Handles custom rendering of {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}
 * by replacing it with a rotating 3d rotor model. All possible textures are
 * stored in the renderer and determined at runtime via the metadata of the
 * corresponding {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}.
 */
public class RenderTileEntityRotorBlock extends TileEntitySpecialRenderer<TileEntityRotorBlock> {

	
	private Map<BlockRotor.EnumType, IBakedModel> bakedModelMap = new EnumMap<BlockRotor.EnumType, IBakedModel>(BlockRotor.EnumType.class);

	private IBakedModel getCachedBakedModel(BlockRotor.EnumType rotorType, ResourceLocation rawModelLocation) {
		IBakedModel bakedModel = null;

		BlockRotor.EnumType type = rotorType;
		if(bakedModelMap.containsKey(type)) {
			bakedModel = bakedModelMap.get(type);
		} else {
			try {
				IModel rawModel = OBJLoader.INSTANCE.loadModel(rawModelLocation);
				bakedModel = getBakedRotorModel(rawModel);
				bakedModelMap.put(type, bakedModel);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		
		return bakedModel;
	}
	
	@Override
	public void render(TileEntityRotorBlock entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		
		float rotation = entity.getRotation(partialTicks);
		
		EnumFacing dir = entity.getDirection();
		if(dir == null){
			return;
		}
				
		IBakedModel bakedRotorModel = getCachedBakedModel(entity.getType(), entity.getModel());
		bindTexture(entity.getTexture());
		
		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();
		
		GlStateManager.translate(x, y, z);
		
		// Position the rotor on the centre of the face 
		GL11.glTranslated(0.5f - (dir.getFrontOffsetX() * 0.5f), 0.5f, 0.5f - (dir.getFrontOffsetZ() * 0.5f));

		//Now turn it the right way
		int d = (((dir.ordinal() + 2) * 5) >> 1) & 3;//Convert EnumFacing ordinals 2,3,4,5 to factors 2,0,3,1 respectively
		GlStateManager.rotate(90.0f * d, 0, 1.0f, 0);
				
		//Now rotate it along it's hub to it's current orientation
		GlStateManager.rotate(rotation, 0, 0, 1.0f);
		
		//Some models need to change direction depending on which way they are turning
		boolean flipped = entity.isFlipped();
		
		if(flipped){
			GlStateManager.scale(-1.0f, 1.0f, 1.0f);
			GL11.glFrontFace(GL11.GL_CW);
		}
		
		GlStateManager.disableLighting();
		
		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelBrightnessColor(
				getWorld().getBlockState(entity.getPos()),
				bakedRotorModel,
				1.0f, //Brightness
				1.0f, 1.0f, 1.0f);//R, G, B
		
		if(flipped){
			GL11.glFrontFace(GL11.GL_CCW);
		}
		
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();
	}
	
	/**
	 * This is necessary for Minecraft to use for an
	 * external 3d mesh that uses 0.0 to 1.0 for it's
	 * UV coordinates.  Minecraft apparently internally
	 * uses 0.0 to 16.0 for it's UVs.
	 * 
	 * @author ferreusveritas
	 *
	 */
	private static class BlenderOBJAtlas extends TextureAtlasSprite {
		public static BlenderOBJAtlas instance = new BlenderOBJAtlas();
		
		protected BlenderOBJAtlas() {
			super("blender");
		}
		
		@Override
		public float getInterpolatedU(double u) {
			return (float)u / 16;//Scale the coordinates down by 16x
		}
		
		@Override
		public float getInterpolatedV(double v) {
			return (float)v / -16;//Also flips the V coordinates in addition to scaling
		}
		
	}
	
	private IBakedModel getBakedRotorModel(IModel model) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builder.put("ambient", "false");//Also available: "gui3d", "flip-v"
		model = model.process(builder.build());
			
		return model.bake(
			TRSRTransformation.identity(),
			Attributes.DEFAULT_BAKED_FORMAT,
			new Function<ResourceLocation, TextureAtlasSprite>() {
				@Override
				public TextureAtlasSprite apply(ResourceLocation location) {
					return BlenderOBJAtlas.instance;
				}
			}
		);
	}
}
