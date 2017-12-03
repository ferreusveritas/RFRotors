package com.ferreusveritas.rfrotors.tileentities;

import com.ferreusveritas.rfrotors.RFRotors;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.EnergyPacket;
import com.ferreusveritas.rfrotors.lib.IModelProvider;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;

/**
 * Tile entity for the {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}
 * class which stores rotation and scale data for the attached
 * {@link RenderTileEntityRotorBlock} as well as the type of the rotor used
 * to create it.
 */
public class TileEntityWindRotorBlock extends TileEntityRotorBlock implements IRotor, IModelProvider {
	
	private static ResourceLocation sailRotorTexture;
	private static ResourceLocation modernRotorTexture;
	private static String sailRotorResLocation = "models/sailrotor";
	private static String modernRotorResLocation = "models/modernrotor";
	private static IModel sailRotorModel;
	private static IModel modernRotorModel;
	
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
	public float getRotorDepth() {
		return getType().getRotorDepth();
	}
	
	@Override
	public float getRotorHubRadius(){
		return getType().getRotorHubRadius();
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
		int spot = (int) ((world.getWorldTime() ^ pos.getX() ^ pos.getY() ^ pos.getZ()) & 0x1FF);
		long spotmask = (1L << (spot & 0x3f));
		
		if((fieldMask & spotmask) != 0 ){
			int dx = (spot & 7) - 3;
			int dy = ((spot >> 3) & 7) - 3;
			int dz = (spot >> 6) & 7;
			
			BlockPos dPos = pos.up(dy).offset(rotorDir.rotateY(), dx);
			boolean isAir = world.isAirBlock(dPos.offset(rotorDir, dz)) && world.isAirBlock(dPos.offset(rotorDir, -dz));
			
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
		float windSpeed = RFRotors.windManager.getWindSpeed(world, pos);
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
	public boolean attach(EnumFacing dir) {
		rotorDir = dir;
		initClearanceFields();
		return true;
	}
	
	@Override
	public EnumFacing getDirection() {
		if(rotorDir == null) {
			rotorDir = EnumFacing.NORTH;
		}
		return rotorDir;
	}
	
	///////////////////////////////////////////
	//  Model Interface
	///////////////////////////////////////////
	
	static public void initResources() {
		try {
			sailRotorTexture = new ResourceLocation(Constants.MODID, sailRotorResLocation + ".png");
			modernRotorTexture = new ResourceLocation(Constants.MODID, modernRotorResLocation + ".png");
			sailRotorModel = OBJLoader.INSTANCE.loadModel(new ResourceLocation(Constants.MODID, sailRotorResLocation + ".obj"));
			modernRotorModel = OBJLoader.INSTANCE.loadModel(new ResourceLocation(Constants.MODID, modernRotorResLocation + ".obj"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public IModel getModel() {
		switch(getType()){
			default:
			case WINDROTORSAIL: return sailRotorModel;
			case WINDROTORMODERN: return modernRotorModel;
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
			case WINDROTORSAIL: return sailRotorTexture;
			case WINDROTORMODERN: return modernRotorTexture;
		}
	}
	
	@Override
	public boolean flip() {
		return false;
	}
	
}
