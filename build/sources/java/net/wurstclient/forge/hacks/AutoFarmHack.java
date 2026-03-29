/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockReed;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;

public final class AutoFarmHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);

	private final Map<String, Item> plants = new HashMap<>();
	private final ArrayList<Target> harvestTargets = new ArrayList<>();
	private final ArrayList<Target> replantTargets = new ArrayList<>();

	private int timer;
	private Target currentTarget;

	public AutoFarmHack()
	{
		super("AutoFarm",
			"Harvests and replants crops automatically.\n"
				+ "Supports wheat, carrots, potatoes, nether warts,\n"
				+ "pumpkins, melons, sugar cane and cactus.");
		setCategory(Category.BLOCKS);
		addSetting(range);
	}

	@Override
	protected void onEnable()
	{
		timer = 0;
		currentTarget = null;
		plants.clear();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		harvestTargets.clear();
		replantTargets.clear();
		currentTarget = null;
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		if(mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null)
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		scanArea();

		currentTarget = null;
		for(Target target : replantTargets)
		{
			if(!tryToReplant(target))
				continue;

			currentTarget = target;
			return;
		}

		for(Target target : harvestTargets)
		{
			if(!BlockUtils.breakBlockSimple(mc.theWorld, target.x, target.y,
				target.z))
				continue;

			currentTarget = target;
			return;
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(harvestTargets.isEmpty() && replantTargets.isEmpty())
			return;

		EspRenderer.begin();
		for(Target target : harvestTargets)
			drawTarget(target, 0.2F, 1.0F, 0.2F);

		for(Target target : replantTargets)
			drawTarget(target, 1.0F, 0.2F, 0.2F);

		if(currentTarget != null)
			drawTarget(currentTarget, 1.0F, 1.0F, 0.0F);

		EspRenderer.end();
	}

	private void scanArea()
	{
		harvestTargets.clear();
		replantTargets.clear();

		double rangeSq = range.getValue() * range.getValue();
		int blockRange = (int)Math.ceil(range.getValue());
		double eyeX = mc.thePlayer.posX;
		double eyeY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
		double eyeZ = mc.thePlayer.posZ;
		int centerX = MathHelper.floor_double(mc.thePlayer.posX);
		int centerY = MathHelper.floor_double(mc.thePlayer.posY);
		int centerZ = MathHelper.floor_double(mc.thePlayer.posZ);

		for(int x = centerX - blockRange; x <= centerX + blockRange; x++)
			for(int y = centerY - blockRange; y <= centerY + blockRange; y++)
				for(int z = centerZ - blockRange; z <= centerZ + blockRange; z++)
				{
					double dx = x + 0.5 - eyeX;
					double dy = y + 0.5 - eyeY;
					double dz = z + 0.5 - eyeZ;
					double distanceSq = dx * dx + dy * dy + dz * dz;
					if(distanceSq > rangeSq)
						continue;

					registerPlant(x, y, z);
					if(shouldHarvest(x, y, z))
						harvestTargets.add(new Target(x, y, z, distanceSq));

					if(canReplant(x, y, z))
						replantTargets.add(new Target(x, y, z, distanceSq));
				}

		Collections.sort(harvestTargets,
			Comparator.comparingDouble(t -> t.distanceSq));
		Collections.sort(replantTargets,
			Comparator.comparingDouble(t -> t.distanceSq));
	}

	private void registerPlant(int x, int y, int z)
	{
		Block block = mc.theWorld.getBlock(x, y, z);
		Item seed = null;
		if(block == Blocks.wheat)
			seed = Items.wheat_seeds;
		else if(block == Blocks.carrots)
			seed = Items.carrot;
		else if(block == Blocks.potatoes)
			seed = Items.potato;
		else if(block == Blocks.nether_wart)
			seed = Items.nether_wart;

		if(seed != null)
			plants.put(key(x, y, z), seed);
	}

	private boolean shouldHarvest(int x, int y, int z)
	{
		Block block = mc.theWorld.getBlock(x, y, z);
		int meta = mc.theWorld.getBlockMetadata(x, y, z);

		if(block == Blocks.wheat || block == Blocks.carrots
			|| block == Blocks.potatoes)
			return meta >= 7;

		if(block instanceof BlockNetherWart)
			return meta >= 3;

		if(block == Blocks.pumpkin || block == Blocks.melon_block)
			return hasMatureStemAdjacent(x, y, z);

		if(block instanceof BlockReed)
			return mc.theWorld.getBlock(x, y - 1, z) instanceof BlockReed
				&& !(mc.theWorld.getBlock(x, y - 2, z) instanceof BlockReed);

		if(block instanceof BlockCactus)
			return mc.theWorld.getBlock(x, y - 1, z) instanceof BlockCactus
				&& !(mc.theWorld.getBlock(x, y - 2, z) instanceof BlockCactus);

		return false;
	}

	private boolean hasMatureStemAdjacent(int x, int y, int z)
	{
		return isMatureStem(x + 1, y, z) || isMatureStem(x - 1, y, z)
			|| isMatureStem(x, y, z + 1) || isMatureStem(x, y, z - 1);
	}

	private boolean isMatureStem(int x, int y, int z)
	{
		Block block = mc.theWorld.getBlock(x, y, z);
		return (block == Blocks.pumpkin_stem || block == Blocks.melon_stem)
			&& mc.theWorld.getBlockMetadata(x, y, z) >= 7;
	}

	private boolean canReplant(int x, int y, int z)
	{
		if(!BlockUtils.getMaterial(mc.theWorld, x, y, z).isReplaceable())
			return false;

		Item item = plants.get(key(x, y, z));
		if(item == null)
			return false;

		Block below = mc.theWorld.getBlock(x, y - 1, z);
		if(item == Items.nether_wart)
			return below == Blocks.soul_sand;

		return below == Blocks.farmland;
	}

	private boolean tryToReplant(Target target)
	{
		Item neededItem = plants.get(key(target.x, target.y, target.z));
		if(neededItem == null)
			return false;

		if(!equipItem(neededItem))
			return false;

		return BlockUtils.placeBlockSimple(mc.theWorld, target.x, target.y,
			target.z);
	}

	private boolean equipItem(Item item)
	{
		ItemStack held = mc.thePlayer.getCurrentEquippedItem();
		if(!WItem.isNullOrEmpty(held) && held.getItem() == item)
			return true;

		for(int slot = 0; slot < 9; slot++)
		{
			ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack) || stack.getItem() != item)
				continue;

			mc.thePlayer.inventory.currentItem = slot;
			return true;
		}

		for(int slot = 9; slot < 36; slot++)
		{
			ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
			if(WItem.isNullOrEmpty(stack) || stack.getItem() != item)
				continue;

			PlayerControllerUtils.windowClick_QUICK_MOVE(slot);
			timer = 3;
			return false;
		}

		ChatUtils.error("Out of seeds for AutoFarm.");
		setEnabled(false);
		return false;
	}

	private void drawTarget(Target target, float red, float green, float blue)
	{
		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(target.x, target.y,
			target.z, target.x + 1, target.y + 1, target.z + 1)
			.offset(-RenderManager.renderPosX, -RenderManager.renderPosY,
				-RenderManager.renderPosZ);
		EspRenderer.drawBox(box, red, green, blue);
	}

	private static String key(int x, int y, int z)
	{
		return x + "," + y + "," + z;
	}

	private static final class Target
	{
		private final int x;
		private final int y;
		private final int z;
		private final double distanceSq;

		private Target(int x, int y, int z, double distanceSq)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.distanceSq = distanceSq;
		}
	}
}
