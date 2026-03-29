/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.lang.reflect.Field;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class FastBreakHack extends Hack
{
	private Field hitDelayField;
	private Field damageField;

	public FastBreakHack()
	{
		super("FastBreak", "Allows you to break blocks faster.");
		setCategory(Category.BLOCKS);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		try
		{
			resolveFields();
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		try
		{
			applyFastBreak();
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	public void onPlayerDamageBlock(WPlayerDamageBlockEvent event)
	{
		try
		{
			applyFastBreak();
			if(mc.thePlayer == null || mc.thePlayer.sendQueue == null)
				return;

			if(damageField.getFloat(mc.playerController) <= 0.7F)
				return;

			damageField.setFloat(mc.playerController, 1.0F);
			mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(2,
				event.getX(), event.getY(), event.getZ(), event.getSide()));
		}catch(ReflectiveOperationException e)
		{
			setEnabled(false);
			throw new RuntimeException(e);
		}
	}

	private void resolveFields() throws ReflectiveOperationException
	{
		hitDelayField = PlayerControllerMP.class.getDeclaredField(
			wurst.isObfuscated() ? "field_78781_i" : "blockHitDelay");
		damageField = PlayerControllerMP.class.getDeclaredField(
			wurst.isObfuscated() ? "field_78770_f" : "curBlockDamageMP");
		hitDelayField.setAccessible(true);
		damageField.setAccessible(true);
	}

	private void applyFastBreak() throws ReflectiveOperationException
	{
		if(mc.playerController == null)
			return;
		if(hitDelayField == null || damageField == null)
			resolveFields();

		hitDelayField.setInt(mc.playerController, 0);
		float value = damageField.getFloat(mc.playerController);
		if(value > 0.7F)
			damageField.setFloat(mc.playerController, 1.0F);
	}
}
