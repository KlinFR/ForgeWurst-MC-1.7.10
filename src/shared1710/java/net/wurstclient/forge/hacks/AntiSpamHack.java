/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;

public final class AntiSpamHack extends Hack
{
	private static final int LINE_ID = 2048576431;

	private String lastMessage = "";
	private int duplicateCount;

	public AntiSpamHack()
	{
		super("AntiSpam",
			"Blocks chat spam by adding a counter to repeated messages.");
		setCategory(Category.CHAT);
	}

	@Override
	protected void onEnable()
	{
		lastMessage = "";
		duplicateCount = 0;
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent
	public void onChatReceived(ClientChatReceivedEvent event)
	{
		IChatComponent message = event.message;
		if(message == null)
			return;

		String text = message.getUnformattedText();
		if(text == null || text.isEmpty())
			return;

		if(!text.equals(lastMessage))
		{
			lastMessage = text;
			duplicateCount = 1;
			return;
		}

		duplicateCount++;
		event.setCanceled(true);

		GuiNewChat chat = mc.ingameGUI.getChatGUI();
		chat.deleteChatLine(LINE_ID);
		chat.printChatMessageWithOptionalDeletion(
			new ChatComponentText(text + " [x" + duplicateCount + "]"), LINE_ID);
	}
}
