package com.ferreusveritas.rfrotors.lib;

import net.minecraft.util.ResourceLocation;

public interface IModelProvider {

	public ResourceLocation getModel();
	
	public ResourceLocation getTexture();
	
	public boolean isFlipped();
	
}
