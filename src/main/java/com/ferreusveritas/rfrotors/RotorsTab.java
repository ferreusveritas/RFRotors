package com.ferreusveritas.rfrotors;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RotorsTab extends CreativeTabs {

	public RotorsTab() {
		super("tabRFRotors");
	}

	@Override
	public ItemStack getTabIconItem() {
		return new ItemStack(Item.getItemFromBlock(ModBlocks.generatorBlock));
	}

}
