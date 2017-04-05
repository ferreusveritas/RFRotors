package com.ferreusveritas.rfrotors.blocks;

import java.util.List;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.items.ItemBlockRotor;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;
import com.ferreusveritas.rfrotors.util.Util;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Rotor blocks are created when a {@link BlockGenerator} is right clicked with
 * a rotor item, and are necessary for the {@link BlockGenerator} to produce RF.
 */
public class BlockRotor extends BlockContainer {
	
    public BlockRotor() {
        super(Material.iron);
        setStepSound(Block.soundTypeMetal);
        this.setBlockName(Constants.MODID + "_" + "rotor");
        this.setHardness(3.5f);
        this.setResistance(10f);
        this.setCreativeTab(RFRotors.rotorsTab);
        GameRegistry.registerBlock(this, ItemBlockRotor.class, "rotor");
    }
    
	@Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return Blocks.iron_bars.getIcon(side, 0);
    }
	
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return -1;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {

    	if(player.isSneaking() && Util.hasWrench(player, x, y, z)){
    		dismantle(world, x, y, z);
    		return true;
    	}
    	
    	return false;
    }
    
    @Override
    public TileEntity createNewTileEntity(World pWorld, int pMeta) {
   		switch(pMeta){
   			default:
   			case 0: return new TileEntityWindRotorBlock().setType(pMeta);
   			case 1: return new TileEntityWindRotorBlock().setType(pMeta);
   			case 2: return new TileEntityWaterRotorBlock().setType(pMeta);
   			case 3: return new TileEntityWaterRotorBlock().setType(pMeta);
   		}
    }

    //For NEI and pickBlock(...)
    @Override
    public int damageDropped(int meta) {
    	return meta;
    }
    
    @Override
    public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
    	for(int i = 0; i < 4; i++){
    		list.add(new ItemStack(item, 1, i));
    	}
    }
    
    public IRotor getRotor(IBlockAccess access, int x, int y, int z){
        TileEntity entity = access.getTileEntity(x, y, z);
        return (IRotor) ((entity instanceof IRotor) ? entity : null);
    }
    
    @Override
    public void onBlockHarvested(World world, int x, int y, int z, int side, EntityPlayer player) {
        // Tell the parent generator block that it no longer has a rotor
    	IRotor rotor = getRotor(world, x, y, z);
        if(rotor != null) {
            ForgeDirection rotorDir = rotor.getDirection().getOpposite();
            int parentX = x + rotorDir.offsetX;
            int parentY = y + rotorDir.offsetY;
            int parentZ = z + rotorDir.offsetZ;
            TileEntityGeneratorBlock generatorEntity = (TileEntityGeneratorBlock)world.getTileEntity(parentX, parentY, parentZ);
            if(generatorEntity != null && generatorEntity instanceof TileEntityGeneratorBlock){
            	generatorEntity.detach();
            }
        }
        // Dismantle the rotor
        dismantle(world, x, y, z);
    }

    /**
     * Remove the block from the world and drop the corresponding rotor as an
     * item. Does not notify the parent {@link TurbineBlock} of any changes.
     * @param pWorld Minecraft {@link World}
     * @param x X coordinate of the block
     * @param y Y coordinate of the block
     * @param z Z coordinate of the block
     */
    public void dismantle(World world, int x, int y, int z) {
    	IRotor rotor = getRotor(world, x, y, z);
        if(rotor != null){
        	world.setBlockToAir(x, y, z);
        	dropBlockAsItem(world, x, y, z, new ItemStack(this, 1, rotor.getType()));
        }
    }

    /**
     * Checks the area around the specified position for a 3x3x1 plane of free
     * blocks in the plane normal to the specified direction, which corresponds
     * to the bounding box of the rotor when its rendered.
     * @param world Minecraft {@link World}
     * @param pX X coordinate where the block is trying to be placed
     * @param pY Y coordinate where the block is trying to be placed
     * @param pZ Z coordinate where the block is trying to be placed
     * @param pPlayer Player trying to place the block
     * @param pDir Direction the rotor should be facing in
     * @return {@code true} if the rotor can be placed, and {@code false}
     * otherwise
     */
    public static boolean canPlace(World world, int pX, int pY, int pZ, EntityPlayer pPlayer, ForgeDirection pDir, int radius ) {
    	
    	int zlimit = Math.abs(pDir.offsetX) * radius;
    	int xlimit = Math.abs(pDir.offsetZ) * radius;

		for(int dy = -radius; dy <= radius; dy++){
			for(int dz = -zlimit; dz <= zlimit; dz++){
    			for(int dx = -xlimit; dx <= xlimit; dx++){
    				Block block = world.getBlock(pX + dx, pY + dy, pZ + dz);
    				if(!block.isReplaceable(world, pX + dx, pY + dy, pZ + dz)){
   	                	return false;
    				}
    			}
    		}
    	}
    	
    	return true;
    }

    
    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
    	TileEntity entity = access.getTileEntity(x, y, z);
    	if(entity instanceof TileEntityRotorBlock){
    		TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock) entity;
    		IRotor rotor = (IRotor) entity;
    	    	
    		if(rotor != null){
    			float depth = rotorEntity.getRotorDepth();
    			float hubRadius = rotorEntity.getRotorHubRadius();
    			ForgeDirection rotorDir = rotor.getDirection();
    			float minX = rotorDir.offsetX == 1 ? 0f : rotorDir.offsetX == -1 ? 1 - depth : 0.5f - hubRadius;
    			float minY = 0.5f - hubRadius;
    			float minZ = rotorDir.offsetZ == 1 ? 0f : rotorDir.offsetZ == -1 ? 1 - depth : 0.5f - hubRadius;
    			float maxX = rotorDir.offsetX == 1 ? depth : rotorDir.offsetX == -1 ? 1f : 0.5f + hubRadius;
    			float maxY = 0.5f + hubRadius;
    			float maxZ = rotorDir.offsetZ == 1 ? depth : rotorDir.offsetZ == -1 ? 1f : 0.5f + hubRadius;
    			this.setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
    		}
    	}
    }
    
}
