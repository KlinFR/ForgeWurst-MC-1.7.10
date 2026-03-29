/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class PlayerControllerUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private PlayerControllerUtils()
	{
	}

	public static ItemStack windowClick_PICKUP(int slot)
	{
		return windowClick(slot, 0, 0);
	}

	public static ItemStack windowClick_QUICK_MOVE(int slot)
	{
		return windowClick(slot, 0, 1);
	}

	public static ItemStack windowClick_THROW(int slot)
	{
		return windowClick(slot, 1, 4);
	}

	public static float getCurBlockDamageMP()
		throws ReflectiveOperationException
	{
		return findField("curBlockDamageMP", "field_78770_f")
			.getFloat(mc.playerController);
	}

	public static void setBlockHitDelay(int blockHitDelay)
		throws ReflectiveOperationException
	{
		findField("blockHitDelay", "field_78781_i")
			.setInt(mc.playerController, blockHitDelay);
	}

	public static void setIsHittingBlock(boolean isHittingBlock)
		throws ReflectiveOperationException
	{
		findField("isHittingBlock", "field_78778_j")
			.setBoolean(mc.playerController, isHittingBlock);
	}

	private static ItemStack windowClick(int slot, int mouseButton, int mode)
	{
		EntityPlayer player = WMinecraft.getPlayer();
		if(player == null)
			return null;

		return mc.playerController.windowClick(player.openContainer.windowId, slot,
			mouseButton, mode, player);
	}

	private static Field findField(String... names)
		throws ReflectiveOperationException
	{
		ReflectiveOperationException lastException = null;

		for(String name : names)
		{
			try
			{
				Field field = PlayerControllerMP.class.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			}catch(ReflectiveOperationException e)
			{
				lastException = e;
			}
		}

		throw lastException != null ? lastException
			: new NoSuchFieldException(names[0]);
	}
}
