/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.fmlevents;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;

public final class WGetAmbientOcclusionLightValueEvent extends Event
{
	private final Block block;
	private float lightValue;

	public WGetAmbientOcclusionLightValueEvent(Block block, float lightValue)
	{
		this.block = block;
		this.lightValue = lightValue;
	}

	public Block getBlock()
	{
		return block;
	}

	public float getLightValue()
	{
		return lightValue;
	}

	public void setLightValue(float lightValue)
	{
		this.lightValue = lightValue;
	}
}
