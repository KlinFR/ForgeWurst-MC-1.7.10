/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class RotationUtils
{
	private RotationUtils()
	{
	}

	public static Vec3 getEyesPos()
	{
		EntityPlayerSP player = WMinecraft.getPlayer();
		return Vec3.createVectorHelper(player.posX,
			player.posY + player.getEyeHeight(), player.posZ);
	}

	public static Vec3 getClientLookVec()
	{
		EntityPlayerSP player = WMinecraft.getPlayer();

		float f =
			MathHelper.cos(-player.rotationYaw * 0.017453292F - (float)Math.PI);
		float f1 =
			MathHelper.sin(-player.rotationYaw * 0.017453292F - (float)Math.PI);

		float f2 = -MathHelper.cos(-player.rotationPitch * 0.017453292F);
		float f3 = MathHelper.sin(-player.rotationPitch * 0.017453292F);

		return Vec3.createVectorHelper(f1 * f2, f3, f * f2);
	}

	public static double getAngleToLookVec(Vec3 vec)
	{
		float[] needed = getNeededRotations(vec);
		EntityPlayerSP player = WMinecraft.getPlayer();
		float diffYaw = wrap(player.rotationYaw) - needed[0];
		float diffPitch = wrap(player.rotationPitch) - needed[1];
		return Math.sqrt(diffYaw * diffYaw + diffPitch * diffPitch);
	}

	public static boolean faceVectorPacket(Vec3 vec)
	{
		float[] rotations = getNeededRotations(vec);
		EntityPlayerSP player = WMinecraft.getPlayer();
		player.rotationYaw = rotations[0];
		player.rotationPitch = rotations[1];
		return true;
	}

	public static void faceVectorForWalking(Vec3 vec)
	{
		float[] rotations = getNeededRotations(vec);
		EntityPlayerSP player = WMinecraft.getPlayer();
		player.rotationYaw = rotations[0];
		player.rotationPitch = 0;
	}

	public static EnumFacing getHorizontalFacing(EntityPlayer player)
	{
		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F
			+ 0.5D) & 3;
		switch(i)
		{
			case 0:
				return EnumFacing.SOUTH;
			case 1:
				return EnumFacing.WEST;
			case 2:
				return EnumFacing.NORTH;
			default:
				return EnumFacing.EAST;
		}
	}

	private static float[] getNeededRotations(Vec3 vec)
	{
		Vec3 eyesPos = getEyesPos();

		double diffX = vec.xCoord - eyesPos.xCoord;
		double diffY = vec.yCoord - eyesPos.yCoord;
		double diffZ = vec.zCoord - eyesPos.zCoord;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

		return new float[]{MathHelper.wrapAngleTo180_float(yaw),
			MathHelper.wrapAngleTo180_float(pitch)};
	}

	private static float wrap(float value)
	{
		return MathHelper.wrapAngleTo180_float(value);
	}
}
