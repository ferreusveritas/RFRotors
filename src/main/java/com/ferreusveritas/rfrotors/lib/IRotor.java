package com.ferreusveritas.rfrotors.lib;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;

public interface IRotor {

	public EnergyPacket getEnergyPacket();
	
	public boolean attach(ForgeDirection dir);

	public int getType();
	
    public float getRotation(float dt);
        
    public ForgeDirection getDirection();
}
