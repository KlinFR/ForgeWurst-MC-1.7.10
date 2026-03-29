/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WUpdateEvent;

public final class WTickBridge
{
	public static final WTickBridge INSTANCE = new WTickBridge();

	private WTickBridge()
	{
	}

	@SubscribeEvent
	public void onClientTick(ClientTickEvent event)
	{
		if(event.phase != Phase.START)
			return;

		if(WMinecraft.getPlayer() == null || WMinecraft.getWorld() == null
			|| !WMinecraft.getWorld().isRemote)
			return;

		MinecraftForge.EVENT_BUS.post(new WUpdateEvent(WMinecraft.getPlayer()));
	}
}
