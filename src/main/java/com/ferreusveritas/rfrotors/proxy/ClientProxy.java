package com.ferreusveritas.rfrotors.proxy;
import com.ferreusveritas.rfrotors.ModBlocks;
import com.ferreusveritas.rfrotors.ModItems;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.tileentities.RenderTileEntityRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		OBJLoader.INSTANCE.addDomain(Constants.MODID);
	}
	
	
	public void registerTileEntities() {
		super.registerTileEntities();
		TileEntitySpecialRenderer rotorRenderer = new RenderTileEntityRotorBlock();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWindRotorBlock.class, rotorRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWaterRotorBlock.class, rotorRenderer);
	}
	
	public void registerRenderers() {
		regMesh(ModItems.dustChromel);
		regMesh(ModItems.ingotChromel);
		regMesh(ModItems.nuggetChromel);
		regMesh(ModItems.modernRotorBlade);
		regMesh(ModItems.sailRotorBlade);
		
		regMesh(Item.getItemFromBlock(ModBlocks.generatorBlock));
	}

	private void regMesh(Item item) {
		regMesh(item, 0);
	}
	
	private void regMesh(Item item, int meta) {
		ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		mesher.register(item, meta, new ModelResourceLocation(item.getRegistryName(), "inventory"));

		//Register Color Handler for the item.
		if(item instanceof IItemColor) {
			Minecraft.getMinecraft().getItemColors().registerItemColorHandler((IItemColor) item, new Item[] {item});
		}
	}
	
}
