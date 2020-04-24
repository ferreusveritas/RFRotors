package com.ferreusveritas.rfrotors.proxy;

import com.ferreusveritas.rfrotors.ModConstants;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy {

	public void preInit() {}
	
	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityGeneratorBlock.class, new ResourceLocation(ModConstants.MODID, TileEntityGeneratorBlock.publicName));
		GameRegistry.registerTileEntity(TileEntityWindRotorBlock.class, new ResourceLocation(ModConstants.MODID, TileEntityWindRotorBlock.publicName));
		GameRegistry.registerTileEntity(TileEntityWaterRotorBlock.class, new ResourceLocation(ModConstants.MODID, TileEntityWaterRotorBlock.publicName));
	}

	public void registerRenderers() {}
}
