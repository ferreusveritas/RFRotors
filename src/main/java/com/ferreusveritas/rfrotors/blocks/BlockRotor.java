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

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * Rotor blocks are created when a {@link BlockGenerator} is right clicked with
 * a rotor item, and are necessary for the {@link BlockGenerator} to produce RF.
 */
public class BlockRotor extends BlockContainer {
	
	public static final String name = "rotor";

	public BlockRotor() {
		this(name);
	}
	
	public BlockRotor(String name) {
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setRegistryName(name);
		setUnlocalizedName(name);
		setHardness(3.5f);
		setResistance(10f);
		setCreativeTab(RFRotors.rotorsTab);
		GameRegistry.registerBlock(this, ItemBlockRotor.class, "rotor");
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
	//public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fx, float fy, float fz) {
	
		if(player.isSneaking() && Util.hasWrench(player, pos)){
			dismantle(world, pos);
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
	
	public IRotor getRotor(IBlockAccess access, BlockPos pos){
		TileEntity entity = access.getTileEntity(pos);
		return (IRotor) ((entity instanceof IRotor) ? entity : null);
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
	//public void onBlockHarvested(World world, int x, int y, int z, int side, EntityPlayer player) {
		// Tell the parent generator block that it no longer has a rotor
		IRotor rotor = getRotor(world, pos);
		if(rotor != null) {
			EnumFacing rotorDir = rotor.getDirection().getOpposite();
			BlockPos parentPos = pos.offset(rotorDir);
			TileEntityGeneratorBlock generatorEntity = (TileEntityGeneratorBlock)world.getTileEntity(parentPos);
			if(generatorEntity != null && generatorEntity instanceof TileEntityGeneratorBlock){
				generatorEntity.detach();
			}
		}
		// Dismantle the rotor
		dismantle(world, pos);
	}

	/**
	 * Remove the block from the world and drop the corresponding rotor as an
	 * item. Does not notify the parent {@link TurbineBlock} of any changes.
	 * @param pWorld Minecraft {@link World}
	 * @param x X coordinate of the block
	 * @param y Y coordinate of the block
	 * @param z Z coordinate of the block
	 */
	public void dismantle(World world, BlockPos pos) {
		IRotor rotor = getRotor(world, pos);
		if(rotor != null){
			world.setBlockToAir(pos);
			//TODO: Port to 1.12.2
			//dropBlockAsItem(world, pos, new ItemStack(this, 1, rotor.getType()));
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
	public static boolean canPlace(World world, BlockPos pos, EntityPlayer pPlayer, EnumFacing pDir, int radius ) {	
		for(BlockPos iPos : BlockPos.getAllInBox(pos.offset(pDir.rotateY(), radius).offset(EnumFacing.UP, radius), pos.offset(pDir.getOpposite().rotateY(), radius).offset(EnumFacing.DOWN, radius))) {
			Block block = world.getBlockState(iPos).getBlock();
			if(!block.isReplaceable(world, iPos)) {
				return false;
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
				EnumFacing rotorDir = rotor.getDirection();
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
