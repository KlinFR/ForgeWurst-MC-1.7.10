/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class FullbrightHack extends Hack
{
	private float previousGamma = 0.0F;

	public FullbrightHack()
	{
		super("Fullbright", "Allows you to see in the dark.");
		setCategory(Category.RENDER);
	}

	@Override
	protected void onEnable()
	{
		previousGamma = mc.gameSettings.gammaSetting;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		mc.gameSettings.gammaSetting = previousGamma;
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		mc.gameSettings.gammaSetting = 16.0F;
	}
}
