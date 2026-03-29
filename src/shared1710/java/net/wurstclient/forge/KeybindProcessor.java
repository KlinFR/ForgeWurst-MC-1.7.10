/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.ArrayList;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent.KeyInputEvent;

public final class KeybindProcessor
{
	private final HackList hax;
	private final KeybindList keybinds;
	private final CommandProcessor cmdProcessor;

	public KeybindProcessor(HackList hax, KeybindList keybinds,
		CommandProcessor cmdProcessor)
	{
		this.hax = hax;
		this.keybinds = keybinds;
		this.cmdProcessor = cmdProcessor;
	}

	@SubscribeEvent
	public void onKeyInput(KeyInputEvent event)
	{
		int keyCode = Keyboard.getEventKey();
		if(keyCode == 0 || !Keyboard.getEventKeyState())
			return;

		String commands = keybinds.getCommands(Keyboard.getKeyName(keyCode));
		if(commands == null)
			return;

		for(String command : splitCommands(commands))
		{
			runBinding(command);
		}
	}

	private void runBinding(String command)
	{
		if(command.isEmpty())
			return;

		if(command.startsWith(".") || command.startsWith("/")
			|| command.contains(" "))
		{
			cmdProcessor.runCommand(command);
			return;
		}

		Hack hack = hax.get(command);
		if(hack != null)
		{
			hack.setEnabled(!hack.isEnabled());
			return;
		}

		if(cmdProcessor.hasCommand(command))
			cmdProcessor.runCommand(command);
	}

	private ArrayList<String> splitCommands(String commands)
	{
		ArrayList<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();

		for(int i = 0; i < commands.length(); i++)
		{
			char c = commands.charAt(i);
			if(c == ';')
			{
				if(i + 1 < commands.length() && commands.charAt(i + 1) == ';')
				{
					current.append(';');
					i++;
					continue;
				}

				addCommand(result, current);
				continue;
			}

			current.append(c);
		}

		addCommand(result, current);
		return result;
	}

	private void addCommand(ArrayList<String> result, StringBuilder current)
	{
		String command = current.toString().trim();
		if(!command.isEmpty())
			result.add(command);

		current.setLength(0);
	}
}
