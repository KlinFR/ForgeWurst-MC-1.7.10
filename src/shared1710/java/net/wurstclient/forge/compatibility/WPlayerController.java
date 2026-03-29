/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

public final class WPlayerController
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private WPlayerController()
	{
	}

	public static boolean processRightClickBlock(int x, int y, int z, int side,
		Vec3 hitVec)
	{
		return mc.playerController.onPlayerRightClick(WMinecraft.getPlayer(),
			WMinecraft.getWorld(), WMinecraft.getPlayer().getCurrentEquippedItem(), x,
			y, z, side, hitVec);
	}
}
