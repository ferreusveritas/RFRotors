package com.ferreusveritas.rfrotors.tileentities;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.EnergyPacket;
import com.ferreusveritas.rfrotors.lib.IModelProvider;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * Tile entity for the {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}
 * class which stores rotation and scale data for the attached
 * {@link RenderTileEntityRotorBlock} as well as the type of the rotor used
 * to create it.
 */
public class TileEntityWindRotorBlock extends TileEntityRotorBlock implements IRotor, IModelProvider {

	private static ResourceLocation sailRotorTexture;
	private static ResourceLocation modernRotorTexture;
	private static String sailRotorResLocation = "models/sailRotor";
	private static String modernRotorResLocation = "models/modernRotor";
    private static IModelCustom sailRotorModel;
    private static IModelCustom modernRotorModel;
	
    public static String publicName = "tileEntityWindRotorBlock";
    private static final float degreesPerRFPerTick = ModConfiguration.getAngularVelocityPerRF();


    private long clearanceFields[];//if any bits are active then air flow is impeded
    private int flow;
    private static long fieldMask = 0x003E7F7F777F7F3EL;
    /*
     fieldMask binary description:
     00000000 = 00
     00111110 = 3E 
     01111111 = 7F
     01111111 = 7F
     01110111 = 77
     01111111 = 7F
     01111111 = 7F
     00111110 = 3E
     */
    
    public TileEntityWindRotorBlock() {
        clearanceFields = new long[8];
        initClearanceFields();
	}
    
    @Override
	public float getRotorDepth(){
    	if(getType() == 0){
    		return 0.4375f;
    	} else {
    		return 0.375f;
    	}
    }
    
    @Override
	public float getRotorHubRadius(){
    	if(getType() == 0){
    		return 0.25f;
    	} else {
    		return 0.125f;
    	}
    }
    	
    public void initClearanceFields(){
        clearanceFields[0] = 0;
        for(int i = 0; i < 7; i++){
        	clearanceFields[i + 1] = (0x0303030303030303L << i) & fieldMask;
        }
    }
    
    private float getTunnelFieldEfficiency(){
    	return flow / 4.4f;
    }
    
    @Override
    protected void updateFlow() {
    	int spot = (int) ((worldObj.getWorldTime() ^ xCoord ^ yCoord ^ zCoord) & 0x1FF);
    	long spotmask = (1L << (spot & 0x3f));
    	
    	if((fieldMask & spotmask) != 0 ){
    		int dx = (spot & 7) - 3;
    		int dy = ((spot >> 3) & 7) - 3;
    		int dz = (spot >> 6) & 7;
    		
    		boolean isAir;
    		
            if(rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.SOUTH) {
            	isAir = worldObj.isAirBlock(xCoord + dx, yCoord + dy, zCoord + dz) && worldObj.isAirBlock(xCoord + dx, yCoord + dy, zCoord - dz);
            } else {//Flip the behavior of NS to EW and vice versa
            	isAir = worldObj.isAirBlock(xCoord + dz, yCoord + dy, zCoord + dx) && worldObj.isAirBlock(xCoord - dz, yCoord + dy, zCoord + dx);
            }
    		
    		long before = clearanceFields[dz];
    		long result = before;
    		if(isAir) {
    			result &= ~spotmask;//clear bit
    		} else {
    			result |= spotmask;//set bit
    		}

   			if(result != before){//If something has changed then recalculate the flow
   				clearanceFields[dz] = result;
   		    	if(clearanceFields[0] != 0){//It is critical for Field 0 to be unobstructed for fan to actually turn
   		    		flow = 0;
   		    	} else {
   		    		flow = 44 - Long.bitCount(clearanceFields[1] | clearanceFields[2] | clearanceFields[3] | clearanceFields[4] | clearanceFields[5] | clearanceFields[6] | clearanceFields[7] );
   		    	}
   			}
    	}
    	
    }

	///////////////////////////////////////////
	//  Rotor Interface
	///////////////////////////////////////////
	    
    /**
     * Create a new energy packet from the wind.
     * Calculates the energy in the wind that is accessible to the windmill and
     * creates a new energy packet containing that energy. Takes into account
     * the height, weather, and wind tunnel length.
     * @return Energy packet containing energy from the wind.
     */
	@Override
	public EnergyPacket getEnergyPacket() {
        float windSpeed = RFRotors.windManager.getWindSpeed(worldObj, xCoord, yCoord, zCoord);
        float energy = windSpeed * ModConfiguration.getWindGenerationBase() * getTunnelFieldEfficiency() * 0.1f * ModConfiguration.getRotorEnergyMultiplier(getType());
        speed = energy * degreesPerRFPerTick;
        if(energy < 0.01) {
            return new EnergyPacket(0, 0);
        }
        else {
            return new EnergyPacket(energy * energyPacketLength, energyPacketLength);
        }
	}

	@Override
	public boolean attach(ForgeDirection dir) {
		rotorDir = dir;
    	initClearanceFields();
		return true;
	}

	@Override
	public ForgeDirection getDirection() {
		return rotorDir;
	}
	
	///////////////////////////////////////////
	//  Model Interface
	///////////////////////////////////////////
	
	static public void initResources() {
		sailRotorTexture = new ResourceLocation(Constants.MODID, sailRotorResLocation + ".png");
		modernRotorTexture = new ResourceLocation(Constants.MODID, modernRotorResLocation + ".png");
		sailRotorModel = AdvancedModelLoader.loadModel(new ResourceLocation(Constants.MODID, sailRotorResLocation + ".obj"));
		modernRotorModel = AdvancedModelLoader.loadModel(new ResourceLocation(Constants.MODID, modernRotorResLocation + ".obj"));
	}
	
	@Override
	public IModelCustom getModel() {
		switch(getType()){
			default:
			case 0: return sailRotorModel;
			case 1: return modernRotorModel;
		}
	}

    /**
     * Convert the rotor tier into the correct texture depending on the mod
     * configuration files. Takes a texture relative to the materials used
     * to make the rotor.
     * @return Array index used by {@link RenderTileEntityRotorBlock} to
     * identify the texture
     */
    @Override
	public ResourceLocation getTexture() {
    	switch(getType()){
    		default:
    		case 0: return sailRotorTexture;
    		case 1: return modernRotorTexture;
    	}
    }

	@Override
	public boolean flip() {
		return false;
	}
    
}
