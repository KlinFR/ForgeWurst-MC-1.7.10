/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.util.AxisAlignedBB;
import net.wurstclient.forge.utils.RenderUtils;

public final class MobEspHack extends Hack
{
	private int mobBox;
	private final ArrayList<EntityLivingBase> mobs = new ArrayList<>();

	public MobEspHack()
	{
		super("MobEsp", "Draws boxes around mobs and animals.");
		setCategory(Category.RENDER);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);

		mobBox = GL11.glGenLists(1);
		GL11.glNewList(mobBox, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(AxisAlignedBB.getBoundingBox(-0.5D, 0D,
			-0.5D, 0.5D, 1D, 0.5D));
		GL11.glEnd();
		GL11.glEndList();
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		if(mobBox != 0)
		{
			GL11.glDeleteLists(mobBox, 1);
			mobBox = 0;
		}

		mobs.clear();
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		mobs.clear();

		if(event.getPlayer() == null || event.getPlayer().worldObj == null)
			return;

		for(Object obj : event.getPlayer().worldObj.loadedEntityList)
		{
			if(!(obj instanceof EntityLivingBase))
				continue;

			EntityLivingBase entity = (EntityLivingBase)obj;
			if(entity.isDead || entity == event.getPlayer())
				continue;

			if(!(entity instanceof IMob) && !(entity instanceof EntityAnimal))
				continue;

			mobs.add(entity);
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(mc.thePlayer == null || mc.theWorld == null)
			return;

		EspRenderer.begin();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2.0F);
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);
		for(EntityLivingBase entity : mobs)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(entity.prevPosX
				+ (entity.posX - entity.prevPosX) * event.partialTicks,
				entity.prevPosY + (entity.posY - entity.prevPosY)
					* event.partialTicks,
				entity.prevPosZ + (entity.posZ - entity.prevPosZ)
					* event.partialTicks);
			GL11.glScaled(entity.width + 0.1F, entity.height + 0.1F,
				entity.width + 0.1F);

			if(entity instanceof IMob)
				GL11.glColor4f(1.0F, 0.25F, 0.25F, 0.5F);
			else
				GL11.glColor4f(0.25F, 1.0F, 0.25F, 0.5F);

			GL11.glCallList(mobBox);
			GL11.glPopMatrix();
		}
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		EspRenderer.end();
	}
}
