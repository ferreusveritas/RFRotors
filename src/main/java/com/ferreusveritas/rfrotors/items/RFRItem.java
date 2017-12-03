package com.ferreusveritas.rfrotors.items;

import com.ferreusveritas.rfrotors.RFRotors;

import net.minecraft.item.Item;

/**
 * Abstract class which all items from this mod should extend. Adds them to
 * creative tabs, and sets the name and texture from the name given in the
 * constructor.
 */
public class RFRItem extends Item {

	private String name;

	public RFRItem(String pName) {
		name = pName;
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(RFRotors.rotorsTab);
	}
}
