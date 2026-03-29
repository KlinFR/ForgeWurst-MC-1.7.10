/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.util.Vec3;

public final class WVec3d
{
	private WVec3d()
	{
	}

	public static double getX(Vec3 vec)
	{
		return vec.xCoord;
	}

	public static double getY(Vec3 vec)
	{
		return vec.yCoord;
	}

	public static double getZ(Vec3 vec)
	{
		return vec.zCoord;
	}
}
