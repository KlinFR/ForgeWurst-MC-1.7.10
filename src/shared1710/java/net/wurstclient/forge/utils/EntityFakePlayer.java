/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import net.wurstclient.forge.compatibility.WMinecraft;

public class EntityFakePlayer extends EntityOtherPlayerMP
{
	public EntityFakePlayer()
	{
		super(WMinecraft.getWorld(), WMinecraft.getPlayer().getGameProfile());

		EntityPlayerSP player = WMinecraft.getPlayer();
		copyLocationAndAnglesFrom(player);

		inventory.copyInventory(player.inventory);

		rotationYawHead = player.rotationYawHead;
		renderYawOffset = player.renderYawOffset;

		World world = WMinecraft.getWorld();
		world.spawnEntityInWorld(this);
	}

	public void resetPlayerPosition()
	{
		EntityPlayerSP player = WMinecraft.getPlayer();
		player.setPositionAndRotation(posX, posY, posZ, rotationYaw,
			rotationPitch);
	}

	public void despawn()
	{
		setDead();
	}
}
