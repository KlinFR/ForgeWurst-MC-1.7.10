/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class PlayerEspHack extends Hack
{
	public PlayerEspHack()
	{
		super("PlayerEsp", "Draws boxes around other players.");
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
		if(mc.thePlayer == null || mc.theWorld == null)
			return;

		EspRenderer.begin();
		for(Object obj : mc.theWorld.loadedEntityList)
		{
			if(!(obj instanceof EntityPlayer))
				continue;

			EntityPlayer player = (EntityPlayer)obj;
			if(player == mc.thePlayer || player.isDead)
				continue;

			EspRenderer.drawBox(player, 0.15F, 0.65F, 1.0F,
				event.partialTicks);
		}
		EspRenderer.end();
	}
}
