package com.ferreusveritas.rfrotors.tileentities;

import java.util.ArrayList;

import com.ferreusveritas.rfrotors.blocks.BlockRotor;
import com.ferreusveritas.rfrotors.lib.IModelProvider;
import com.ferreusveritas.rfrotors.lib.IRotor;
import com.ferreusveritas.rfrotors.util.RotorDamageSource;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityRotorBlock extends TileEntity implements ITickable, IRotor, IModelProvider {

	private static final String NBT_ROTOR_TYPE = "RFRRotorType";
	private static final String NBT_ROTOR_DIR = "RFRRotorDir";
	private static final String NBT_ROTOR_SPEED = "RFRRotorSpeed";
	protected static int energyPacketLength = 4;
	
	protected EnumFacing rotorDir = EnumFacing.NORTH;
	protected float speed = 0.0f;//Speed in degrees per tick;
	protected float oldSpeed = 0.0f;
	protected float rotation = 0.0f;
	protected float lastRotation = 0.0f;//For animation interpolation
	private BlockRotor.EnumType type;
	
	@Override
	public void update() {
		if(world.isRemote) {//Client
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
	public TileEntityRotorBlock setType(BlockRotor.EnumType type){
		this.type = type;
		return this;
	}
	
	/**
	 * Get the tier of the rotor used to make the corresponding block.
	 * @return Integer corresponding to the rotor tier used to make the rotor
	 */
	public BlockRotor.EnumType getType() {
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
		
		float xHigh = (rotorDir.getFrontOffsetX() * (depth - 0.5f)) + (rotorDir.getFrontOffsetZ() * rad);
		float zHigh = (rotorDir.getFrontOffsetZ() * (depth - 0.5f)) + (rotorDir.getFrontOffsetX() * rad);
		float xLow = (rotorDir.getFrontOffsetX() * -0.5f ) + (rotorDir.getFrontOffsetZ() * -rad);
		float zLow = (rotorDir.getFrontOffsetZ() * -0.5f ) + (rotorDir.getFrontOffsetX() * -rad);
		
		xHigh *= xHigh > 0f ? 1f : -1f;
		zHigh *= zHigh > 0f ? 1f : -1f;
		xLow *= xLow < 0f ? 1f : -1f;
		zLow *= zLow < 0f ? 1f : -1f;
		
		return new AxisAlignedBB(
				pos.getX() + 0.5f + xLow,
				pos.getY() + 0.5f - rad,
				pos.getZ() + 0.5f + zLow,
				pos.getX() + 0.5f + xHigh,
				pos.getY() + 0.5f + rad,
				pos.getZ() + 0.5f + zHigh);
	}
	
	void expelEntities(){
		float rad = getRotorRadius();
		ArrayList list = (ArrayList) world.getEntitiesWithinAABB(Entity.class, getRotorBoundingBox());
		
		for(Object object: list){
			Entity entity = (Entity) object;
			AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
			float avgRad = (float) (((boundingBox.maxX - boundingBox.minX) + (boundingBox.maxY - boundingBox.minY)) / 2.0f);
			double du = (rotorDir.getFrontOffsetZ() != 0 ? pos.getX() - entity.posX : pos.getZ() - entity.posZ) + 0.5f;
			double dv = pos.getY() - entity.posY + 0.5f;
			double dfc = Math.sqrt(du * du + dv * dv);
			if(dfc == 0){
				dfc = 1.0;
			}
			if(dfc < avgRad + rad){
				du = du / dfc;
				dv = dv / dfc;
				if(rotorDir.getFrontOffsetZ() != 0){//Rotating on zAxis
					int rot = rotorDir.getFrontOffsetZ();
					entity.setVelocity(-du * 0.1, -dv * 0.1, rot * 0.02);
					entity.addVelocity(dv * speed * rot * 0.05, -du * speed * rot * 0.05, speed * 0.0);
				} else {//Rotating on xAxis
					int rot = -rotorDir.getFrontOffsetX();
					entity.setVelocity(rot * 0.02, -dv * 0.1, -du * 0.1);
					entity.addVelocity(speed * 0.0, -du * speed * rot * 0.05, dv * speed * rot * 0.05);
				}
				if(speed > 4.0f && entity instanceof EntityLivingBase && ((world.getWorldTime() % 4) == 0)){
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
			IBlockState blockState = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, blockState, blockState, 3);
			markDirty();
		}
	}
	
	public void readSyncableDataFromNBT(NBTTagCompound pNbt) {
		type = BlockRotor.EnumType.byMetadata(pNbt.getInteger(NBT_ROTOR_TYPE));
		speed = pNbt.getFloat(NBT_ROTOR_SPEED);
		rotorDir = EnumFacing.getFront(pNbt.getInteger(NBT_ROTOR_DIR));
	}
	
	public NBTTagCompound writeSyncableDataToNBT(NBTTagCompound pNbt) {
		pNbt.setInteger(NBT_ROTOR_TYPE, type.getMetadata());
		pNbt.setFloat(NBT_ROTOR_SPEED, speed);
		pNbt.setInteger(NBT_ROTOR_DIR, rotorDir.ordinal());
		return pNbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound pNbt) {
		super.readFromNBT(pNbt);
		readSyncableDataFromNBT(pNbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		writeSyncableDataToNBT(nbt);
		return nbt;
	}
	
	@Override
	public NBTTagCompound getUpdateTag() {
		return writeSyncableDataToNBT(super.getUpdateTag());
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, writeToNBT(new NBTTagCompound()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		readSyncableDataFromNBT(pkt.getNbtCompound());
	}

}
