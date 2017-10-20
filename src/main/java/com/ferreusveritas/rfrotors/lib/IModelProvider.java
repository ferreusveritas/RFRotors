package com.ferreusveritas.rfrotors.lib;

import net.minecraft.util.ResourceLocation;

public interface IModelProvider {

	//FIXME: Port to 1.12.2..  the following is nonsense
	public String getModel();
	
	public ResourceLocation getTexture();
	
	public boolean flip();
	
}
