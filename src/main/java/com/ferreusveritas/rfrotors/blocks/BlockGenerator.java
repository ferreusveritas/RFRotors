package com.ferreusveritas.rfrotors.blocks;

import com.ferreusveritas.rfrotors.ModBlocks;
import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.items.ItemRotor;
import com.ferreusveritas.rfrotors.lib.EnergyStorage;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.util.Lang;
import com.google.common.base.Preconditions;

import cofh.core.util.RayTracer;
import cofh.core.util.helpers.WrenchHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

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
public class BlockGenerator extends BlockDirectional implements ITileEntityProvider {

	protected final int maximumEnergyTransfer;
	protected final int capacity;
	
	private String name;
	
	public BlockGenerator(String pName, int pCapacity) {
		super(Material.ROCK);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		setHardness(3.5f);
		setSoundType(SoundType.METAL);
		maximumEnergyTransfer = (int)(ModConfiguration.getWindGenerationBase() * ModConfiguration.getGeneratorEnergyTransferMultiplier());
		capacity = pCapacity;
		name = pName;
		setRegistryName(name);
		setUnlocalizedName(name);
		setCreativeTab(RFRotors.rotorsTab);
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}
	
	public static EnumFacing getFacing(int meta) {
		int i = meta & 7;
		return i > 5 ? null : EnumFacing.getFront(i);
	}
	
	/** Convert the given metadata into a BlockState for this Block */
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, getFacing(meta & 7));
	}
	
	/** Convert the BlockState into the correct metadata value */
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityGeneratorBlock(maximumEnergyTransfer, capacity);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	/** Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the IBlockstate */
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}
	
	/** Called by ItemBlocks after a block is set in the world, to allow post-place logic */
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		// If the ItemBlock has energy stored in it then give the newly created tile entity that energy
		
		if(stack.hasTagCompound()) {
			TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
			entity.setEnergyStored(stack.getTagCompound().getInteger(EnergyStorage.NBT_ENERGY));
		}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote) {
			if(player.isSneaking()) {
				RayTraceResult traceResult = RayTracer.retrace(player);
								
				if (WrenchHelper.isHoldingUsableWrench(player, traceResult)) {
					// Dismantle block if player has a wrench
					dismantle(world, pos);
				}
				else {
					// Print energy information otherwise
					if(hand == EnumHand.MAIN_HAND) {
						printChatInfo(world, pos, player);
					}
				}
				
				return true;
			}
			else {
				// Attach a rotor if the player is holding one
				ItemStack equippedItem = player.getHeldItemMainhand();
				
				if(equippedItem != null && (equippedItem.getItem() instanceof ItemRotor) ) {
					// Get the direction offset of the face the player clicked
					if(facing == EnumFacing.DOWN || facing == EnumFacing.UP) {
						return false;
					}
					
					BlockPos dPos = pos.offset(facing);
					
					ItemRotor equippedRotor = (ItemRotor)equippedItem.getItem();
					int radius = equippedRotor.getPlacementRadius(equippedItem);
					
					// Check that the tile entity for this block doesn't already have a rotor
					// and that a rotor can be placed at the offset
					TileEntityGeneratorBlock generatorEntity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
					if(BlockRotor.canPlace(world, dPos, player, facing, radius) && !generatorEntity.hasRotor()) {
						
						// Attach the rotor to the generator
						world.setBlockState(dPos, ModBlocks.rotorBlock.getDefaultState().withProperty(BlockRotor.TYPE, BlockRotor.EnumType.byMetadata(equippedItem.getItemDamage())), 3);
						generatorEntity.setRotor(facing);
						
						// Remove rotor from player's inventory
						if(!player.isCreative()) {
							equippedItem.shrink(1);
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
		
		if (!world.isRemote && !world.restoringBlockSnapshots) {// do not drop items while restoring blockstates, prevents item dupe
			spawnAsEntity(world, pos, itemStack);
		}
	}
	
	/**
	 * Print the amount of energy stored in the windmill in RF and the amount
	 * being produced in RF/t to the chat.
	 * @param world Minecraft {@link World}
	 * @param pX X coordinate of this block
	 * @param pY Y coordinate of this block
	 * @param pZ Z coordinate of this block
	 * @param player Player whose chat the message should be printed to
	 */
	private void printChatInfo(World world, BlockPos pos, EntityPlayer player) {
		TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pos);
		String msg = String.format("%s: %d/%d RF %s: %.2f RF/t",
				Lang.localize("energy.stored"),
				entity.getEnergyStored(),
				entity.getMaxEnergyStored(),
				Lang.localize("energy.generating"),
				entity.getCurrentEnergyGeneration());
		
		player.sendMessage(new TextComponentString(msg));
	
	}
}
