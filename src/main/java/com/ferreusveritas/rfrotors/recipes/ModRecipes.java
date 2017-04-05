package com.ferreusveritas.rfrotors.recipes;


import com.ferreusveritas.rfrotors.blocks.ModBlocks;
import com.ferreusveritas.rfrotors.items.ModItems;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.google.common.base.Preconditions;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

/**
 * Contains methods to register the recipes for all the items and blocks in the
 * mod. Uses vanilla recipes by default but if Thermal Foundation or Thermal
 * Expansion is loaded (and the functionality not disabled in the config file)
 * then the recipes will be modified to utilise those mods.
 */
public class ModRecipes {

    /**
     * Register Thermal Expansion and Thermal Foundation recipes if they are
     * loaded and the functionality is enabled in the config file otherwise
     * register the vanilla recipes.
     */
    public static void init() {
        registerCommonRecipes();
        registerRotors();
        registerGenerator();
    }

    /**
     * Register recipes not dependent on the loaded mods.
     */
    private static void registerCommonRecipes() {
    	ItemStack aerotheumBucket = new ItemStack(GameRegistry.findItem("ThermalFoundation", "bucket"), 1, 7);
    	ItemStack cinnabar = new ItemStack(GameRegistry.findItem("ThermalFoundation", "material"), 1, 20);
    	GameRegistry.addRecipe(new ShapelessOreRecipe(ModItems.dustChromel, "dustPlatinum", "dustPlatinum", "dustNickel", "dustNickel", aerotheumBucket, cinnabar));
    	GameRegistry.addSmelting(ModItems.dustChromel, new ItemStack(ModItems.ingotChromel), 0);
    	GameRegistry.addRecipe(new ItemStack(ModItems.ingotChromel), "nnn", "nnn", "nnn", 'n', new ItemStack(ModItems.nuggetChromel));
    	GameRegistry.addShapelessRecipe(new ItemStack(ModItems.nuggetChromel, 9), new ItemStack(ModItems.ingotChromel));
    }

    /**
     * Register Thermal Foundation rotor recipes. Rotor types are
     * <ul>
     *     <li>Sail Rotor</li>
     *     <li>Modern Rotor</li>
     *     <li>Wooden Water Rotor</li>
     *     <li>Iron Water Rotor</li>
     * </ul>
     */
    private static void registerRotors() {

    	ItemStack bronzeGear = new ItemStack(GameRegistry.findItem("ThermalFoundation", "material"), 1, 137);
    	ItemStack shinyGear = new ItemStack(GameRegistry.findItem("ThermalFoundation", "material"), 1, 133);
    	ItemStack invarGear = new ItemStack(GameRegistry.findItem("ThermalFoundation", "material"), 1, 136);
    	
    	//Sail Rotor Recipes
    	ItemStack cloth = new ItemStack(Blocks.wool, 1, 0);
    	if(Loader.isModLoaded("ProjRed|Core")){
			cloth = new ItemStack(GameRegistry.findItem("ProjRed|Core", "projectred.core.part"), 1, 35);
		}
        GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.sailRotorBlade, "iii", "bbb", "www", 'i', "ingotIron", 'b', Blocks.iron_bars, 'w', cloth));
    	GameRegistry.addRecipe(new ItemStack(ModBlocks.rotorBlock, 1, 0), " b ", "bgb", " b ", 'b', new ItemStack(ModItems.sailRotorBlade), 'g', bronzeGear);

    	//Modern Rotor Recipes
    	GameRegistry.addRecipe(new ShapedOreRecipe(ModItems.modernRotorBlade, "www", "ttt", "iii", 'w', "dyeWhite", 't', "ingotTin", 'i', ModItems.ingotChromel));
    	GameRegistry.addRecipe(new ItemStack(ModBlocks.rotorBlock, 1, 1), " b ", " g ", "b b", 'b', new ItemStack(ModItems.modernRotorBlade), 'g', shinyGear);
    	
    	//Wooden Water Wheel Recipe
    	GameRegistry.addRecipe(new ItemStack(ModBlocks.rotorBlock, 1, 2), "bbb", "bgb", "bbb", 'b', Items.boat, 'g', bronzeGear);
    	
    	//Iron Water Wheel Recipe
    	GameRegistry.addRecipe(new ItemStack(ModBlocks.rotorBlock, 1, 3), "ccc", "cgc", "ccc", 'c', Items.cauldron, 'g', invarGear);

    }

    /**
     * Register Thermal Expansion generator recipes.
     */
    private static void registerGenerator() {
    	ItemStack bronzeGear = new ItemStack(GameRegistry.findItem("ThermalFoundation", "material"), 1, 137);
        ItemStack powerCoilSilver = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "powerCoilSilver", 1));
        ItemStack machineFrameBasic = Preconditions.checkNotNull(GameRegistry.findItemStack(Constants.THERMAL_EXPANSION_MOD_ID, "frameMachineBasic", 1));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ModBlocks.generatorBlock, 1, 0), " x ", "gmg", " c ", 'x', "dustRedstone", 'g', bronzeGear, 'm', machineFrameBasic, 'c', powerCoilSilver));
    }

}
