/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class BunnyHopHack extends Hack
{
	public BunnyHopHack()
	{
		super("BunnyHop", "Makes you jump automatically.");
		setCategory(Category.MOVEMENT);
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
	public void onUpdate(WUpdateEvent event)
	{
		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null || !player.onGround || player.isSneaking()
			|| player.isInsideOfMaterial(Material.water))
			return;

		if(player.moveForward != 0 || player.moveStrafing != 0)
			player.jump();
	}
}
