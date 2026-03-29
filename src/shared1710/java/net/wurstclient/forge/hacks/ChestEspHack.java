/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class ChestEspHack extends Hack
{
	public ChestEspHack()
	{
		super("ChestEsp", "Draws boxes around chests.");
		setCategory(Category.RENDER);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(mc.theWorld == null)
			return;

		EspRenderer.begin();
		for(Object obj : mc.theWorld.loadedTileEntityList)
		{
			if(!(obj instanceof TileEntity))
				continue;

			TileEntity tile = (TileEntity)obj;
			if(tile instanceof TileEntityChest)
				EspRenderer.drawBox(AxisAlignedBB.getBoundingBox(tile.xCoord,
					tile.yCoord, tile.zCoord, tile.xCoord + 1, tile.yCoord + 1,
					tile.zCoord + 1), 1.0F, 0.6F, 0.1F);
			else if(tile instanceof TileEntityEnderChest)
				EspRenderer.drawBox(AxisAlignedBB.getBoundingBox(tile.xCoord,
					tile.yCoord, tile.zCoord, tile.xCoord + 1, tile.yCoord + 1,
					tile.zCoord + 1), 0.7F, 0.2F, 1.0F);
		}
		EspRenderer.end();
	}
}
