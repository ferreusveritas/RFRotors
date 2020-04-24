package com.ferreusveritas.rfrotors;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

public class ModRecipes {
	
	public static void register(IForgeRegistry<IRecipe> registry) {
		GameRegistry.addSmelting(ModItems.dustChromel, new ItemStack(ModItems.ingotChromel), 0);
	}

}
