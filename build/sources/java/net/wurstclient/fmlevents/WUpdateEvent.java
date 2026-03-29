/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.client.entity.EntityPlayerSP;

public final class WUpdateEvent extends Event
{
	private final EntityPlayerSP player;

	public WUpdateEvent(EntityPlayerSP player)
	{
		this.player = player;
	}

	public EntityPlayerSP getPlayer()
	{
		return player;
	}
}
