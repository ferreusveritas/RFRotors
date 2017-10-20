package com.ferreusveritas.rfrotors.lib;

import com.ferreusveritas.rfrotors.blocks.BlockRotor;

import net.minecraft.util.EnumFacing;

public interface IRotor {

	public EnergyPacket getEnergyPacket();
	
	public boolean attach(EnumFacing dir);
	
	public BlockRotor.EnumType getType();
	
	public float getRotation(float dt);
	
	public EnumFacing getDirection();
}
