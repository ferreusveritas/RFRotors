package com.ferreusveritas.rfrotors.lib;

import net.minecraft.util.EnumFacing;

public interface IRotor {

	public EnergyPacket getEnergyPacket();
	
	public boolean attach(EnumFacing dir);
	
	public int getType();
	
	public float getRotation(float dt);
	
	public EnumFacing getDirection();
}
