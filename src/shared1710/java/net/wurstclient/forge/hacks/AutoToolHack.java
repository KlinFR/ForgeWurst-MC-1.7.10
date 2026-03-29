/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class AutoToolHack extends Hack
{
	public AutoToolHack()
	{
		super("AutoTool",
			"Automatically equips the fastest tool in your hotbar.");
		setCategory(Category.BLOCKS);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event)
	{
		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null || player.capabilities.isCreativeMode
			|| mc.theWorld == null)
			return;

		Block block = mc.theWorld.getBlock(event.getX(), event.getY(),
			event.getZ());
		equipBestTool(player, block);
	}

	private void equipBestTool(EntityClientPlayerMP player, Block block)
	{
		float bestSpeed = 1.0F;
		int bestSlot = -1;
		for(int slot = 0; slot < 9; slot++)
		{
			ItemStack stack = player.inventory.mainInventory[slot];
			if(stack == null || stack.getItem() instanceof ItemSword)
				continue;

			float speed = stack.getStrVsBlock(block);
			if(speed > bestSpeed)
			{
				bestSpeed = speed;
				bestSlot = slot;
			}
		}

		if(bestSlot >= 0)
			player.inventory.currentItem = bestSlot;
	}
}
