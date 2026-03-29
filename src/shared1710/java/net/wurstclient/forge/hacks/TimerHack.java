/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.lang.reflect.Field;

import net.minecraft.util.Timer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class TimerHack extends Hack
{
	private static final float SPEED = 1.5F;

	public TimerHack()
	{
		super("Timer", "Speeds up the game.");
		setCategory(Category.OTHER);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		setTimerSpeed(1.0F);
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		setTimerSpeed(SPEED);
	}

	private void setTimerSpeed(float speed)
	{
		try
		{
			Field fTimer = mc.getClass().getDeclaredField(
				wurst.isObfuscated() ? "field_71428_T" : "timer");
			fTimer.setAccessible(true);

			Field fTimerSpeed = Timer.class.getDeclaredField(
				wurst.isObfuscated() ? "field_74278_d" : "timerSpeed");
			fTimerSpeed.setAccessible(true);
			fTimerSpeed.setFloat(fTimer.get(mc), speed);
		}catch(ReflectiveOperationException e)
		{
			throw new RuntimeException(e);
		}
	}
}
