package com.ferreusveritas.rfrotors.proxy;

import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;

import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void preInit() {}
	
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityGeneratorBlock.class, TileEntityGeneratorBlock.publicName);
		GameRegistry.registerTileEntity(TileEntityWindRotorBlock.class, TileEntityWindRotorBlock.publicName);
		GameRegistry.registerTileEntity(TileEntityWaterRotorBlock.class, TileEntityWaterRotorBlock.publicName);
	}

	public void registerRenderers() {}
}
