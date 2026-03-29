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

public final class WCanRenderInPassEvent extends Event
{
	private final Block block;
	private final int pass;
	private boolean rendered;

	public WCanRenderInPassEvent(Block block, int pass, boolean rendered)
	{
		this.block = block;
		this.pass = pass;
		this.rendered = rendered;
	}

	public Block getBlock()
	{
		return block;
	}

	public int getPass()
	{
		return pass;
	}

	public boolean isRendered()
	{
		return rendered;
	}

	public void setRendered(boolean rendered)
	{
		this.rendered = rendered;
	}
}
