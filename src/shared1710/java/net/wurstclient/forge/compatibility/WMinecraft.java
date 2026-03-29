/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.gui.FontRenderer;

public final class WMinecraft
{
	public static final String VERSION = "1.7.10";

	private static final Minecraft mc = Minecraft.getMinecraft();

	public static EntityClientPlayerMP getPlayer()
	{
		return mc.thePlayer;
	}

	public static WorldClient getWorld()
	{
		return mc.theWorld;
	}

	public static FontRenderer getFontRenderer()
	{
		return mc.fontRendererObj;
	}
}
