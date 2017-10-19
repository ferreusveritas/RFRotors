package com.ferreusveritas.rfrotors;

import com.ferreusveritas.rfrotors.blocks.ModBlocks;
import com.ferreusveritas.rfrotors.items.ModItems;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;
import com.ferreusveritas.rfrotors.proxy.CommonProxy;
import com.ferreusveritas.rfrotors.recipes.ModRecipes;
import com.ferreusveritas.rfrotors.util.WindManager;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION, dependencies = "required-after:ThermalFoundation;required-after:ThermalExpansion;after:ProjRed|Core")
public class RFRotors {

	@SidedProxy(clientSide = Constants.CLIENT_PROXY_CLASS, serverSide = Constants.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	public static WindManager windManager = new WindManager();
	public static final RotorsTab rotorsTab = new RotorsTab();
	
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent pEvent) {
		ModConfiguration.init(pEvent.getSuggestedConfigurationFile());
		ModBlocks.init();
		ModItems.init();
	}
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent pEvent) {
		proxy.registerTileEntities();
		ModRecipes.init();
	}
	
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent pEvent) {
	
	}
}
