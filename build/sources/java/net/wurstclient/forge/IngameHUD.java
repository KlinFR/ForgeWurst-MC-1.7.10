/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.ArrayList;
import java.util.Comparator;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.clickgui.ClickGuiScreen;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class IngameHUD
{
	private final Minecraft mc = Minecraft.getMinecraft();
	private final HackList hackList;

	public IngameHUD(HackList hackList)
	{
		this.hackList = hackList;
	}

	@SubscribeEvent
	public void onRenderGUI(RenderGameOverlayEvent.Post event)
	{
		if(event.type != ElementType.ALL || mc.gameSettings.showDebugInfo)
			return;

		ClickGui clickGui = ForgeWurst.getForgeWurst().getGui();
		boolean blend = GL11.glGetBoolean(GL11.GL_BLEND);
		clickGui.updateColors();

		int textColor = 0xffffff;
		Hack rainbowUiHack = hackList.get("RainbowUI");
		if(rainbowUiHack == null)
			rainbowUiHack = hackList.get("RainbowUi");
		if(rainbowUiHack != null && rainbowUiHack.isEnabled())
		{
			float[] acColor = clickGui.getAcColor();
			textColor = (int)(acColor[0] * 256) << 16
				| (int)(acColor[1] * 256) << 8 | (int)(acColor[2] * 256);
		}

		GL11.glPushMatrix();
		GL11.glScaled(1.33333333, 1.33333333, 1);
		WMinecraft.getFontRenderer().drawStringWithShadow(
			"ForgeWurst v" + ForgeWurst.VERSION, 3, 3, textColor);
		GL11.glPopMatrix();

		int y = 19;
		ArrayList<Hack> hacks = new ArrayList<>();
		hacks.addAll(hackList.getValues());
		hacks.sort(Comparator.comparing(Hack::getName));

		for(Hack hack : hacks)
		{
			if(!hack.isEnabled())
				continue;

			WMinecraft.getFontRenderer().drawStringWithShadow(
				hack.getRenderName(), 2, y, textColor);
			y += 9;
		}

		if(!(mc.currentScreen instanceof ClickGuiScreen))
			clickGui.renderPinnedWindows(event.partialTicks);

		if(blend)
			GL11.glEnable(GL11.GL_BLEND);
		else
			GL11.glDisable(GL11.GL_BLEND);
	}
}
