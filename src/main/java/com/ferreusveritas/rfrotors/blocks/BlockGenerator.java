package com.ferreusveritas.rfrotors.blocks;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.items.ItemBlockRotor;
import com.ferreusveritas.rfrotors.lib.*;
import com.ferreusveritas.rfrotors.tileentities.TileEntityGeneratorBlock;
import com.ferreusveritas.rfrotors.util.Lang;
import com.ferreusveritas.rfrotors.util.Util;
import com.google.common.base.Preconditions;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

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
    private IIcon frontIcon;
    private IIcon backIcon;
    private IIcon sideIcon;

    public BlockGenerator(String pName, int pCapacity) {
        super(Material.rock);
        setHardness(3.5f);
        setStepSound(Block.soundTypeMetal);
        maximumEnergyTransfer = (int)(ModConfiguration.getWindGenerationBase() * ModConfiguration.getGeneratorEnergyTransferMultiplier());
        capacity = pCapacity;
        name = pName;
        this.setBlockName(Constants.MODID + "_" + name);
        this.setCreativeTab(RFRotors.rotorsTab);
        GameRegistry.registerBlock(this, name);
    }

    @Override
	public int getRenderType() {
        return 31;//Rotated Pillar
    }
    
    @Override
    public void registerBlockIcons(IIconRegister pIconRegister) {
        frontIcon = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Front");
        backIcon = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Back");
        sideIcon = pIconRegister.registerIcon(Constants.MODID + ":" + name + "Side");
    }
    
    static public int encodeDirToMetadata(ForgeDirection dir){
    	return (dir.offsetY != 0 ? 0 : dir.offsetX != 0 ? 4 : 8) | ( (dir.offsetX | dir.offsetY | dir.offsetZ) == 1 ? 2 : 0);
    }
    
    static public ForgeDirection decodeMetadataToDir(int meta){
    	return ForgeDirection.getOrientation(((meta & 8) >> 2) | (meta & 4) | ((meta & 2) >> 1));
    }
    
    @Override
	@SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
    	ForgeDirection sideDir = ForgeDirection.getOrientation(side);

    	int meta = access.getBlockMetadata(x, y, z);
    	ForgeDirection genDir = decodeMetadataToDir(meta);
    	
    	if(sideDir == genDir){
    		return frontIcon;
    	}
    	else if(sideDir == genDir.getOpposite()){
    		return backIcon;
    	}
    	else {
    		return sideIcon;
    	}
    }
    
    /**
     * Gets the block's texture. Args: side, meta
     */
    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
    	switch(side){
    		default: return sideIcon;
    		case 0: return backIcon;
    		case 1: return frontIcon;
    	}
    }
    
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityGeneratorBlock(maximumEnergyTransfer, capacity);
    }

    @Override
    public boolean hasTileEntity(int pMetadata) {
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int pX, int pY, int pZ, EntityLivingBase pEntity, ItemStack pItemStack) {
        // If the ItemBlock version has energy stored in it then give the newly
        // created tile entity that energy
        if(pItemStack.stackTagCompound != null) {
            TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)world.getTileEntity(pX, pY, pZ);
            entity.setEnergyStored(pItemStack.stackTagCompound.getInteger(EnergyStorage.NBT_ENERGY));
        }
        super.onBlockPlacedBy(world, pX, pY, pZ, pEntity, pItemStack);

        int dir = MathHelper.floor_double((360.0F - pEntity.rotationYaw) * 4.0F / 360.0F + 2.5D) & 3;
        final int dirmap[] = {10, 6, 8, 4};
        
        world.setBlockMetadataWithNotify(pX, pY, pZ, dirmap[dir], 3);
        
    }

    @Override
    public boolean onBlockActivated(World pWorld, int pX, int pY, int pZ, EntityPlayer pPlayer, int pSide, float pDx, float pDy, float pDz) {
        if(!pWorld.isRemote) {
            if(pPlayer.isSneaking()) {
                // Dismantle block if player has a wrench
                if(Util.hasWrench(pPlayer, pX, pY, pZ)) {
                    dismantle(pWorld, pX, pY, pZ);
                    return true;
                }
                else {
                    // Print energy information otherwise
                    printChatInfo(pWorld, pX, pY, pZ, pPlayer);
                    return true;
                }
            }
            else {
                // Attach a rotor if the player is holding one
                ItemStack equippedItem = pPlayer.getCurrentEquippedItem();
                                
                if(equippedItem != null && (equippedItem.getItem() instanceof ItemBlockRotor) ) {
                    // Get the direction offset of the face the player clicked
                    ForgeDirection fDirection = ForgeDirection.getOrientation(pSide);
                    if(fDirection == ForgeDirection.DOWN || fDirection == ForgeDirection.UP) {
                        return false;
                    }
                    int dx = pX + fDirection.offsetX;
                    int dy = pY + fDirection.offsetY;
                    int dz = pZ + fDirection.offsetZ;
                    
                    ItemBlockRotor equippedRotor = (ItemBlockRotor)equippedItem.getItem();
                    int radius = equippedRotor.getPlacementRadius(equippedItem);
                    
                    // Check that the tile entity for this block doesn't already have a rotor
                    // and that a rotor can be placed at the offset
                    TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)pWorld.getTileEntity(pX, pY, pZ);
                    if(BlockRotor.canPlace(pWorld, dx, dy, dz, pPlayer, fDirection, radius) && !entity.hasRotor()) {
                    	
                    	// Attach the rotor to the generator
                        pWorld.setBlock(dx, dy, dz, ModBlocks.rotorBlock, equippedItem.getItemDamage(), 3);
                        entity.setRotor(fDirection);

                        // Remove rotor from player's inventory
                        if(equippedItem.stackSize > 1) {
                            equippedItem.stackSize -= 1;
                        }
                        else {
                            pPlayer.destroyCurrentEquippedItem();
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onBlockHarvested(World pWorld, int pX, int pY, int pZ, int pSide, EntityPlayer pPlayer) {
        if(!pWorld.isRemote) {
            dismantle(pWorld, pX, pY, pZ);
        }
    }

    /**
     * Removes the attached rotor (if there is one) and drops it the ground
     * and then removes this block and drops that too, making sure to save the
     * stored energy in the newly created {@link ItemStack}
     * @param pWorld Minecraft {@link World}
     * @param pX X coordinate of this block
     * @param pY Y coordinate of this block
     * @param pZ Z coordinate of this block
     */
    private void dismantle(World pWorld, int pX, int pY, int pZ) {
        // Something has gone very wrong if this block doesn't have a tile entity
        TileEntityGeneratorBlock entity = (TileEntityGeneratorBlock)pWorld.getTileEntity(pX, pY, pZ);
        Preconditions.checkNotNull(entity);
        // Remove the attached rotor, if there is one
        if(entity.hasRotor()) {
            ForgeDirection dir = entity.getRotorDir();
            BlockRotor rotor = (BlockRotor)pWorld.getBlock(pX + dir.offsetX, pY + dir.offsetY, pZ + dir.offsetZ);
            rotor.dismantle(pWorld, pX + dir.offsetX, pY + dir.offsetY, pZ + dir.offsetZ);
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
        
        pWorld.setBlockToAir(pX, pY, pZ);
        dropBlockAsItem(pWorld, pX, pY, pZ, itemStack);
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
