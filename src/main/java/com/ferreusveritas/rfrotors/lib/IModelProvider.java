package com.ferreusveritas.rfrotors.lib;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;

public interface IModelProvider {

	public IModelCustom getModel();
	
    public ResourceLocation getTexture();
    
    public boolean flip();
	
}
