/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.lang.reflect.Field;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WItem;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.ChatUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;

public final class AutoFishHack extends Hack
{
	private final SliderSetting validRange = new SliderSetting("Valid range",
		"Displayed bite radius. The 1.7.10 port uses the bobber state directly,\n"
			+ "so this is mainly useful for debug drawing.",
		1.5, 0.25, 8, 0.25, ValueDisplay.DECIMAL);
	private final CheckboxSetting debugDraw = new CheckboxSetting("Debug draw",
		"Shows the catch radius around your bobber and the last detected bite.",
		false);

	private Field ticksCatchableField;
	private int timer;
	private int lastCatchableTicks;
	private Vec3 lastCatchPos;

	public AutoFishHack()
	{
		super("AutoFish", "Automatically catches fish using your best rod.");
		setCategory(Category.OTHER);
		addSetting(validRange);
		addSetting(debugDraw);
	}

	@Override
	protected void onEnable()
	{
		timer = 0;
		lastCatchableTicks = 0;
		lastCatchPos = null;
		ticksCatchableField = findTicksCatchableField();
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityClientPlayerMP player = WMinecraft.getPlayer();
		if(player == null || mc.theWorld == null || mc.currentScreen != null)
			return;

		if(timer > 0)
		{
			timer--;
			return;
		}

		int bestRodSlot = findBestRodSlot(player);
		if(bestRodSlot == -1)
		{
			ChatUtils.error("Out of fishing rods.");
			setEnabled(false);
			return;
		}

		if(!equipRod(player, bestRodSlot))
			return;

		EntityFishHook bobber = player.fishEntity;
		if(bobber == null)
		{
			useRod(player);
			return;
		}

		int ticksCatchable = getTicksCatchable(bobber);
		if(ticksCatchable > 0 && lastCatchableTicks <= 0)
		{
			lastCatchPos = Vec3.createVectorHelper(bobber.posX, bobber.posY,
				bobber.posZ);
			useRod(player);
		}

		lastCatchableTicks = ticksCatchable;
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(!debugDraw.isChecked())
			return;

		EntityClientPlayerMP player = WMinecraft.getPlayer();
		if(player == null)
			return;

		EntityFishHook bobber = player.fishEntity;
		EspRenderer.begin();

		if(bobber != null)
		{
			double r = validRange.getValue();
			AxisAlignedBB box = AxisAlignedBB.getBoundingBox(bobber.posX - r,
				bobber.posY - 0.0625, bobber.posZ - r, bobber.posX + r,
				bobber.posY + 0.0625, bobber.posZ + r)
				.offset(-RenderManager.renderPosX, -RenderManager.renderPosY,
					-RenderManager.renderPosZ);
			EspRenderer.drawBox(box, 1.0F, 0.2F, 0.2F);
		}

		if(lastCatchPos != null)
		{
			double x = lastCatchPos.xCoord - RenderManager.renderPosX;
			double y = lastCatchPos.yCoord - RenderManager.renderPosY;
			double z = lastCatchPos.zCoord - RenderManager.renderPosZ;

			GL11.glColor4f(1.0F, 0.1F, 0.1F, 0.9F);
			GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex3d(x - 0.15, y, z - 0.15);
			GL11.glVertex3d(x + 0.15, y, z + 0.15);
			GL11.glVertex3d(x - 0.15, y, z + 0.15);
			GL11.glVertex3d(x + 0.15, y, z - 0.15);
			GL11.glEnd();
		}

		EspRenderer.end();
	}

	private int findBestRodSlot(EntityClientPlayerMP player)
	{
		int bestValue = -1;
		int bestSlot = -1;
		for(int slot = 0; slot < 36; slot++)
		{
			ItemStack stack = player.inventory.getStackInSlot(slot);
			int value = getRodValue(stack);
			if(value <= bestValue)
				continue;

			bestValue = value;
			bestSlot = slot;
		}

		return bestSlot;
	}

	private boolean equipRod(EntityClientPlayerMP player, int slot)
	{
		if(slot == player.inventory.currentItem)
			return true;

		if(slot < 9)
		{
			player.inventory.currentItem = slot;
			return true;
		}

		if(player.inventory.mainInventory[player.inventory.currentItem] == null)
		{
			PlayerControllerUtils.windowClick_QUICK_MOVE(slot);
			timer = 4;
			return false;
		}

		PlayerControllerUtils.windowClick_PICKUP(slot);
		PlayerControllerUtils.windowClick_PICKUP(36 + player.inventory.currentItem);
		PlayerControllerUtils.windowClick_PICKUP(slot);
		timer = 4;
		return false;
	}

	private int getRodValue(ItemStack stack)
	{
		if(WItem.isNullOrEmpty(stack)
			|| !(stack.getItem() instanceof ItemFishingRod))
			return -1;

		int luck = EnchantmentHelper.getEnchantmentLevel(
			Enchantment.luckOfTheSea.effectId, stack);
		int lure =
			EnchantmentHelper.getEnchantmentLevel(Enchantment.lure.effectId, stack);
		int unbreaking = EnchantmentHelper.getEnchantmentLevel(
			Enchantment.unbreaking.effectId, stack);
		return luck * 9 + lure * 9 + unbreaking * 2
			+ (stack.getMaxDurability() - stack.getCurrentDurability());
	}

	private int getTicksCatchable(EntityFishHook bobber)
	{
		try
		{
			return ticksCatchableField.getInt(bobber);
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}

	private Field findTicksCatchableField()
	{
		String[] names = wurst.isObfuscated()
			? new String[]{"field_146045_ax", "ticksCatchable"}
			: new String[]{"ticksCatchable", "field_146045_ax"};

		ReflectiveOperationException lastException = null;
		for(String name : names)
		{
			try
			{
				Field field = EntityFishHook.class.getDeclaredField(name);
				field.setAccessible(true);
				return field;
			}catch(ReflectiveOperationException e)
			{
				lastException = e;
			}
		}

		throw new RuntimeException(lastException);
	}

	private void useRod(EntityClientPlayerMP player)
	{
		ItemStack stack = player.getCurrentEquippedItem();
		if(WItem.isNullOrEmpty(stack)
			|| !(stack.getItem() instanceof ItemFishingRod))
			return;

		if(mc.playerController.sendUseItem(player, mc.theWorld, stack))
			player.swingItem();

		timer = 15;
	}
}
