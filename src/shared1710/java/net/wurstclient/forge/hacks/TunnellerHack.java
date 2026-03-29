/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.KeyBindingUtils;
import net.wurstclient.forge.utils.RotationUtils;

@Hack.DontSaveState
public final class TunnellerHack extends Hack
{
	private final EnumSetting<TunnelSize> size = new EnumSetting<>(
		"Tunnel size", TunnelSize.values(), TunnelSize.SIZE_3X3);
	private final SliderSetting limit = new SliderSetting("Limit",
		"Automatically stops once the tunnel reaches the given length.\n\n"
			+ "0 = no limit",
		0, 0, 1000, 1,
		v -> v == 0 ? "disabled" : v == 1 ? "1 block" : (int)v + " blocks");
	private final CheckboxSetting torches = new CheckboxSetting("Place torches",
		"Places torches along the tunnel when possible.", false);

	private final ArrayList<Target> digTargets = new ArrayList<>();
	private final ArrayList<Target> floorTargets = new ArrayList<>();

	private int startX;
	private int startY;
	private int startZ;
	private EnumFacing direction;
	private int length;
	private int lastTorchLength;
	private Target currentTarget;

	public TunnellerHack()
	{
		super("Tunneller", "Automatically digs a tunnel.");
		setCategory(Category.BLOCKS);
		addSetting(size);
		addSetting(limit);
		addSetting(torches);
	}

	@Override
	public String getRenderName()
	{
		if(limit.getValueI() == 0)
			return getName();

		return getName() + " [" + length + "/" + limit.getValueI() + "]";
	}

	@Override
	protected void onEnable()
	{
		if(mc.thePlayer == null || mc.theWorld == null)
			return;

		startX = MathHelper.floor_double(mc.thePlayer.posX);
		startY = MathHelper.floor_double(mc.thePlayer.boundingBox.minY);
		startZ = MathHelper.floor_double(mc.thePlayer.posZ);
		direction = RotationUtils.getHorizontalFacing(mc.thePlayer);
		length = 0;
		lastTorchLength = 0;
		currentTarget = null;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		resetKeys();
		digTargets.clear();
		floorTargets.clear();
		currentTarget = null;
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		if(mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null)
			return;

		resetKeys();
		updateTargets();

		if(hasLiquidHazard())
		{
			ChatUtils.error("The tunnel intersects liquid. Tunneller stopped.");
			setEnabled(false);
			return;
		}

		if(fillFloorHoles())
			return;

		if(tryPlaceTorch())
			return;

		if(digNextBlock())
			return;

		if(reachedLimit())
		{
			ChatUtils.message("Tunnel completed.");
			setEnabled(false);
			return;
		}

		length++;
		walkForward();
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(digTargets.isEmpty() && floorTargets.isEmpty() && currentTarget == null)
			return;

		EspRenderer.begin();
		for(Target target : digTargets)
			drawTarget(target, 0.2F, 1.0F, 0.2F);
		for(Target target : floorTargets)
			drawTarget(target, 1.0F, 1.0F, 0.2F);
		if(currentTarget != null)
			drawTarget(currentTarget, 1.0F, 0.6F, 0.0F);
		EspRenderer.end();
	}

	private void updateTargets()
	{
		digTargets.clear();
		floorTargets.clear();
		currentTarget = null;

		for(int side = size.getSelected().minSide; side <= size.getSelected().maxSide;
			side++)
			for(int y = size.getSelected().maxHeight; y >= 0; y--)
			{
				int x = getTunnelX(length, side);
				int blockY = startY + y;
				int z = getTunnelZ(length, side);

				if(BlockUtils.canBeClicked(mc.theWorld, x, blockY, z))
					digTargets.add(new Target(x, blockY, z));

				int floorY = startY - 1;
				if(y == 0 && shouldFillFloor(x, floorY, z))
					floorTargets.add(new Target(x, floorY, z));
			}
	}

	private boolean hasLiquidHazard()
	{
		for(Target target : digTargets)
			if(mc.theWorld.getBlock(target.x, target.y, target.z) instanceof BlockLiquid)
				return true;

		for(Target target : floorTargets)
			if(mc.theWorld.getBlock(target.x, target.y, target.z) instanceof BlockLiquid)
				return true;

		return false;
	}

	private boolean fillFloorHoles()
	{
		if(floorTargets.isEmpty())
			return false;

		if(!equipSolidBlock())
		{
			ChatUtils.error(
				"Tunneller found a hole in the floor but has no solid blocks.");
			setEnabled(false);
			return true;
		}

		KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
		Target target = floorTargets.get(0);
		currentTarget = target;
		return BlockUtils.placeBlockSimple(mc.theWorld, target.x, target.y,
			target.z);
	}

	private boolean tryPlaceTorch()
	{
		if(!torches.isChecked())
			return false;

		if(length - lastTorchLength < size.getSelected().torchDistance)
			return false;

		int x = getTunnelX(length, 0);
		int y = startY;
		int z = getTunnelZ(length, 0);
		if(mc.theWorld.getBlock(x, y, z) != Blocks.air
			|| !Blocks.torch.canPlaceBlockAt(mc.theWorld, x, y, z))
			return false;

		if(!equipTorch())
			return false;

		KeyBindingUtils.setPressed(mc.gameSettings.keyBindSneak, true);
		currentTarget = new Target(x, y, z);
		boolean placed = BlockUtils.placeBlockSimple(mc.theWorld, x, y, z);
		if(placed && mc.theWorld.getBlock(x, y, z) instanceof BlockTorch)
			lastTorchLength = length;

		return placed;
	}

	private boolean digNextBlock()
	{
		if(digTargets.isEmpty())
			return false;

		equipBestTool(digTargets.get(0));
		currentTarget = digTargets.get(0);
		return BlockUtils.breakBlockSimple(mc.theWorld, currentTarget.x,
			currentTarget.y, currentTarget.z);
	}

	private void walkForward()
	{
		Vec3 vec = Vec3.createVectorHelper(getTunnelX(length, 0) + 0.5,
			mc.thePlayer.posY, getTunnelZ(length, 0) + 0.5);
		RotationUtils.faceVectorForWalking(vec);
		KeyBindingUtils.setPressed(mc.gameSettings.keyBindForward, true);
	}

	private boolean reachedLimit()
	{
		return limit.getValueI() > 0 && length >= limit.getValueI();
	}

	private boolean shouldFillFloor(int x, int y, int z)
	{
		Block block = mc.theWorld.getBlock(x, y, z);
		return block == Blocks.air || !block.isNormalCube()
			|| block instanceof BlockLiquid;
	}

	private boolean equipSolidBlock()
	{
		for(int slot = 0; slot < 9; slot++)
		{
			ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack) || !(stack.getItem() instanceof ItemBlock))
				continue;

			Block block = ((ItemBlock)stack.getItem()).blockInstance;
			if(!block.isNormalCube() || block instanceof BlockFalling
				|| block instanceof BlockTorch)
				continue;

			mc.thePlayer.inventory.currentItem = slot;
			return true;
		}

		return false;
	}

	private boolean equipTorch()
	{
		for(int slot = 0; slot < 9; slot++)
		{
			ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack) || !(stack.getItem() instanceof ItemBlock))
				continue;

			Block block = ((ItemBlock)stack.getItem()).blockInstance;
			if(!(block instanceof BlockTorch))
				continue;

			mc.thePlayer.inventory.currentItem = slot;
			return true;
		}

		return false;
	}

	private void equipBestTool(Target target)
	{
		Block block = mc.theWorld.getBlock(target.x, target.y, target.z);
		float bestSpeed = 1.0F;
		int bestSlot = -1;
		for(int slot = 0; slot < 9; slot++)
		{
			ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack))
				continue;

			float speed = stack.getStrVsBlock(block);
			if(speed <= bestSpeed)
				continue;

			bestSpeed = speed;
			bestSlot = slot;
		}

		if(bestSlot >= 0)
			mc.thePlayer.inventory.currentItem = bestSlot;
	}

	private void resetKeys()
	{
		KeyBinding[] bindings = {mc.gameSettings.keyBindForward,
			mc.gameSettings.keyBindBack, mc.gameSettings.keyBindLeft,
			mc.gameSettings.keyBindRight, mc.gameSettings.keyBindJump,
			mc.gameSettings.keyBindSneak};
		for(KeyBinding binding : bindings)
			KeyBindingUtils.setPressed(binding, false);
	}

	private int getTunnelX(int forward, int side)
	{
		int dx = direction.getFrontOffsetX();
		int dz = direction.getFrontOffsetZ();
		int rightX = -dz;
		return startX + dx * forward + rightX * side;
	}

	private int getTunnelZ(int forward, int side)
	{
		int dx = direction.getFrontOffsetX();
		int dz = direction.getFrontOffsetZ();
		int rightZ = dx;
		return startZ + dz * forward + rightZ * side;
	}

	private void drawTarget(Target target, float red, float green, float blue)
	{
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(target.x, target.y,
			target.z, target.x + 1, target.y + 1, target.z + 1)
			.offset(-RenderManager.renderPosX, -RenderManager.renderPosY,
				-RenderManager.renderPosZ);
		EspRenderer.drawBox(box, red, green, blue);
	}

	private static final class Target
	{
		private final int x;
		private final int y;
		private final int z;

		private Target(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}

	private enum TunnelSize
	{
		SIZE_1X2("1x2", 0, 0, 1, 13),
		SIZE_3X3("3x3", -1, 1, 2, 11);

		private final String name;
		private final int minSide;
		private final int maxSide;
		private final int maxHeight;
		private final int torchDistance;

		private TunnelSize(String name, int minSide, int maxSide, int maxHeight,
			int torchDistance)
		{
			this.name = name;
			this.minSide = minSide;
			this.maxSide = maxSide;
			this.maxHeight = maxHeight;
			this.torchDistance = torchDistance;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
