package com.ferreusveritas.rfrotors;

import com.ferreusveritas.rfrotors.items.ItemRotor;
import com.ferreusveritas.rfrotors.items.RFRItem;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Creates {@link RFRItem} instances of all the items in the mod.
 */
public final class ModItems {

	public static RFRItem ingotChromel;
	public static RFRItem dustChromel;
	public static RFRItem nuggetChromel;
	
	public static RFRItem sailRotorBlade;
	public static RFRItem modernRotorBlade;
	
	public static RFRItem rotorItem;
	
	/**
	 * Creates {@link RFRItem} instances of all the items in the mod.
	 */
	public static void preInit() {
		String metalName = "Chromel";
		ingotChromel = new RFRItem("ingot" + metalName);
		dustChromel = new RFRItem("dust" + metalName);
		nuggetChromel = new RFRItem("nugget" + metalName);
		
		sailRotorBlade = new RFRItem("sailRotorBlade");
		modernRotorBlade = new RFRItem("modernRotorBlade");
		
		rotorItem = new ItemRotor();
	}

	public static void register(IForgeRegistry<Item> registry) {
		registry.register(ingotChromel);
		registry.register(dustChromel);
		registry.register(nuggetChromel);
		
		registry.register(sailRotorBlade);
		registry.register(modernRotorBlade);
		
		registry.register(rotorItem);
		
		registerItemBlock(registry, ModBlocks.generatorBlock);
	}

	public static void registerItemBlock(IForgeRegistry<Item> registry, Block block) {
		ItemBlock itemBlock = new ItemBlock(block);
		itemBlock.setRegistryName(block.getRegistryName());
		registry.register(itemBlock);
	}
	
}
