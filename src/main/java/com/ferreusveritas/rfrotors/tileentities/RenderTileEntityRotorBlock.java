package com.ferreusveritas.rfrotors.tileentities;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.lwjgl.opengl.GL11;

import com.ferreusveritas.rfrotors.lib.IModelProvider;
import com.ferreusveritas.rfrotors.lib.IRotor;

/**
 * Handles custom rendering of {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}
 * by replacing it with a rotating 3d rotor model. All possible textures are
 * stored in the renderer and determined at runtime via the metadata of the
 * corresponding {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}.
 */
public class RenderTileEntityRotorBlock extends TileEntitySpecialRenderer {

	public RenderTileEntityRotorBlock() {
		TileEntityWindRotorBlock.initResources();
		TileEntityWaterRotorBlock.initResources();
	}


	
	@Override
	public void render(TileEntity entity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
	//public void renderTileEntityAt(TileEntity entity, double x, double y, double z, float dt) {
		
		if(!(entity instanceof IRotor) || !(entity instanceof IModelProvider)){
			return;
		}
		
		IRotor rotor = (IRotor) entity;
		IModelProvider model = (IModelProvider) entity;
		float rotation = rotor.getRotation(partialTicks);
		EnumFacing dir = rotor.getDirection();
		if(dir == null){
			return;
		}
		
		bindTexture(model.getTexture());
		GL11.glPushMatrix();
		
		// Position the rotor on the centre of the face and turn it the right way
		GL11.glTranslated(x + 0.5f - (dir.getFrontOffsetX() * 0.5f), y + 0.5f, z + 0.5f - (dir.getFrontOffsetZ() * 0.5f));
		int d = (((dir.ordinal() + 2) * 5) >> 1) & 3;//Convert FD ordinals 2,3,4,5 to factors 2,0,3,1 respectively 
		GL11.glRotatef(90.0f * d, 0, 1.0f, 0);
		
		GL11.glRotatef(rotation, 0, 0, 1.0f);
		
		boolean flipped = model.flip(); 
		
		if(flipped){
			GL11.glScalef(-1.0f, 1.0f, 1.0f);
			GL11.glFrontFace(GL11.GL_CW);
		}
		
		GL11.glPushMatrix();
		//TODO: Port to 1.12.2
		//model.getModel().renderAll();
		GL11.glPopMatrix();
		
		if(flipped){
			GL11.glFrontFace(GL11.GL_CCW);
		}
		
		GL11.glPopMatrix();
	}
}
