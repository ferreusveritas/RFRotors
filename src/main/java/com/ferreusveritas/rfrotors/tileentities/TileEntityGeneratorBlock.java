package com.ferreusveritas.rfrotors.tileentities;

import com.ferreusveritas.rfrotors.blocks.BlockGenerator;
import com.ferreusveritas.rfrotors.lib.EnergyPacket;
import com.ferreusveritas.rfrotors.lib.EnergyStorage;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Tile entity for the {@link com.ferreusveritas.rfwindmill.blocks.GeneratorBlock}
 * class, handles the energy generation, storage, and transfer. Energy produced
 * is dependent on height of the windmill, the rotor material, the free space
 * around the windmill, and the strength of the weather..
 */
public final class TileEntityGeneratorBlock extends TileEntity implements IEnergyProvider {

    private EnergyStorage storage;
    private static final int tunnelRange = 10;

    private static final String NBT_ROTOR_DIR = "RFRRotorDir";
    private ForgeDirection rotorDir;
    private float currentEnergyGeneration;

    private EnergyPacket energyPacket = new EnergyPacket();

    public static final String publicName = "tileEntityGeneratorBlock";
    private static final String name = "tileEntityGeneratorBlock";

    public TileEntityGeneratorBlock() {
        this(0, 0);
    }

    public TileEntityGeneratorBlock(int pMaximumEnergyTransfer, int pCapacity) {
        storage = new EnergyStorage(pCapacity, pMaximumEnergyTransfer);
        rotorDir = ForgeDirection.NORTH;
    }

    public String getName() {
        return name;
    }
    
    /**
     * Update the parent entity, generate RF and transfer as much as possible to
     * connected storage cells if energy is currently stored.
     */
    @Override
    public void updateEntity() {
        super.updateEntity();
        if(!worldObj.isRemote) {
            // Energy left in the packet so utilize it
            if(energyPacket.getLifetime() > 0) {
                extractFromEnergyPacket(energyPacket);
            } else {// No energy left so attempt to generate a packet from the rotor
            	IRotor rotor = getRotor();
            	if(rotor != null){
            		energyPacket = rotor.getEnergyPacket();
            		extractFromEnergyPacket(energyPacket);
            	}
            }

            if(storage.getEnergyStored() > 0) {
                transferEnergy();
            }
        }
    }

    private IRotor getRotor(){
    	ForgeDirection dir = getRotorDir();
    	TileEntity entity = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
   		return (entity instanceof IRotor) ? (IRotor)entity : null; 
    }

    /**
     * Read the non-syncable data from {@code pNbt}.
     * @param pNbt NBT to read from
     */
    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        rotorDir = ForgeDirection.getOrientation(pNbt.getInteger(NBT_ROTOR_DIR));
        storage.readFromNBT(pNbt);
    }

    /**
     * Write the non-syncable data to {@code pNbt}.
     * @param pNbt NBT to write to
     */
    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        pNbt.setInteger(NBT_ROTOR_DIR, rotorDir.ordinal());
        storage.writeToNBT(pNbt);
    }

    /**
     * Calculate the energy that can be extracted from the energy packet
     * limited by the efficiency of the system. Takes into account the efficiency
     * of the turbine and of the rotor. Does not modify the packet.
     * @param pEnergyPacket Energy packet to calculate from
     * @return Extractable energy in {@code pEnergyPacket} in RF/t
     */
    private float getExtractableEnergyFromPacket(EnergyPacket pEnergyPacket) {
        return pEnergyPacket.getEnergyPerTick() * ModConfiguration.getGeneratorEfficiency();
    }

    /**
     * Takes an energy packet and extracts energy from it. Modifies the lifetime
     * of the packet accordingly.
     * @param pEnergyPacket Packet to extract from
     */
    private void extractFromEnergyPacket(EnergyPacket pEnergyPacket){
        currentEnergyGeneration = getExtractableEnergyFromPacket(pEnergyPacket);
        pEnergyPacket.deplete();
        storage.modifyEnergyStored(currentEnergyGeneration);
    }

    /**
     * The current rate of energy production in RF/t
     * @return The current rate of energy production, 0 if no rotor attached
     */
    public float getCurrentEnergyGeneration() {
        return currentEnergyGeneration;
    }

    /**
     * Transfer energy to any blocks demanding energy that are connected to
     * this one.
     */
    private void transferEnergy() {
        for(ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity tile = getWorldObj().getTileEntity(
                    xCoord + direction.offsetX,
                    yCoord + direction.offsetY,
                    zCoord + direction.offsetZ);
            if(tile instanceof IEnergyReceiver) {
                IEnergyReceiver receiver = (IEnergyReceiver)tile;
                extractEnergy(direction.getOpposite(), receiver.receiveEnergy(direction.getOpposite(), storage.getExtract(), false), false);
            }
        }
    }

    @Override
    public int extractEnergy(ForgeDirection pFrom, int pMaxExtract, boolean pSimulate) {
        if(canConnectEnergy(pFrom)) {
            return storage.extractEnergy(pMaxExtract, pSimulate);
        }
        else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored(ForgeDirection pFrom) {
        return storage.getEnergyStored();
    }

    public int getEnergyStored() {
        return getEnergyStored(ForgeDirection.NORTH);
    }

    public void setEnergyStored(int pEnergy) {
        storage.setEnergyStored(pEnergy);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection pFrom) {
        return storage.getMaxEnergyStored();
    }

    public int getMaxEnergyStored() {
        return getMaxEnergyStored(ForgeDirection.NORTH);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection pFrom) {
        return true;
    }

    public boolean hasRotor(){
    	return getRotor() != null;
    }
    
    /**
     * Set the tier of the rotor connected to the corresponding
     * {@link com.ferreusveritas.rfrotors.blocks.BlockGenerator}.
     * @param fDir Direction the rotor is facing, i.e. the normal to the face
     *             the rotor is being placed on
     */
    public void setRotor(ForgeDirection fDir) {
        rotorDir = fDir;
        
        if(hasRotor()){
            IRotor rotor = getRotor();
            rotor.attach(rotorDir);
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, BlockGenerator.encodeDirToMetadata(rotorDir), 3);
        }
    }

    public void detach(){

    }
    
    public ForgeDirection getRotorDir() {
        return rotorDir;
    }
}
