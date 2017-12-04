package com.ferreusveritas.rfrotors;

import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;
import com.ferreusveritas.rfrotors.proxy.CommonProxy;
import com.ferreusveritas.rfrotors.util.WindManager;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

@Mod(modid = Constants.MODID, name = Constants.NAME, version = Constants.VERSION, dependencies = "required-after:ThermalFoundation;required-after:ThermalExpansion;after:ProjRed|Core")
public class RFRotors {

	@SidedProxy(clientSide = Constants.CLIENT_PROXY_CLASS, serverSide = Constants.SERVER_PROXY_CLASS)
	public static CommonProxy proxy;
	
	public static WindManager windManager = new WindManager();
	public static final RotorsTab rotorsTab = new RotorsTab();
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent pEvent) {
		ModConfiguration.init(pEvent.getSuggestedConfigurationFile());		
		ModBlocks.preInit();		
		ModItems.preInit();
		
		proxy.preInit();
	}
	
	@EventHandler
	public void init(FMLInitializationEvent pEvent) {
		proxy.registerTileEntities();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent pEvent) {
	
	}
	
	///////////////////////////////////////////
	// REGISTRATION
	///////////////////////////////////////////
	
	@Mod.EventBusSubscriber(modid = Constants.MODID)
	public static class RegistrationHandler {

		@SubscribeEvent
		public static void registerBlocks(final RegistryEvent.Register<Block> event) {
			final IForgeRegistry<Block> registry = event.getRegistry();
			ModBlocks.register(registry);
		}
		
		@SubscribeEvent
		public static void registerItems(final RegistryEvent.Register<Item> event) {
			final IForgeRegistry<Item> registry = event.getRegistry();
			ModItems.register(registry);
		}
		
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void registerRecipes(final RegistryEvent.Register<IRecipe> event) {
			final IForgeRegistry<IRecipe> registry = event.getRegistry();
			ModRecipes.register(registry);
		}
		
		@SideOnly(Side.CLIENT)
		@SubscribeEvent
		public static void registerModels(ModelRegistryEvent event) {
			proxy.registerRenderers();
		}
	}
}
