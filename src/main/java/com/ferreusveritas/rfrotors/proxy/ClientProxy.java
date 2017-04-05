package com.ferreusveritas.rfrotors.proxy;
import com.ferreusveritas.rfrotors.tileentities.RenderTileEntityRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;

import cpw.mods.fml.client.registry.ClientRegistry;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class ClientProxy extends CommonProxy {

    public void registerTileEntities() {
        super.registerTileEntities();
        TileEntitySpecialRenderer rotorRenderer = new RenderTileEntityRotorBlock();
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindRotorBlock.class, rotorRenderer);
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaterRotorBlock.class, rotorRenderer);
    }
}
