package com.ferreusveritas.rfrotors.proxy;


import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;

import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void registerTileEntities() {
        GameRegistry.registerTileEntity(TileEntityGeneratorBlock.class, TileEntityGeneratorBlock.publicName);
        GameRegistry.registerTileEntity(TileEntityWindRotorBlock.class, TileEntityWindRotorBlock.publicName);
        GameRegistry.registerTileEntity(TileEntityWaterRotorBlock.class, TileEntityWaterRotorBlock.publicName);
    }
}
