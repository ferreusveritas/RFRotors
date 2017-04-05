package com.ferreusveritas.rfrotors.items;

import com.ferreusveritas.rfrotors.lib.Constants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ItemBlockRotor extends ItemBlock {

    protected IIcon[] icons;
    public String names[] = {"sailRotor", "modernRotor", "waterRotorWood", "waterRotorIron"};
    
    public ItemBlockRotor(Block block) {
    	super(block);
    	setCreativeTab(block.getCreativeTabToDisplayOn());
    	setHasSubtypes(true);
    }

    @Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata) {
    	//Handled by BlockGenerator
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

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister reg) {
		this.icons = new IIcon[names.length];
		
		for(int i = 0; i < icons.length; i++){
			icons[i] = reg.registerIcon(Constants.MODID + ":" + names[i]);
		}
	}
	
	@Override
    public String getUnlocalizedName(ItemStack stack) {
        int i = MathHelper.clamp_int(stack.getItemDamage(), 0, names.length - 1);
        return super.getUnlocalizedName() + "." + names[i];
    }
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIconFromDamage(int damage) {
        damage = MathHelper.clamp_int(damage, 0, names.length - 1);
        return this.icons[damage];
	}
	
}
