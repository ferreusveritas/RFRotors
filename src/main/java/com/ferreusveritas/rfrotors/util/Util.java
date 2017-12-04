package com.ferreusveritas.rfrotors.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

/**
 * Various useful functions that are used throughout the codebase.
 */
public class Util {

	/**
	 * Checks if the player is currently wielding a Buildcraft-compatible wrench
	 * @param pPlayer Player who might be holding the wrench
	 * @param pX X coordinate of the position trying to be wrenched
	 * @param pY Y coordinate of the position trying to be wrenched
	 * @param pZ Z coordinate of the position trying to be wrenched
	 * @return {@code true} if the player can wrench and {@code false} otherwise
	 */
	public static boolean hasWrench(EntityPlayer pPlayer, BlockPos pos) {
		ItemStack tool = pPlayer.getHeldItemMainhand();
		return false;
		//TODO: Port to 1.12.2
		//return tool != null && (ModAPIManager.INSTANCE.hasAPI("BuildCraftAPI|tools") &&
		//		(tool.getItem() instanceof IToolWrench) &&
		//		((IToolWrench)tool.getItem()).canWrench(pPlayer, pX, pY, pZ));
	}

}
