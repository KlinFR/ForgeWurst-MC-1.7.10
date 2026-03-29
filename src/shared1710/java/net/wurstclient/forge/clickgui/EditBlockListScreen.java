/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.clickgui;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.block.Block;
import net.wurstclient.forge.settings.BlockListSetting;

public final class EditBlockListScreen extends GuiScreen
{
	private final GuiScreen prevScreen;
	private final BlockListSetting setting;
	private GuiTextField input;

	public EditBlockListScreen(GuiScreen prevScreen, BlockListSetting setting)
	{
		this.prevScreen = prevScreen;
		this.setting = setting;
	}

	@Override
	public void initGui()
	{
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 100, height - 28, 98, 20,
			"Done"));
		buttonList.add(new GuiButton(1, width / 2 + 2, height - 28, 98, 20,
			"Reset"));
		input = new GuiTextField(fontRendererObj, width / 2 - 100, 30, 200, 20);
		input.setFocused(true);
	}

	@Override
	protected void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			mc.displayGuiScreen(prevScreen);
			return;
		}

		if(button.id == 1)
			setting.resetToDefaults();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode)
	{
		if(input.textboxKeyTyped(typedChar, keyCode))
		{
			if(keyCode == Keyboard.KEY_RETURN)
				tryAddBlock();
			return;
		}

		if(keyCode == Keyboard.KEY_RETURN)
		{
			tryAddBlock();
			return;
		}
		
		if(keyCode == Keyboard.KEY_ESCAPE)
		{
			mc.displayGuiScreen(prevScreen);
			return;
		}

		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		super.mouseClicked(mouseX, mouseY, mouseButton);
		input.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void updateScreen()
	{
		input.updateCursorCounter();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks)
	{
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, "Edit block list", width / 2, 12,
			0xffffff);
		input.drawTextBox();

		int y = 60;
		for(String block : setting.getBlockNames())
		{
			drawString(fontRendererObj, block, width / 2 - 100, y, 0xffffff);
			y += 10;
		}

		drawCenteredString(fontRendererObj,
			"Type a block registry name and press Enter to add it.",
			width / 2, height - 45, 0xa0a0a0);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void onGuiClosed()
	{
	}

	private void tryAddBlock()
	{
		String text = input.getText().trim();
		if(text.isEmpty())
			return;

		Block block = Block.getBlockFromName(text);
		if(block != null)
			setting.add(block);

		input.setText("");
	}
}
