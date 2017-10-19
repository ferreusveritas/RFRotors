package com.ferreusveritas.rfrotors.blocks;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.items.ItemBlockRotor;
import com.ferreusveritas.rfrotors.lib.*;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.util.Lang;
import com.ferreusveritas.rfrotors.util.Util;
import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The main structural block of a windmill, handles creation of the
 * {@link TileEntityGeneratorBlock} tile entity and {@link BlockRotor} creation.
 *
 * Multiple block types are implemented using metadata and an array of relevant
 * values for each type is passed to the constructor.
 *
 * When sneak-right clicked empty handed info is printed to the chat detailing
 * current RF production rate and RF storage. When sneak-right clicked with a
 * wrench the {@link BlockGenerator} is removed from the world and dropped
 * instantly as an item, as is the attached {@link BlockRotor} (if any).
 * Right clicking with a rotor will attach the rotor to the block and create a
 * corresponding {@link BlockRotor}.
 */
public class BlockGenerator extends Block implements ITileEntityProvider {

	protected final int maximumEnergyTransfer;
	protected final int capacity;
	
	private String name;
	
	public BlockGenerator(String pName, int pCapacity) {
		super(Material.ROCK);
		setHardness(3.5f);
		setSoundType(SoundType.METAL);
		maximumEnergyTransfer = (int)(ModConfiguration.getWindGenerationBase() * ModConfiguration.getGeneratorEnergyTransferMultiplier());
		capacity = pCapacity;
		name = pName;
		setRegistryName(name);
		setUnlocalizedName(name);
		this.setCreativeTab(RFRotors.rotorsTab);
		GameRegistry.registerBlock(this, name);
	}
	
	static public int encodeDirToMetadata(EnumFacing dir){
		return (dir.offsetY != 0 ? 0 : dir.offsetX != 0 ? 4 : 8) | ( (dir.offsetX | dir.offsetY | dir.offsetZ) == 1 ? 2 : 0);
	}
	
	static public EnumFacing decodeMetadataToDir(int meta){
		return EnumFacing.getFront(((meta & 8) >> 2) | (meta & 4) | ((meta & 2) >> 1));
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityGeneratorBlock(maximumEnergyTransfer, capacity);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
	//public void onBlockPlacedBy(World world, int pX, int pY, int pZ, EntityLivingBase pEntity, ItemStack pItemStack) {
		// If the ItemBlock version has energy stored in it then give the newly
		// created tile entity that energy
		
		if(stack.hasTagCompound()) {
			TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
			entity.setEnergyStored(stack.getTagCompound().getInteger(EnergyStorage.NBT_ENERGY));
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		
		int dir = MathHelper.floor((360.0F - placer.rotationYaw) * 4.0F / 360.0F + 2.5D) & 3;
		final int dirmap[] = {10, 6, 8, 4};
		
		//TODO: Port to 1.12.2
		world.setBlockState(pos, state);
		//world.setBlockMetadataWithNotify(pX, pY, pZ, dirmap[dir], 3);
		
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
	//public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pDx, float pDy, float pDz) {
		if(!world.isRemote) {
			if(player.isSneaking()) {
				// Dismantle block if player has a wrench
				if(Util.hasWrench(player, pos)) {
					dismantle(world, pos);
					return true;
				}
				else {
					// Print energy information otherwise
					printChatInfo(world, pos, player);
					return true;
				}
			}
			else {
				// Attach a rotor if the player is holding one
				ItemStack equippedItem = player.getHeldItemMainhand();
				
				if(equippedItem != null && (equippedItem.getItem() instanceof ItemBlockRotor) ) {
					// Get the direction offset of the face the player clicked
					if(facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
						return false;
					}
					
					BlockPos dPos = pos.offset(facing);
					
					ItemBlockRotor equippedRotor = (ItemBlockRotor)equippedItem.getItem();
					int radius = equippedRotor.getPlacementRadius(equippedItem);
					
					// Check that the tile entity for this block doesn't already have a rotor
					// and that a rotor can be placed at the offset
					TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
					if(BlockRotor.canPlace(world, dPos, player, facing, radius) && !entity.hasRotor()) {
						
						// Attach the rotor to the generator
						//TODO: Port to 1.12.2
						world.setBlockState(dPos, state);
						//world.setBlockState(dPos, ModBlocks.rotorBlock, equippedItem.getItemDamage(), 3);
						entity.setRotor(facing);
						
						// Remove rotor from player's inventory
						
						if(equippedItem.getCount() > 1) {
							equippedItem.shrink(1);
						}
						else {
							player.setHeldItem(hand, null);
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if(!world.isRemote) {
			dismantle(world, pos);
		}
	}

    /**
     * Removes the attached rotor (if there is one) and drops it the ground
     * and then removes this block and drops that too, making sure to save the
     * stored energy in the newly created {@link ItemStack}
     * @param world Minecraft {@link World}
     * @param pX X coordinate of this block
     * @param pY Y coordinate of this block
     * @param pZ Z coordinate of this block
     */
	private void dismantle(World world, BlockPos pos) {
		// Something has gone very wrong if this block doesn't have a tile entity
		TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
		Preconditions.checkNotNull(entity);
		// Remove the attached rotor, if there is one
		if(entity.hasRotor()) {
			EnumFacing dir = entity.getRotorDir();
			BlockPos dPos = pos.offset(dir);
			Block candidate = world.getBlockState(dPos).getBlock();
			if(candidate instanceof BlockRotor) {
				BlockRotor rotor = (BlockRotor) candidate;
				rotor.dismantle(world, dPos);
			}
		}
	
		// Remove the actual generator and drop it
		ItemStack itemStack = new ItemStack(this, 1, 0);
		int energy = entity.getEnergyStored();
		if(energy > 0) {
			if(itemStack.getTagCompound() == null) {
				itemStack.setTagCompound(new NBTTagCompound());
			}
			itemStack.getTagCompound().setInteger(EnergyStorage.NBT_ENERGY, energy);
		}
		
		world.setBlockToAir(pos);
		dropBlockAsItem(world, pX, pY, pZ, itemStack);
	}

    /**
     * Print the amount of energy stored in the windmill in RF and the amount
     * being produced in RF/t to the chat.
     * @param pWorld Minecraft {@link World}
     * @param pX X coordinate of this block
     * @param pY Y coordinate of this block
     * @param pZ Z coordinate of this block
     * @param pPlayer Player whose chat the message should be printed to
     */
    private void printChatInfo(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer) {
        TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)pWorld.getTileEntity(pX, pY, pZ);
        String msg = String.format("%s: %d/%d RF %s: %.2f RF/t",
                Lang.localize("energy.stored"),
                entity.getEnergyStored(),
                entity.getMaxEnergyStored(),
                Lang.localize("energy.generating"),
                entity.getCurrentEnergyGeneration());
        pPlayer.addChatMessage(new ChatComponentText(msg));

    }
}
