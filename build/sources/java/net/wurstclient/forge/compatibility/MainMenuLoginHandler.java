/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.wurstclient.forge.gui.GuiAltLogin;

public final class MainMenuLoginHandler
{
	private static final int ALT_LOGIN_ID = -14372659;

	@SubscribeEvent
	public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event)
	{
		if(!(event.gui instanceof GuiMainMenu))
			return;

		event.buttonList.add(new GuiButton(ALT_LOGIN_ID, 2, event.gui.height - 72,
			80, 20, "Alt Login"));
	}

	@SubscribeEvent
	public void onActionPerformed(GuiScreenEvent.ActionPerformedEvent.Pre event)
	{
		if(!(event.gui instanceof GuiMainMenu) || event.button == null
			|| event.button.id != ALT_LOGIN_ID)
			return;

		Minecraft.getMinecraft().displayGuiScreen(new GuiAltLogin(event.gui));
		event.setCanceled(true);
	}
}
