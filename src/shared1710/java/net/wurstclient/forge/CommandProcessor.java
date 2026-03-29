/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.client.ClientCommandHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WChatOutputEvent;
import net.wurstclient.forge.Command.CmdException;
import net.wurstclient.forge.utils.ChatUtils;

public final class CommandProcessor
{
	private final CommandList cmds;
	private String lastTabCompletionPrefix;

	public CommandProcessor(CommandList cmds)
	{
		this.cmds = cmds;
	}

	public boolean runCommand(String input)
	{
		return runCommand(input, true);
	}

	public boolean handleChatTabComplete(GuiScreen screen, int keyCode)
	{
		if(keyCode != Keyboard.KEY_TAB || !(screen instanceof GuiChat))
			return false;

		GuiTextField inputField = findChatInputField(screen);
		if(inputField == null)
			return false;

		String text = inputField.getText();
		if(text.isEmpty())
		{
			clearTabCompletionState();
			return false;
		}

		if(text.charAt(0) != '.')
		{
			clearTabCompletionState();
			return false;
		}

		String commandInput = text.substring(1);
		if(commandInput.isEmpty())
		{
			clearTabCompletionState();
			return false;
		}

		String[] parts = splitArguments(commandInput);
		if(parts.length == 0)
		{
			clearTabCompletionState();
			return false;
		}

		String commandName = parts[0];
		List<String> matches = getCommandMatches(commandName);
		if(matches.isEmpty())
		{
			clearTabCompletionState();
			return false;
		}

		String completion = matches.size() == 1 ? matches.get(0)
			: getCommonPrefix(matches);
		if(completion.length() <= commandName.length())
		{
			String completionKey = commandName.toLowerCase();
			if(!completionKey.equals(lastTabCompletionPrefix))
			{
				ChatUtils.message("Suggestions: " + formatSuggestions(matches));
				lastTabCompletionPrefix = completionKey;
			}

			return true;
		}

		StringBuilder completedText = new StringBuilder();
		completedText.append('.').append(completion);
		completedText.append(commandInput.substring(commandName.length()));
		if(commandInput.length() == commandName.length())
			completedText.append(' ');

		inputField.setText(completedText.toString());
		inputField.setCursorPositionEnd();
		clearTabCompletionState();

		return true;
	}

	@SubscribeEvent
	public void onSentMessage(WChatOutputEvent event)
	{
		String message = event.getMessage().trim();
		if(message.isEmpty())
			return;

		if(!message.startsWith("."))
			return;

		event.setCanceled(true);
		Minecraft.getMinecraft().ingameGUI.getChatGUI()
			.addToSentMessages(message);
		runCommand(message, false);
	}

	private boolean runCommand(String input, boolean allowVanillaFallback)
	{
		String originalInput = input.trim();
		if(originalInput.isEmpty())
			return false;

		boolean hadDot = originalInput.startsWith(".");
		boolean hadSlash = originalInput.startsWith("/");
		String commandInput = originalInput;
		if(hadDot || hadSlash)
			commandInput = commandInput.substring(1).trim();

		if(commandInput.isEmpty())
			return false;

		String[] parts = splitArguments(commandInput);
		if(parts.length == 0)
			return false;

		Command cmd = cmds.get(parts[0]);

		if(cmd == null)
		{
			if(hadSlash && allowVanillaFallback)
			{
				EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
				if(player == null)
					return false;

				Minecraft.getMinecraft().ingameGUI.getChatGUI()
					.addToSentMessages(originalInput);
				if(ClientCommandHandler.instance.executeCommand(player,
					commandInput) == 0)
					player.sendChatMessage(originalInput);
				return true;
			}

			if(hadDot)
			{
				ChatUtils.error("Unknown command: ." + parts[0]);
				ChatUtils.message("Type \".help\" for a list of commands.");
				return true;
			}
			return false;
		}

		try
		{
			cmd.call(Arrays.copyOfRange(parts, 1, parts.length));
		}catch(CmdException e)
		{
			e.printToChat();
		}

		return true;
	}

	public boolean hasCommand(String name)
	{
		return cmds.get(name) != null;
	}

	private void clearTabCompletionState()
	{
		lastTabCompletionPrefix = null;
	}

	private String[] splitArguments(String input)
	{
		ArrayList<String> parts = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean quoted = false;
		boolean escaped = false;

		for(int i = 0; i < input.length(); i++)
		{
			char c = input.charAt(i);

			if(escaped)
			{
				current.append(c);
				escaped = false;
				continue;
			}

			if(c == '\\')
			{
				escaped = true;
				continue;
			}

			if(c == '"')
			{
				quoted = !quoted;
				continue;
			}

			if(Character.isWhitespace(c) && !quoted)
			{
				addPart(parts, current);
				continue;
			}

			current.append(c);
		}

		if(escaped)
			current.append('\\');

		addPart(parts, current);
		return parts.toArray(new String[0]);
	}

	private void addPart(List<String> parts, StringBuilder current)
	{
		if(current.length() == 0)
			return;

		parts.add(current.toString());
		current.setLength(0);
	}

	private List<String> getCommandMatches(String prefix)
	{
		ArrayList<String> matches = new ArrayList<>();
		String lowerPrefix = prefix.toLowerCase();

		Collection<Command> commands = cmds.getValues();
		for(Command cmd : commands)
		{
			String name = cmd.getCommandName();
			if(name.startsWith(lowerPrefix))
				matches.add(name);
		}

		matches.sort(String::compareToIgnoreCase);
		return matches;
	}

	private String getCommonPrefix(List<String> values)
	{
		if(values.isEmpty())
			return "";

		String prefix = values.get(0);
		for(int i = 1; i < values.size(); i++)
		{
			String value = values.get(i);
			int maxLength = Math.min(prefix.length(), value.length());
			int j = 0;
			while(j < maxLength
				&& Character.toLowerCase(prefix.charAt(j)) == Character
					.toLowerCase(value.charAt(j)))
				j++;

			prefix = prefix.substring(0, j);
			if(prefix.isEmpty())
				return "";
		}

		return prefix;
	}

	private String formatSuggestions(List<String> matches)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < matches.size(); i++)
		{
			if(i > 0)
				sb.append(", ");
			sb.append('.').append(matches.get(i));
		}

		return sb.toString();
	}

	private GuiTextField findChatInputField(GuiScreen screen)
	{
		for(Class<?> type = screen.getClass(); type != null; type = type
			.getSuperclass())
			for(java.lang.reflect.Field field : type.getDeclaredFields())
				if(GuiTextField.class.isAssignableFrom(field.getType()))
				{
					try
					{
						field.setAccessible(true);
						Object value = field.get(screen);
						if(value instanceof GuiTextField)
							return (GuiTextField)value;
					}catch(IllegalAccessException e)
					{
						return null;
					}
				}

		return null;
	}
}
