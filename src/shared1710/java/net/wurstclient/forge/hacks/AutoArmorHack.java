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

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class AutoArmorHack extends Hack
{
	private int timer;

	public AutoArmorHack()
	{
		super("AutoArmor", "Manages your armor automatically.");
		setCategory(Category.COMBAT);
	}

	@Override
	protected void onEnable()
	{
		timer = 0;
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
		if(timer > 0)
		{
			timer--;
			return;
		}

		if(mc.currentScreen instanceof GuiContainer)
			return;

		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null)
			return;

		InventoryPlayer inventory = player.inventory;
		int[] bestSlots = {-1, -1, -1, -1};
		int[] bestValues = {0, 0, 0, 0};

		for(int type = 0; type < 4; type++)
		{
			ItemStack equipped = inventory.armorInventory[type];
			if(equipped != null && equipped.getItem() instanceof ItemArmor)
				bestValues[type] = getArmorValue((ItemArmor)equipped.getItem(),
					equipped);
		}

		for(int slot = 0; slot < 36; slot++)
		{
			ItemStack stack = inventory.mainInventory[slot];
			if(stack == null || !(stack.getItem() instanceof ItemArmor))
				continue;

			ItemArmor armor = (ItemArmor)stack.getItem();
			int type = armor.armorType;
			int value = getArmorValue(armor, stack);
			if(value > bestValues[type])
			{
				bestValues[type] = value;
				bestSlots[type] = slot;
			}
		}

		ArrayList<Integer> types = new ArrayList<>();
		for(int i = 0; i < 4; i++)
			types.add(i);
		Collections.shuffle(types);

		for(int type : types)
		{
			int slot = bestSlots[type];
			if(slot == -1)
				continue;

			ItemStack oldArmor = inventory.armorInventory[type];
			if(oldArmor != null && inventory.getFirstEmptyStack() == -1)
				continue;

			if(oldArmor != null)
				mc.playerController.windowClick(player.inventoryContainer.windowId,
					8 - type, 0, 1, player);

			int windowSlot = slot < 9 ? 36 + slot : slot;
			mc.playerController.windowClick(player.inventoryContainer.windowId,
				windowSlot, 0, 1, player);
			timer = 2;
			break;
		}
	}

	private int getArmorValue(ItemArmor armor, ItemStack stack)
	{
		int protection = EnchantmentHelper.getEnchantmentLevel(
			Enchantment.protection.effectId, stack);
		return armor.damageReduceAmount * 5 + protection * 3 + armor.armorType;
	}
}
