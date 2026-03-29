/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.block.material.Material;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;

public final class JesusHack extends Hack
{
	private final CheckboxSetting preventJumping =
		new CheckboxSetting("Prevent jumping",
			"Prevents you from jumping while walking on liquids.", false);
	private int tickTimer;

	public JesusHack()
	{
		super("Jesus", "Allows you to walk on water.");
		setCategory(Category.MOVEMENT);
		addSetting(preventJumping);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
		tickTimer = 0;
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
EntityClientPlayerMP player = mc.thePlayer;
		if(player == null || mc.theWorld == null)
			return;

		if(player.isSneaking() && GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
			return;

		if(player.isInWater())
		{
			player.motionY = 0.11D;
			player.fallDistance = 0;
			tickTimer = 0;
			return;
		}

		if(isStandingOnLiquid(player))
		{
			if(preventJumping.isChecked()
				&& GameSettings.isKeyDown(mc.gameSettings.keyBindJump))
			{
				player.motionY = 0;
				player.fallDistance = 0;
				tickTimer = 0;
				return;
			}

			// Keep the player hovering on the surface instead of sinking.
			player.motionY = tickTimer == 0 ? 0.03D : 0;
			player.onGround = true;
			player.fallDistance = 0;
			tickTimer = (tickTimer + 1) % 2;
		}else
			tickTimer = 0;
	}

	private boolean isStandingOnLiquid(EntityClientPlayerMP player)
	{
		World world = player.worldObj;
		int x = MathHelper.floor_double(player.posX);
		int y = MathHelper.floor_double(player.boundingBox.minY - 0.01D);
		int z = MathHelper.floor_double(player.posZ);
		Material material = world.getBlock(x, y, z).getMaterial();
		return material == Material.water || material == Material.lava;
	}
}
