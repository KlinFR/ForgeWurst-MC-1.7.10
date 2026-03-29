/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class KillauraHack extends Hack
{
	private int attackCooldown;

	public KillauraHack()
	{
		super("Killaura", "Automatically attacks entities around you.");
		setCategory(Category.COMBAT);
	}

	@Override
	protected void onEnable()
	{
		attackCooldown = 0;
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
		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null || mc.currentScreen != null)
			return;

		if(attackCooldown > 0)
		{
			attackCooldown--;
			return;
		}

		EntityLivingBase target = null;
		double bestDistanceSq = 16.0D;

		for(Object obj : mc.theWorld.loadedEntityList)
		{
			if(!(obj instanceof EntityLivingBase))
				continue;

			EntityLivingBase entity = (EntityLivingBase)obj;
			if(entity == player || entity.isDead || entity.getHealth() <= 0)
				continue;

			if(entity instanceof EntityPlayer && ((EntityPlayer)entity).isPlayerSleeping())
				continue;

			if(entity.isInvisible())
				continue;

			double distanceSq = player.getDistanceSqToEntity(entity);
			if(distanceSq >= bestDistanceSq || !player.canEntityBeSeen(entity))
				continue;

			bestDistanceSq = distanceSq;
			target = entity;
		}

		if(target == null)
			return;

		faceEntity(player, target);
		mc.playerController.attackEntity(player, target);
		player.swingItem();
		attackCooldown = 4;
	}

	private void faceEntity(EntityClientPlayerMP player, Entity entity)
	{
		double x = entity.posX - player.posX;
		double y = entity.boundingBox.minY + entity.height / 2.0D
			- (player.posY + player.getEyeHeight());
		double z = entity.posZ - player.posZ;
		double horizontal = Math.sqrt(x * x + z * z);
		player.rotationYaw =
			(float)(Math.atan2(z, x) * 180.0D / Math.PI) - 90.0F;
		player.rotationPitch =
			(float)(-(Math.atan2(y, horizontal) * 180.0D / Math.PI));
	}
}
