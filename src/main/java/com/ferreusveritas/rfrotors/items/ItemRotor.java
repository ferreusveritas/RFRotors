package com.ferreusveritas.rfrotors.items;

import com.ferreusveritas.rfrotors.blocks.BlockRotor;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemRotor extends RFRItem {

	public ItemRotor() {
		super("rfrotor");
        setMaxDamage(0);
		setHasSubtypes(true);
	}
	
	public int getPlacementRadius(ItemStack stack){
		return BlockRotor.EnumType.byMetadata(stack.getItemDamage()).getRotorPlacementRadius();
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName() + "." + BlockRotor.EnumType.byMetadata(stack.getItemDamage()).getName();
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
    		for(BlockRotor.EnumType type : BlockRotor.EnumType.values()) {
    			items.add(new ItemStack(this, 1, type.getMetadata()));
    		}
        }
	}
	
}
