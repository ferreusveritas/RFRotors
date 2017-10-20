package com.ferreusveritas.rfrotors.recipes;


import com.ferreusveritas.rfrotors.blocks.ModBlocks;
import com.ferreusveritas.rfrotors.items.ModItems;
import com.ferreusveritas.rfrotors.lib.Constants;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Contains methods to register the recipes for all the items and blocks in the
 * mod. Uses vanilla recipes by default but if Thermal Foundation or Thermal
 * Expansion is loaded (and the functionality not disabled in the config file)
 * then the recipes will be modified to utilise those mods.
 */
public class ModRecipes {
	
	static ItemStack findThermalFoundationItem(String itemName, int meta) {
		return new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(Constants.THERMAL_FOUNDATION_MOD_ID, itemName)), 1, meta);
	}
	
	static ItemStack findThermalExpansionItem(String itemName, int meta) {
		return new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(Constants.THERMAL_EXPANSION_MOD_ID, itemName)), 1, meta);
	}
	
	/**
	 * Register Thermal Expansion and Thermal Foundation recipes if they are
	 * loaded and the functionality is enabled in the config file otherwise
	 * register the vanilla recipes.
	 */
	public static void register(IForgeRegistry<IRecipe> registry) {
		registerCommonRecipes(registry);
		registerRotors(registry);
		registerGenerator(registry);
	}

	private static ResourceLocation localResource(String name) {
		return new ResourceLocation(Constants.MODID, name);
	}
	
	/**
	 * Register recipes not dependent on the loaded mods.
	 */
	private static void registerCommonRecipes(IForgeRegistry<IRecipe> registry) {
		ItemStack aerotheumBucket = findThermalFoundationItem("bucket", 7);
		ItemStack cinnabar = findThermalFoundationItem("material", 20);

		registry.register(new ShapelessOreRecipe(localResource("dustChromel"), ModItems.dustChromel, "dustPlatinum", "dustPlatinum", "dustNickel", "dustNickel", aerotheumBucket, cinnabar));
		GameRegistry.addSmelting(ModItems.dustChromel, new ItemStack(ModItems.ingotChromel), 0);
		GameRegistry.addShapedRecipe(localResource("ingotChromel"), null, new ItemStack(ModItems.ingotChromel), "nnn", "nnn", "nnn", 'n', new ItemStack(ModItems.nuggetChromel));
		GameRegistry.addShapelessRecipe(localResource("nuggetChromel"), null, new ItemStack(ModItems.nuggetChromel, 9), new Ingredient[] { Ingredient.fromStacks(new ItemStack(ModItems.ingotChromel)) });
	}

	/**
	 * Register Thermal Foundation rotor recipes. Rotor types are
	 * <ul>
	 * 	<li>Sail Rotor</li>
	 * 	<li>Modern Rotor</li>
	 * 	<li>Wooden Water Rotor</li>
	 * 	<li>Iron Water Rotor</li>
	 * </ul>
	 */
	private static void registerRotors(IForgeRegistry<IRecipe> registry) {

		ItemStack bronzeGear = findThermalFoundationItem("material", 137);
		ItemStack shinyGear = findThermalFoundationItem("material", 133);
		ItemStack invarGear = findThermalFoundationItem("material", 136);
		
		//Sail Rotor Recipes
		ItemStack cloth = new ItemStack(Blocks.WOOL, 1, 0);
		//if(Loader.isModLoaded("ProjRed|Core")){
		//	cloth = new ItemStack(GameRegistry.findItem("ProjRed|Core", "projectred.core.part"), 1, 35);
		//}
		
		registry.register(new ShapedOreRecipe(localResource("sailRotorBlade"), ModItems.sailRotorBlade, "iii", "bbb", "www", 'i', "ingotIron", 'b', Blocks.IRON_BARS, 'w', cloth));
		GameRegistry.addShapedRecipe(localResource("rotorWindSail"), null, new ItemStack(ModBlocks.rotorBlock, 1, 0), " b ", "bgb", " b ", 'b', new ItemStack(ModItems.sailRotorBlade), 'g', bronzeGear);
		
		//Modern Rotor Recipes
		registry.register(new ShapedOreRecipe(localResource("modernRotorBlade"), ModItems.modernRotorBlade, "www", "ttt", "iii", 'w', "dyeWhite", 't', "ingotTin", 'i', ModItems.ingotChromel));
		GameRegistry.addShapedRecipe(localResource("rotorWindModern"), null, new ItemStack(ModBlocks.rotorBlock, 1, 1), " b ", " g ", "b b", 'b', new ItemStack(ModItems.modernRotorBlade), 'g', shinyGear);
		
		//Wooden Water Wheel Recipe
		GameRegistry.addShapedRecipe(localResource("rotorWaterWood"), null, new ItemStack(ModBlocks.rotorBlock, 1, 2), "bbb", "bgb", "bbb", 'b', Items.BOAT, 'g', bronzeGear);
		
		//Iron Water Wheel Recipe
		GameRegistry.addShapedRecipe(localResource("rotorWaterIron"), null, new ItemStack(ModBlocks.rotorBlock, 1, 3), "ccc", "cgc", "ccc", 'c', Items.CAULDRON, 'g', invarGear);
		
	}

	/**
	 * Register Thermal Expansion generator recipes.
	 */
	private static void registerGenerator(IForgeRegistry<IRecipe> registry) {
		ItemStack bronzeGear = findThermalFoundationItem("material", 137);		
		
		//TODO: Fix external references
		ItemStack powerCoilSilver = new ItemStack(Items.GOLD_INGOT);
		//ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
		ItemStack machineFrameBasic = new ItemStack(Blocks.PISTON);
		//ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));
		
		registry.register(new ShapedOreRecipe(localResource("generator"), new ItemStack(ModBlocks.generatorBlock, 1, 0), " x ", "gmg", " c ", 'x', "dustRedstone", 'g', bronzeGear, 'm', machineFrameBasic, 'c', powerCoilSilver));
	}

}
