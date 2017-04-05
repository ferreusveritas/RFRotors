package com.ferreusveritas.rfrotors;

import com.ferreusveritas.rfrotors.blocks.ModBlocks;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class RotorsTab extends CreativeTabs {

	public RotorsTab() {
		super("tabRFRotors");
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(ModBlocks.generatorBlock);
	}

}
