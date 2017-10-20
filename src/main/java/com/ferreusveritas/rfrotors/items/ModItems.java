package com.ferreusveritas.rfrotors.items;

import net.minecraft.item.Item;
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
	
	/**
	 * Creates {@link RFRItem} instances of all the items in the mod.
	 */
	public static void init() {
		String metalName = "Chromel";
		ingotChromel = new RFRItem("ingot" + metalName);
		dustChromel = new RFRItem("dust" + metalName);
		nuggetChromel = new RFRItem("nugget" + metalName);
		
		sailRotorBlade = new RFRItem("sailRotorBlade");
		modernRotorBlade = new RFRItem("modernRotorBlade");
	}

	public static void register(IForgeRegistry<Item> registry) {
		registry.register(ingotChromel);
		registry.register(dustChromel);
		registry.register(nuggetChromel);
		
		registry.register(sailRotorBlade);
		registry.register(modernRotorBlade);
	}

}
