package com.ferreusveritas.rfrotors.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ItemBlockRotor extends ItemBlock {

	public String names[] = {"sailRotor", "modernRotor", "waterRotorWood", "waterRotorIron"};
	
	public ItemBlockRotor(Block block) {
		super(block);
		setCreativeTab(block.getCreativeTabToDisplayOn());
		setHasSubtypes(true);
	}
	
	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
		return false;
	}
	
	public int getPlacementRadius(ItemStack stack){
		if(stack.getItem() instanceof ItemBlockRotor){
			switch(stack.getItemDamage()){
				case 0: return 3;
				case 1: return 3;
				case 2: return 2;
				case 3: return 2;
			}
		}
		
		return 0;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		int i = MathHelper.clamp(stack.getItemDamage(), 0, names.length - 1);
		return super.getUnlocalizedName() + "." + names[i];
	}
	

}
