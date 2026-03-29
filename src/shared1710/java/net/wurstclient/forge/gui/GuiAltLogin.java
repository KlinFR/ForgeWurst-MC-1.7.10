/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, you can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.gui;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.wurstclient.forge.compatibility.WSession;

public final class GuiAltLogin extends GuiScreen
{
	private final GuiScreen parent;
	private GuiTextField usernameField;
	private String status;

	public GuiAltLogin(GuiScreen parent)
	{
		this.parent = parent;
	}

	@Override
	public void initGui()
	{
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + 12, 98, 20,
			"Login"));
		buttonList.add(new GuiButton(1, width / 2 + 2, height / 2 + 12, 98, 20,
			"Cancel"));

		usernameField =
			new GuiTextField(fontRendererObj, width / 2 - 100, height / 2 - 24,
				200, 20);
		usernameField.setFocused(true);
		usernameField.setText(mc.getSession().getUsername());
		status = null;
	}

	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			attemptLogin();
			return;
		}

		if(button.id == 1)
			mc.displayGuiScreen(parent);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(usernameField.textboxKeyTyped(typedChar, keyCode))
			return;

		if(keyCode == Keyboard.KEY_RETURN)
		{
			attemptLogin();
			return;
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void updateScreen()
	{
		usernameField.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, "Alt Login", width / 2, 20,
			0xFFFFFF);
		drawCenteredString(fontRendererObj,
			"Enter a nickname and switch to a local offline session.", width / 2,
			40, 0xA0A0A0);
		usernameField.drawTextBox();

		if(status != null)
			drawCenteredString(fontRendererObj, status, width / 2, height / 2 + 40,
				0xFF5555);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void attemptLogin()
	{
		String username = usernameField.getText().trim();
		if(username.isEmpty())
		{
			status = "Nickname can't be empty.";
			return;
		}

		if(username.length() > 16)
		{
			status = "Nickname must be 16 characters or less.";
			return;
		}

		if(WSession.setOfflineUsername(username))
		{
			status = "Switched to " + username + ".";
			System.out.println("Switched offline session to " + username);
			mc.displayGuiScreen(parent);
		}else
			status = "Failed to switch session.";
	}
}
