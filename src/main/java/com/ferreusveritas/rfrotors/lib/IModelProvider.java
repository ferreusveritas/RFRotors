package com.ferreusveritas.rfrotors.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;

public interface IModelProvider {

	public ResourceLocation getModel();
	
	public ResourceLocation getTexture();
	
	public boolean isFlipped();
	
}
