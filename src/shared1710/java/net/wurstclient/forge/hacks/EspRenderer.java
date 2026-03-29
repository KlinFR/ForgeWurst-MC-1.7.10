/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.Entity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.AxisAlignedBB;

final class EspRenderer
{
	private EspRenderer()
	{
	}

	public static void begin()
	{
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glLineWidth(1.5F);
	}

	public static void end()
	{
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glPopMatrix();
	}

	public static void drawBox(AxisAlignedBB box, float red, float green,
		float blue)
	{
		AxisAlignedBB renderBox = box.offset(-RenderManager.renderPosX,
			-RenderManager.renderPosY, -RenderManager.renderPosZ);

		GL11.glColor4f(red, green, blue, 0.75F);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex3d(renderBox.minX, renderBox.minY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.minY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.minY, renderBox.maxZ);
		GL11.glVertex3d(renderBox.minX, renderBox.minY, renderBox.maxZ);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glVertex3d(renderBox.minX, renderBox.maxY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.maxY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.maxY, renderBox.maxZ);
		GL11.glVertex3d(renderBox.minX, renderBox.maxY, renderBox.maxZ);
		GL11.glEnd();

		GL11.glBegin(GL11.GL_LINES);
		GL11.glVertex3d(renderBox.minX, renderBox.minY, renderBox.minZ);
		GL11.glVertex3d(renderBox.minX, renderBox.maxY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.minY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.maxY, renderBox.minZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.minY, renderBox.maxZ);
		GL11.glVertex3d(renderBox.maxX, renderBox.maxY, renderBox.maxZ);
		GL11.glVertex3d(renderBox.minX, renderBox.minY, renderBox.maxZ);
		GL11.glVertex3d(renderBox.minX, renderBox.maxY, renderBox.maxZ);
		GL11.glEnd();
	}

	public static void drawBox(Entity entity, float red, float green,
		float blue, float partialTicks)
	{
		if(entity == null || entity.boundingBox == null)
			return;

		double x = entity.lastTickPosX
			+ (entity.posX - entity.lastTickPosX) * partialTicks;
		double y = entity.lastTickPosY
			+ (entity.posY - entity.lastTickPosY) * partialTicks;
		double z = entity.lastTickPosZ
			+ (entity.posZ - entity.lastTickPosZ) * partialTicks;

		AxisAlignedBB box = entity.boundingBox.offset(x - entity.posX,
			y - entity.posY, z - entity.posZ);
		drawBox(box, red, green, blue);
	}
}
