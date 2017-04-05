package com.ferreusveritas.rfrotors.tileentities;

import com.ferreusveritas.rfrotors.blocks.ModBlocks;
import com.ferreusveritas.rfrotors.lib.Constants;
import com.ferreusveritas.rfrotors.lib.EnergyPacket;
import com.ferreusveritas.rfrotors.lib.IModelProvider;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.lib.ModConfiguration;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

/**
 * Tile entity for the {@link com.ferreusveritas.rfrotors.blocks.BlockRotor}
 * class which stores rotation and scale data for the attached
 * {@link RenderTileEntityRotorBlock} as well as the type of the rotor used
 * to create it.
 */
public class TileEntityWaterRotorBlock extends TileEntityRotorBlock implements IRotor, IModelProvider {

	private static ResourceLocation waterRotorIronTexture;
	private static ResourceLocation waterRotorWoodTexture;
	private static String waterRotorResLocation = "models/waterRotor"; 
    private static IModelCustom waterRotorModel;
	
    private boolean wheelCW = false;
    public static String publicName = "tileEntityWaterRotorBlock";

    public class FluidFlow {
        public int pattern;
        public float speed = 0.0f;
        public Fluid type = null;
        public ForgeDirection direction = null;

        // Get and set commands which use indexing consistent with that
        // of a binary number defined from left to right, ie
        // 0b00000_00010 <==> set(1, 3)
        void set(int du, int dv) {
        	pattern |= 1 << (5 * (dv + 2) + (du + 2));
        }

        boolean get(int du, int dv) {
        	return (pattern & 1 << (5 * (dv + 2) + (du + 2))) != 0;
        }
    }
   
    @Override
	public float getRotorRadius(){
    	return 2.5f;
    }
    
    @Override
	public float getRotorDepth(){
    	return 1.0f;
    }
    
    @Override
	public float getRotorHubRadius(){
    	return 0.3125f;
    }
    
    /**
     * Get the 5x5 flow pattern
     *
     * @param pXY
     * @return
     */
    private FluidFlow getFlowPatternPlane() {
        FluidFlow flow = new FluidFlow();
        // Vector sum of fluid flow gradients along the u direction.
        // Positive means flowing in direction of u.
        int gradient = 0;
        // du and dv are plane coordinates
        for(int dv = 2; dv >= -2; --dv) {
            int depth = -1; // Depth of (u,v). 0 is empty, 8 is full block
            for(int du = -2; du <= 2; ++du) {
                // Map plane coordinates to space coordinates depending on
                // plane orientation. Note that dy := dv always, and that the
                // dv loop goes from high to low. This means the search space
                // is traversed from top to bottom and negative to positive.
                int dx = du * Math.abs(rotorDir.offsetZ);
                int dy = dv;
                int dz = du * Math.abs(rotorDir.offsetX);

                Block b = worldObj.getBlock(xCoord + dx, yCoord + dy, zCoord + dz);
                if( !((dx | dy | dz) == 0) && !b.isReplaceable(worldObj, xCoord + dx, yCoord + dy, zCoord + dz)){
                	flow.pattern = 0;//Mechanism is jammed
                	flow.speed = 0.0f;
                	return flow;
                }
                
                Fluid f = FluidRegistry.lookupFluidForBlock(b);
                if(f != null) {
                    int fMeta = worldObj.getBlockMetadata(xCoord + dx, yCoord + dy, zCoord + dz);
                    // fMeta >= 8 means falling block, which we don't care about
                    if(fMeta < 8) {
                        int fDepth = 8 - (fMeta & 7);
                        // If first block, set the base depth on the row
                        if(depth < 0) {
                            depth = fDepth;
                        } else {
                            if(fDepth != depth) {
                                // depth(u-1,v) - depth(u,v)
                                gradient += depth - fDepth;
                                depth = fDepth;
                            }
                        }
                    }
                    if(flow.type != null) {
                        if(f == flow.type) {
                            flow.set(du, dv);
                        }
//                        else return null;
                    } else {
                        flow.type = f;
                        // Water flows at 3.6m/s and has viscosity of 1000
                        flow.speed = 3600.0f / f.getViscosity();
                        flow.set(du, dv);
                    }
                }
            }
        }
        // Convert gradient to a fluid flow direction
        // Positive gradient is moving in positive u direction
        if(rotorDir.offsetZ != 0) {
            // xy-plane so u is x direction, i.e. East/West
            if(gradient > 0) {
                flow.direction = ForgeDirection.EAST;
            } else if(gradient < 0) {
                flow.direction = ForgeDirection.WEST;
            } else {
                flow.direction = null;
            }
        } else {
            // yz-plane so u is z direction, i.e. South/North
            if(gradient > 0) {
                flow.direction = ForgeDirection.SOUTH;
            } else if(gradient < 0) {
                flow.direction = ForgeDirection.NORTH;
            } else {
                flow.direction = null;
            }
        }

        return flow;
    }

    
    public float calcUndershotEnergy(FluidFlow flow){
    	//Uses KE of water with low efficiency.
    	float efficiency = 0.7f;// Efficiency for undershot is 70% (see Müller)
    	float mass = flow.type.getDensity() / 1000.0f;//Fluid mass relative to the mass of cubic meter of water
    	return 0.5f * mass * (float) Math.pow(flow.speed, 2.0) * efficiency; //Simple KE = 0.5mv^2
    }
    
    public float calcOvershotEnergy(FluidFlow flow, int headHeight){
        // Uses only falling water so low energy, but high efficiency. Head is 5m. One block travels 18m in 5s
        // so flow rate is 3.6m^3/s. For a bucket capacity of V with fluid density d total power is 5*3.6*V*d*9.8/20 RF/t.
        // Assume buckets hold a liter, for balance.
    	float efficiency = 0.7f;// Efficiency for overshot is 85% (see Müller)
    	float mass = flow.type.getDensity() / 1000.0f;//Fluid mass relative to the mass of cubic meter of water
    	final float bucketSize = 0.25f;//Assume a wheelbucket size of 1/4 m^3
    	final float accelGrav = 9.8f;
    	return headHeight * mass * bucketSize * accelGrav * efficiency;//Extract KE from acceleration force due to gravity
    }
    
    
    protected void burn(){
    	worldObj.setBlock(xCoord, yCoord, zCoord, Blocks.fire);
    	worldObj.setBlock(xCoord, yCoord - 1, zCoord, Blocks.fire);
    	worldObj.setBlock(xCoord, yCoord + 1, zCoord, Blocks.fire);
    	worldObj.setBlock(xCoord + rotorDir.offsetZ, yCoord, zCoord + rotorDir.offsetX, Blocks.fire);
    	worldObj.setBlock(xCoord - rotorDir.offsetZ, yCoord, zCoord - rotorDir.offsetX, Blocks.fire);
    	
    	EntityItem burningWheel = new EntityItem(worldObj, xCoord + 0.5f, yCoord + 0.5f, zCoord + 0.5f);
    	burningWheel.setEntityItemStack(new ItemStack(ModBlocks.rotorBlock, 1, 2));
    	burningWheel.setFire(5);
    	worldObj.spawnEntityInWorld(burningWheel);
    }
    
	///////////////////////////////////////////
	//  Rotor Interface
	///////////////////////////////////////////
    
	@Override
	public EnergyPacket getEnergyPacket() {
		FluidFlow flow = getFlowPatternPlane();
		
        if(flow == null || flow.type == null){
        	return new EnergyPacket(0, 0);
        }

		if(getType() == 2 && flow.type.getTemperature() > 450) {//Ignition point of wood in kelvin
			burn();
			return new EnergyPacket(0, 0);
		}
        
        float rotorEnergyMultiplier = ModConfiguration.getRotorEnergyMultiplier(getType());
        
        // Basic idea is to XOR the flow with each of the sample flows and then
        // check then number of true bits. cardinality(f ^ sample)
        // is close to 0 if the flows agree, and large otherwise.
        // For simplicity, let 'close to 0' mean < 3.
        final int sampleFlows[] = {
                0x0000001F,// Undershot
                0x00108421,// Overshot right
                0x01084210,// Overshot left
                0x0010861F,// Overshot carry right
                0x0108421F,// Overshot carry left
                0x000004FF,// Breastshot right
                0x0000439F // Breastshot left
        };
        
        speed = 0.0f;
        
        for(int i = 0; i < sampleFlows.length; ++i) {
            int tmp = flow.pattern ^ sampleFlows[i];
            if(Integer.bitCount(tmp) < 3) {
            	float energy = 0.0f;
            	float circum = 5.0f * 3.14f;
            	float degPerMeter = 360.0f / circum;
            	float secondsPerTick = 1.0f / 20.0f;

            	speed = flow.speed * degPerMeter * secondsPerTick;
            	
            	if(i == 0){//Undershot
            		// Undershot
            		float boost = getType() == 2 ? 1.5f : 1f;//Wooden water wheels can be a bit more efficient due to their low mass 
            		speed *= ((flow.direction == ForgeDirection.NORTH || flow.direction == ForgeDirection.EAST) ? -1 : 1) * boost;
            		energy = calcUndershotEnergy(flow) * boost;
            	} else {
            		speed *= ((rotorDir == ForgeDirection.NORTH || rotorDir == ForgeDirection.EAST) ? -1 : 1);
            		speed *= ((i & 1) == 0 ? -1 : 1);//Even configurations turn CW, ODD turn CCW 
            		switch(i) {
            		case 1:// Overshot right
            		case 2:// Overshot left
            			energy = calcOvershotEnergy(flow, 5);
            			break;
            		case 3:// Overshot carry right
            		case 4:// Overshot carry left
            			// Uses falling water and a small amount of KE
            			energy = calcOvershotEnergy(flow, 5) + calcUndershotEnergy(flow) * 0.1f;
            			break;
            		case 5:// Breastshot right
            		case 6:// Breastshot left
            			//Similar to overshot carry but with a smaller head height, more KE, and slightly reduced efficiency
            			energy = calcOvershotEnergy(flow, 3) + calcUndershotEnergy(flow) * 0.35f;
            			break;
            		}
            	}

            	speed *= flow.type.getDensity() < 0 ? -1 : 1;//Lighter than air substances turn the wheel in the opposite direction
                speed = MathHelper.clamp_float(speed, -5.0f, 5.0f);
                
                return new EnergyPacket(Math.abs(energy) * rotorEnergyMultiplier * energyPacketLength, energyPacketLength);
            }
        }

        return new EnergyPacket(0, 0);
    }

	@Override
	public boolean attach(ForgeDirection dir) {
		rotorDir = dir;
		return false;
	}

	@Override
	public ForgeDirection getDirection() {
		return rotorDir;
	}


	///////////////////////////////////////////
	//  Model Interface
	///////////////////////////////////////////
	
	static public void initResources() {
		waterRotorWoodTexture = new ResourceLocation(Constants.MODID, waterRotorResLocation + "Wood.png");
		waterRotorIronTexture = new ResourceLocation(Constants.MODID, waterRotorResLocation + "Iron.png");
		waterRotorModel = AdvancedModelLoader.loadModel(new ResourceLocation(Constants.MODID, waterRotorResLocation + ".obj"));
	}
	
	@Override
	public IModelCustom getModel() {
		return waterRotorModel;
	}

    /**
     * Convert the rotor tier into the correct texture depending on the mod
     * configuration files. Takes a texture relative to the materials used
     * to make the rotor.
     * @return ResourceLocation used by {@link RenderTileEntityRotorBlock}
     */
    @Override
	public ResourceLocation getTexture() {
    	switch(getType()){
    		default:
    		case 2: return waterRotorWoodTexture;
    		case 3: return waterRotorIronTexture;
    	}
    }

	@Override
	public boolean flip() {
		if(speed < 0){
			wheelCW = true;
		} else 
		if(speed > 0) {
			wheelCW = false;
		}
		return wheelCW;
	}

	
}
