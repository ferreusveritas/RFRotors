package com.ferreusveritas.rfrotors.blocks;

import com.ferreusveritas.rfrotors.ModItems;
import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWaterRotorBlock;
import com.ferreusveritas.rfrotors.tileentities.TileEntityWindRotorBlock;
import com.ferreusveritas.rfrotors.util.Util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Rotor blocks are created when a {@link BlockGenerator} is right clicked with
 * a rotor item, and are necessary for the {@link BlockGenerator} to produce RF.
 */
public class BlockRotor extends BlockContainer {
	
	public static final PropertyEnum<BlockRotor.EnumType> TYPE = PropertyEnum.create("type", BlockRotor.EnumType.class);
	
	public static final String name = "rotor";

	public BlockRotor() {
		this(name);
	}
	
	public BlockRotor(String name) {
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockRotor.EnumType.WINDROTORSAIL));
		setSoundType(SoundType.METAL);
		setRegistryName(name);
		setUnlocalizedName(name);
		setHardness(3.5f);
		setResistance(10f);
		useNeighborBrightness = true; //Necessary for the OBJ model to render with the correct brightness 
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] {TYPE});
	}
	
	/** Convert the given metadata into a BlockState for this Block */
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(TYPE, EnumType.byMetadata(meta));
	}
	
	/** Convert the BlockState into the correct metadata value */
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(TYPE).getMetadata();
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
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
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		
		EnumType type = EnumType.byMetadata(meta);
		
		switch(type) {
			default:
			case WINDROTORSAIL: return new TileEntityWindRotorBlock().setType(type);
			case WINDROTORMODERN: return new TileEntityWindRotorBlock().setType(type);
			case WATERROTORWOOD: return new TileEntityWaterRotorBlock().setType(type);
			case WATERROTORIRON: return new TileEntityWaterRotorBlock().setType(type);
		}
	}
	
	public IRotor getRotor(IBlockAccess access, BlockPos pos){
		TileEntity entity = access.getTileEntity(pos);
		return (IRotor) ((entity instanceof IRotor) ? entity : null);
	}
	
	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
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
			
			if (!world.isRemote && !world.restoringBlockSnapshots) {// do not drop items while restoring blockstates, prevents item dupe
				spawnAsEntity(world, pos, new ItemStack(ModItems.rotorItem, 1, rotor.getType().getMetadata()));
			}
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
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess access, BlockPos pos) {
	//public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z) {
		TileEntity entity = access.getTileEntity(pos);
		if(entity instanceof TileEntityRotorBlock){
			TileEntityRotorBlock rotorEntity = (TileEntityRotorBlock) entity;
			IRotor rotor = (IRotor) entity;
			
			if(rotor != null){
				float depth = rotorEntity.getRotorDepth();
				float hubRadius = rotorEntity.getRotorHubRadius();
				EnumFacing rotorDir = rotor.getDirection();
				float minX = rotorDir.getFrontOffsetX() == 1 ? 0f : rotorDir.getFrontOffsetX() == -1 ? 1 - depth : 0.5f - hubRadius;
				float minY = 0.5f - hubRadius;
				float minZ = rotorDir.getFrontOffsetZ() == 1 ? 0f : rotorDir.getFrontOffsetZ() == -1 ? 1 - depth : 0.5f - hubRadius;
				float maxX = rotorDir.getFrontOffsetX() == 1 ? depth : rotorDir.getFrontOffsetX() == -1 ? 1f : 0.5f + hubRadius;
				float maxY = 0.5f + hubRadius;
				float maxZ = rotorDir.getFrontOffsetZ() == 1 ? depth : rotorDir.getFrontOffsetZ() == -1 ? 1f : 0.5f + hubRadius;
				return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
			}
		}
		
		return new AxisAlignedBB(pos);
	}
	
	public static enum EnumType implements IStringSerializable {
		WINDROTORSAIL(0, "windrotorsail", 3, 7 / 16f, 4 / 16f),
		WINDROTORMODERN(1, "windrotormodern", 3, 6 / 16f, 2 / 16f),
		WATERROTORWOOD(2, "waterrotorwood", 2, 1.0f, 5 / 16f),
		WATERROTORIRON(3, "waterrotoriron", 2, 1.0f, 5 / 16f);
		
		private static final BlockRotor.EnumType[] META_LOOKUP = new BlockRotor.EnumType[values().length];
		private final int meta;
		private final String name;
		private final int rotorPlacementRadius;
		private final float rotorDepth;
		private final float rotorHubRadius;
		
		private EnumType(int metaIn, String nameIn, int rotorPlacementRadius, float rotorDepth, float rotorHubRadius) {
			this.meta = metaIn;
			this.name = nameIn;
			this.rotorPlacementRadius = rotorPlacementRadius;
			this.rotorDepth = rotorDepth;
			this.rotorHubRadius = rotorHubRadius;
		}
		
		public int getMetadata() {
			return this.meta;
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
		public int getRotorPlacementRadius() {
			return this.rotorPlacementRadius;
		}
		
		public float getRotorDepth() {
			return rotorDepth;
		}
		
		public float getRotorHubRadius() {
			return rotorHubRadius;
		}
		
		public static BlockRotor.EnumType byMetadata(int meta) {
			return META_LOOKUP[MathHelper.clamp(meta, 0, META_LOOKUP.length - 1)];
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		static {
			for (BlockRotor.EnumType rotorType : values()) {
				META_LOOKUP[rotorType.getMetadata()] = rotorType;
			}
		}

	}
	
}
