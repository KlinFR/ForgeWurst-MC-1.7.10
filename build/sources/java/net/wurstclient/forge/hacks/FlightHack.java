/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class FlightHack extends Hack
{
	private static final float SPEED = 0.8F;

	public FlightHack()
	{
		super("Flight",
			"Allows you to fly.\n\n"
				+ "\u00a7c\u00a7lWARNING:\u00a7r Use NoFall to reduce fall damage.");
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
		EntityClientPlayerMP player = mc.thePlayer;
		if(player != null)
			player.capabilities.isFlying = false;

		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null)
			return;

		player.capabilities.isFlying = false;
		player.motionX = 0;
		player.motionY = 0;
		player.motionZ = 0;
		player.jumpMovementFactor = SPEED;

		if(mc.gameSettings.keyBindJump.getIsKeyPressed())
			player.motionY += SPEED;
		if(mc.gameSettings.keyBindSneak.getIsKeyPressed())
			player.motionY -= SPEED;
	}
}
