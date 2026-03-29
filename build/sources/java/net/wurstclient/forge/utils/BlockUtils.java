/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayerController;

public final class BlockUtils
{
	private static final Minecraft mc = Minecraft.getMinecraft();

	private BlockUtils()
	{
	}

	public static Block getBlock(World world, int x, int y, int z)
	{
		return world.getBlock(x, y, z);
	}

	public static int getMetadata(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z);
	}

	public static int getId(World world, int x, int y, int z)
	{
		return Block.getIdFromBlock(getBlock(world, x, y, z));
	}

	public static String getName(Block block)
	{
		Object name = Block.blockRegistry.getNameForObject(block);
		return name == null ? null : name.toString();
	}

	public static Material getMaterial(World world, int x, int y, int z)
	{
		return getBlock(world, x, y, z).getMaterial();
	}

	public static AxisAlignedBB getBoundingBox(World world, int x, int y, int z)
	{
		return getBlock(world, x, y, z).getCollisionBoundingBoxFromPool(world,
			x, y, z);
	}

	public static boolean canBeClicked(World world, int x, int y, int z)
	{
		return getBlock(world, x, y, z) != Blocks.air;
	}

	public static float getHardness(World world, int x, int y, int z)
	{
		return getBlock(world, x, y, z).getBlockHardness(world, x, y, z);
	}

	public static boolean placeBlockSimple(World world, int x, int y, int z)
	{
		if(world == null || WMinecraft.getPlayer() == null)
			return false;

		if(!canBeClicked(world, x, y, z))
			return false;

		EnumFacing side = getBestPlaceSide(world, x, y, z);
		if(side == null)
			return false;

		Vec3 hitVec = getHitVec(x, y, z, side);
		RotationUtils.faceVectorPacket(hitVec);

		if(getRightClickDelayTimer() > 0)
			return false;

		return WPlayerController.processRightClickBlock(x + side.getFrontOffsetX(),
			y + side.getFrontOffsetY(), z + side.getFrontOffsetZ(), side.ordinal(),
			hitVec);
	}

	public static boolean breakBlockSimple(World world, int x, int y, int z)
	{
		if(world == null || WMinecraft.getPlayer() == null)
			return false;

		if(!canBeClicked(world, x, y, z))
			return false;

		EnumFacing side = getBestBreakSide(world, x, y, z);
		if(side == null)
			side = EnumFacing.DOWN;

		Vec3 hitVec = getHitVec(x, y, z, side);
		RotationUtils.faceVectorPacket(hitVec);
		mc.playerController.clickBlock(x, y, z, side.ordinal());
		WMinecraft.getPlayer().swingItem();
		return true;
	}

	public static void breakBlocksPacketSpam(World world, Iterable<int[]> blocks)
	{
		EntityClientPlayerMP player = WMinecraft.getPlayer();
		if(world == null || player == null)
			return;

		for(int[] pos : blocks)
		{
			if(pos == null || pos.length < 3)
				continue;

			int x = pos[0];
			int y = pos[1];
			int z = pos[2];
			EnumFacing side = getBestBreakSide(world, x, y, z);
			if(side == null)
				side = EnumFacing.DOWN;

			player.sendQueue.addToSendQueue(
				new C07PacketPlayerDigging(0, x, y, z, side.ordinal()));
			player.sendQueue.addToSendQueue(
				new C07PacketPlayerDigging(2, x, y, z, side.ordinal()));
		}
	}

	private static EnumFacing getBestPlaceSide(World world, int x, int y, int z)
	{
		Vec3 eyesPos = RotationUtils.getEyesPos();
		Vec3 posVec = Vec3.createVectorHelper(x + 0.5, y + 0.5, z + 0.5);
		double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);

		EnumFacing[] sides = EnumFacing.values();
		Vec3[] hitVecs = new Vec3[sides.length];
		for(int i = 0; i < sides.length; i++)
		{
			EnumFacing side = sides[i];
			hitVecs[i] = posVec.addVector(side.getFrontOffsetX() * 0.5D,
				side.getFrontOffsetY() * 0.5D, side.getFrontOffsetZ() * 0.5D);
		}

		for(int i = 0; i < sides.length; i++)
		{
			EnumFacing side = sides[i];
			if(!canBeClicked(world, x + side.getFrontOffsetX(),
				y + side.getFrontOffsetY(), z + side.getFrontOffsetZ()))
				continue;

			if(world.rayTraceBlocks(eyesPos, hitVecs[i], false, true, false) != null)
				continue;

			return side;
		}

		for(int i = 0; i < sides.length; i++)
		{
			EnumFacing side = sides[i];
			if(!canBeClicked(world, x + side.getFrontOffsetX(),
				y + side.getFrontOffsetY(), z + side.getFrontOffsetZ()))
				continue;

			if(distanceSqPosVec > eyesPos.squareDistanceTo(hitVecs[i]))
				continue;

			return side;
		}

		return null;
	}

	private static EnumFacing getBestBreakSide(World world, int x, int y, int z)
	{
		Vec3 eyesPos = RotationUtils.getEyesPos();
		AxisAlignedBB box = getBoundingBox(world, x, y, z);
		if(box == null)
			box = AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);

		Vec3 center = Vec3.createVectorHelper((box.minX + box.maxX) / 2.0D,
			(box.minY + box.maxY) / 2.0D, (box.minZ + box.maxZ) / 2.0D);
		double distanceSqToCenter = eyesPos.squareDistanceTo(center);

		EnumFacing[] sides = EnumFacing.values();
		Vec3[] hitVecs = new Vec3[sides.length];
		for(int i = 0; i < sides.length; i++)
		{
			EnumFacing side = sides[i];
			hitVecs[i] = center.addVector(side.getFrontOffsetX() * 0.5D,
				side.getFrontOffsetY() * 0.5D, side.getFrontOffsetZ() * 0.5D);
		}

		for(int i = 0; i < sides.length; i++)
		{
			if(world.rayTraceBlocks(eyesPos, hitVecs[i], false, true, false) == null)
				return sides[i];
		}

		for(int i = 0; i < sides.length; i++)
		{
			if(eyesPos.squareDistanceTo(hitVecs[i]) >= distanceSqToCenter)
				continue;

			return sides[i];
		}

		return null;
	}

	private static Vec3 getHitVec(int x, int y, int z, EnumFacing side)
	{
		return Vec3.createVectorHelper(x + 0.5D + side.getFrontOffsetX() * 0.5D,
			y + 0.5D + side.getFrontOffsetY() * 0.5D,
			z + 0.5D + side.getFrontOffsetZ() * 0.5D);
	}

	private static int getRightClickDelayTimer()
	{
		try
		{
			Field field = mc.getClass().getDeclaredField(
				ForgeWurst.getForgeWurst().isObfuscated() ? "field_71467_ac"
					: "rightClickDelayTimer");
			field.setAccessible(true);
			return field.getInt(mc);
		}catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
}
