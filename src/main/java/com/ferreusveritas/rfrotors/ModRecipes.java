package com.ferreusveritas.rfrotors;


import com.ferreusveritas.rfrotors.lib.Constants;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreIngredient;
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
		Ingredient aerotheumBucket = Ingredient.fromStacks(findThermalFoundationItem("bucket", 7));
		Ingredient cinnabar = Ingredient.fromStacks(findThermalFoundationItem("material", 20));
		Ingredient dustPlatinum = new OreIngredient("dustPlatinum");
		Ingredient dustNickel = new OreIngredient("dustNickel");
		
		GameRegistry.addShapelessRecipe(
				localResource("dustChromel"), //Name 
				null, //Group
				new ItemStack(ModItems.dustChromel), //Output 
				new Ingredient[] { dustPlatinum, dustPlatinum, dustNickel, dustNickel, aerotheumBucket, cinnabar }
			);
				
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
	
		//Recipe for Wind Sail Rotor Blade
		GameRegistry.addShapedRecipe(
			localResource("sailRotorBlade"), //Name
			null, //Group
			new ItemStack(ModItems.sailRotorBlade), //Output
			"iii",
			"bbb",
			"www",
			'i', "ingotIron",
			'b', Blocks.IRON_BARS, 
			'w', cloth
		);
		
		//Recipe for Wind Sail Rotor
		GameRegistry.addShapedRecipe(
			localResource("rotorWindSail"), //Name
			null, //Group
			new ItemStack(ModBlocks.rotorBlock, 1, 0), //Output
			" b ",
			"bgb",
			" b ",
			'b', new ItemStack(ModItems.sailRotorBlade),
			'g', bronzeGear
		);
		
		//Recipe for Modern Wind Rotor Blade
		GameRegistry.addShapedRecipe(
			localResource("modernRotorBlade"),
			null,
			new ItemStack(ModItems.modernRotorBlade),
			"www",
			"ttt",
			"iii",
			'w', "dyeWhite",
			't', "ingotTin",
			'i', ModItems.ingotChromel
		);

		//Recipe for Modern Wind Rotor
		GameRegistry.addShapedRecipe(
			localResource("rotorWindModern"),//Name
			null,//Group
			new ItemStack(ModBlocks.rotorBlock, 1, 1),//Output 
			" b ",
			" g ",
			"b b",
			'b', new ItemStack(ModItems.modernRotorBlade),
			'g', shinyGear
		);
		
		//Wooden Water Wheel Recipe
		GameRegistry.addShapedRecipe(
			localResource("rotorWaterWood"),//Name
			null,//Group
			new ItemStack(ModBlocks.rotorBlock, 1, 2),//Output
			"bbb",
			"bgb",
			"bbb",
			'b', Items.BOAT,
			'g', bronzeGear
		);
		
		//Iron Water Wheel Recipe
		GameRegistry.addShapedRecipe(
			localResource("rotorWaterIron"),
			null,
			new ItemStack(ModBlocks.rotorBlock, 1, 3),
			"ccc",
			"cgc",
			"ccc",
			'c', Items.CAULDRON,
			'g', invarGear
		);
		
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
		
		GameRegistry.addShapedRecipe(
			localResource("generator"), //Name
			null, //Group
			new ItemStack(ModBlocks.generatorBlock, 1, 0), //Output
			" x ",
			"gmg",
			" c ",
			'x', "dustRedstone",
			'g', bronzeGear,
			'm', machineFrameBasic,
			'c', powerCoilSilver
		);

	}

}
