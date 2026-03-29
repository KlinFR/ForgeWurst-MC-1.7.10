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

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WCameraTransformViewBobbingEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.CheckboxSetting;
import net.wurstclient.forge.utils.RenderUtils;

public final class ItemEspHack extends Hack
{
	private final CheckboxSetting names =
		new CheckboxSetting("Show item names", true);
	private int itemBox;
	private final ArrayList<EntityItem> items = new ArrayList<>();

	public ItemEspHack()
	{
		super("ItemESP", "Highlights nearby dropped items.");
		setCategory(Category.RENDER);
		addSetting(names);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);

		itemBox = GL11.glGenLists(1);
		GL11.glNewList(itemBox, GL11.GL_COMPILE);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(AxisAlignedBB.getBoundingBox(-0.175D, 0D,
			-0.175D, 0.175D, 0.35D, 0.175D));
		GL11.glEnd();
		GL11.glEndList();
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		if(itemBox != 0)
		{
			GL11.glDeleteLists(itemBox, 1);
			itemBox = 0;
		}

		items.clear();
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		items.clear();

		if(event.getPlayer() == null)
			return;

		if(event.getPlayer().worldObj == null)
			return;

		for(Object obj : event.getPlayer().worldObj.loadedEntityList)
		{
			if(!(obj instanceof EntityItem))
				continue;

			EntityItem item = (EntityItem)obj;
			if(item.isDead)
				continue;

			items.add(item);
		}
	}

	@SubscribeEvent
	public void onCameraTransformViewBobbing(
		WCameraTransformViewBobbingEvent event)
	{
		// ItemESP renders a 3D nameplate, which looks better without bobbing.
		if(names.isChecked())
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(mc.theWorld == null || mc.thePlayer == null)
			return;

		EspRenderer.begin();
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2.0F);
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);

		double partialTicks = event.partialTicks;
		for(EntityItem item : items)
		{
			if(item == null || item.isDead)
				continue;

			GL11.glPushMatrix();
			GL11.glTranslated(item.prevPosX
				+ (item.posX - item.prevPosX) * partialTicks,
				item.prevPosY + (item.posY - item.prevPosY) * partialTicks,
				item.prevPosZ + (item.posZ - item.prevPosZ) * partialTicks);

			GL11.glColor4f(1.0F, 1.0F, 0.25F, 0.5F);
			GL11.glCallList(itemBox);

			if(names.isChecked())
				renderNameplate(item, getItemName(item));

			GL11.glPopMatrix();
		}

		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		EspRenderer.end();
	}

	private String getItemName(EntityItem item)
	{
		ItemStack stack = item.getEntityItem();
		if(stack == null)
			return "Item";

		return stack.stackSize + "x " + stack.getDisplayName();
	}

	private void renderNameplate(EntityItem item, String text)
	{
		RenderManager renderManager = RenderManager.instance;
		if(renderManager == null || renderManager.livingPlayer == null)
			return;

		double distance = item.getDistanceSqToEntity(renderManager.livingPlayer);
		if(distance > 64.0D * 64.0D)
			return;

		FontRenderer fontRenderer = renderManager.getFontRenderer();
		float scale = 1.6F;
		float scaleFactor = 0.016666668F * scale;

		GL11.glPushMatrix();
		GL11.glTranslatef(0.0F, item.height + 0.5F, 0.0F);
		GL11.glNormal3f(0.0F, 1.0F, 0.0F);
		GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
		GL11.glScalef(-scaleFactor, -scaleFactor, scaleFactor);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);

		Tessellator tessellator = Tessellator.instance;
		byte offset = 0;
		if("deadmau5".equals(text))
			offset = -10;

		GL11.glDisable(GL11.GL_TEXTURE_2D);
		tessellator.startDrawingQuads();
		int halfWidth = fontRenderer.getStringWidth(text) / 2;
		tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
		tessellator.addVertex((double)(-halfWidth - 1), (double)(-1 + offset),
			0.0D);
		tessellator.addVertex((double)(-halfWidth - 1), (double)(8 + offset),
			0.0D);
		tessellator.addVertex((double)(halfWidth + 1), (double)(8 + offset),
			0.0D);
		tessellator.addVertex((double)(halfWidth + 1), (double)(-1 + offset),
			0.0D);
		tessellator.draw();
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2,
			offset, 553648127);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(true);
		fontRenderer.drawString(text, -fontRenderer.getStringWidth(text) / 2,
			offset, -1);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();
	}
}
