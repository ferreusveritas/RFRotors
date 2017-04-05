package com.ferreusveritas.rfrotors.tileentities;

import java.util.ArrayList;

import com.ferreusveritas.rfrotors.util.RotorDamageSource;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityRotorBlock extends TileEntity {

    private static final String NBT_ROTOR_TYPE = "RFRRotorType";
    private static final String NBT_ROTOR_DIR = "RFRRotorDir";
    private static final String NBT_ROTOR_SPEED = "RFRRotorSpeed";
    protected static int energyPacketLength = 4;

    protected ForgeDirection rotorDir = ForgeDirection.UNKNOWN;
    protected float speed = 0.0f;//Speed in degrees per tick;
    protected float oldSpeed = 0.0f;
    protected float rotation = 0.0f;
    protected float lastRotation = 0.0f;//For animation interpolation
    private int type = 0;
    
    @Override
    public void updateEntity() {
    	if(worldObj.isRemote) {//Client
    		lastRotation = rotation;//For animation interpolation
    		rotation += speed;
    		if(rotation > 360f){
    			rotation -= 360.0f;
    			lastRotation -= 360.0f;
    		} else if(rotation < 0.0f){
    			rotation += 360.0f;
    			lastRotation += 360.0f;
    		}
    		
    	} else {//Server
    		updateFlow();
    		syncSpeed();
    	}
    	
    	expelEntities();
    }
    
    protected void updateFlow(){
    }
    
    /**
     * Set the type of rotor used to make the corresponding block
     * @param pType Tier of the rotor
     */
    public TileEntityRotorBlock setType(int type){
    	this.type = type;
    	return this;
    }
    
    /**
     * Get the tier of the rotor used to make the corresponding block.
     * @return Integer corresponding to the rotor tier used to make the rotor
     */
    public int getType() {
        return type;
    }
    
    /**
     * Get the rotation of the rotor about the axis normal to the face its
     * attached to.  Used by the client for rendering.
     * @return Rotation in degrees of the rotor
     */
    public float getRotation(float dt) {
        return rotation + (rotation - lastRotation) * dt;
    }
    
    public float getRotorRadius(){
    	return 3.5f;
    }
    
    public float getRotorDepth(){
   		return 1.0f;
    }
    
    public float getRotorHubRadius(){
    	return 0.5f;
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
    	return getRotorBoundingBox();
    }
    
    public AxisAlignedBB getRotorBoundingBox(){
    	float rad = getRotorRadius();
    	float depth = getRotorDepth();
    	
    	ForgeDirection genDir = rotorDir.getOpposite();

    	float xHigh = (rotorDir.offsetX * (depth - 0.5f)) + (rotorDir.offsetZ * rad);
    	float zHigh = (rotorDir.offsetZ * (depth - 0.5f)) + (rotorDir.offsetX * rad);
    	float xLow = (rotorDir.offsetX * -0.5f ) + (rotorDir.offsetZ * -rad);
    	float zLow = (rotorDir.offsetZ * -0.5f ) + (rotorDir.offsetX * -rad);
    	
    	xHigh *= xHigh > 0f ? 1f : -1f;
    	zHigh *= zHigh > 0f ? 1f : -1f;
    	xLow *= xLow < 0f ? 1f : -1f;
    	zLow *= zLow < 0f ? 1f : -1f;
    	
    	return AxisAlignedBB.getBoundingBox(
    			xCoord + 0.5f + xLow,
    			yCoord + 0.5f - rad,
    			zCoord + 0.5f + zLow,
    			xCoord + 0.5f + xHigh,
    			yCoord + 0.5f + rad,
    			zCoord + 0.5f + zHigh);
    }
    
    void expelEntities(){
    	float rad = getRotorRadius();
    	ArrayList list = (ArrayList) worldObj.getEntitiesWithinAABB(Entity.class, getRotorBoundingBox());
    	
    	for(Object object: list){
    		Entity entity = (Entity) object;
    		float avgRad = (float) (((entity.boundingBox.maxX - entity.boundingBox.minX) + (entity.boundingBox.maxY - entity.boundingBox.minY)) / 2.0f);
    		double du = (rotorDir.offsetZ != 0 ? xCoord - entity.posX : zCoord - entity.posZ) + 0.5f;
    		double dv = yCoord - entity.posY + 0.5f;
    		double dfc = Math.sqrt(du * du + dv * dv);
    		if(dfc == 0){
    			dfc = 1.0;
    		}
    		if(dfc < avgRad + rad){
    			du = du / dfc;
    			dv = dv / dfc;
    			if(rotorDir.offsetZ != 0){//Rotating on zAxis
    				int rot = rotorDir.offsetZ;
    				entity.setVelocity(-du * 0.1, -dv * 0.1, rot * 0.02);
    				entity.addVelocity(dv * speed * rot * 0.05, -du * speed * rot * 0.05, speed * 0.0);
    			} else {//Rotating on xAxis
    				int rot = -rotorDir.offsetX;
    				entity.setVelocity(rot * 0.02, -dv * 0.1, -du * 0.1);
    				entity.addVelocity(speed * 0.0, -du * speed * rot * 0.05, dv * speed * rot * 0.05);
    			}
    			if(speed > 4.0f && entity instanceof EntityLivingBase && ((worldObj.getWorldTime() % 4) == 0)){
    				entity.attackEntityFrom(new RotorDamageSource(), speed - 2f);
    			}
    		}
    	}
    	
    }
    
    /**
     * Sync the rotor speed between client and server. (Used in
     * rendering with {@link RenderTileEntityRotorBlock}.)
     */
    protected void syncSpeed() {
        // Amount of energy generated has changed so sync with server
        if(Math.abs(speed - oldSpeed) > 0.1) {
            oldSpeed = speed;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            markDirty();
        }
    }

    public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
        type = pNbt.getInteger(NBT_ROTOR_TYPE);
        speed = pNbt.getFloat(NBT_ROTOR_SPEED);
        rotorDir = ForgeDirection.getOrientation(pNbt.getInteger(NBT_ROTOR_DIR));
    }

    public void writeSyncableDataToNBT(NBTTagCompound pNbt) {
        pNbt.setInteger(NBT_ROTOR_TYPE, type);
        pNbt.setFloat(NBT_ROTOR_SPEED, speed);
        pNbt.setInteger(NBT_ROTOR_DIR, rotorDir.ordinal());
    }

    @Override
    public void readFromNBT(NBTTagCompound pNbt) {
        super.readFromNBT(pNbt);
        readSyncableDataFromNBT(pNbt);
    }

    @Override
    public void writeToNBT(NBTTagCompound pNbt) {
        super.writeToNBT(pNbt);
        writeSyncableDataToNBT(pNbt);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound syncData = new NBTTagCompound();
        writeSyncableDataToNBT(syncData);

        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, syncData);
    }

    @Override
    public void onDataPacket(NetworkManager pNet, S35PacketUpdateTileEntity pPacket) {
        readSyncableDataFromNBT(pPacket.func_148857_g());
    }

}
