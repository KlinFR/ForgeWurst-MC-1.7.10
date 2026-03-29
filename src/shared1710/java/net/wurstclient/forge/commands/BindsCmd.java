/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import org.lwjgl.input.Keyboard;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.KeybindList;
import net.wurstclient.forge.KeybindList.Keybind;
import net.wurstclient.forge.utils.ChatUtils;

public final class BindsCmd extends Command
{
	public BindsCmd()
	{
		super("binds", "Manages keybinds.",
			"Syntax: /binds add <key> <hacks>",
			"Syntax: /binds add <key> <commands>",
			"Syntax: /binds remove <key>",
			"Syntax: /binds list [page]",
			"Syntax: /binds remove-all",
			"Syntax: /binds reset",
			"Multiple hacks/commands must be separated by ';'.");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		KeybindList keybinds = wurst.getKeybinds();
		if(args.length == 0)
		{
			list(1);
			return;
		}

		String subCmd = args[0].toLowerCase();
		switch(subCmd)
		{
			case "add":
			add(args);
			return;
			
			case "remove":
			remove(args);
			return;
			
			case "list":
			list(args.length < 2 ? 1 : parsePage(args[1]));
			return;
			
			case "remove-all":
			case "clear":
			if(args.length != 1)
				throw new CmdSyntaxError();
			
			keybinds.removeAll();
			ChatUtils.message("All keybinds removed.");
			return;
			
			case "reset":
			if(args.length != 1)
				throw new CmdSyntaxError();
			
			keybinds.loadDefaults();
			ChatUtils.message("All keybinds reset to defaults.");
			return;
		}

		// Compatibility with the earlier 1.7.10 syntax: /binds <key> <commands>
		if(args.length < 2)
			throw new CmdSyntaxError();
		
		addPrefixed(args);
	}

	private void add(String[] args) throws CmdException
	{
		if(args.length < 3)
			throw new CmdSyntaxError();
		
		String key = normalizeKey(args[1]);
		String commands = joinArgs(args, 2);
		wurst.getKeybinds().add(key, commands);
		ChatUtils.message("Keybind set: " + key + " -> " + commands);
	}

	private void addPrefixed(String[] args) throws CmdException
	{
		String key = normalizeKey(args[0]);
		String commands = joinArgs(args, 1);
		wurst.getKeybinds().add(key, commands);
		ChatUtils.message("Keybind set: " + key + " -> " + commands);
	}

	private void remove(String[] args) throws CmdException
	{
		if(args.length != 2)
			throw new CmdSyntaxError();
		
		String key = normalizeKey(args[1]);
		String oldCommands = wurst.getKeybinds().getCommands(key);
		if(oldCommands == null)
			throw new CmdError("Nothing to remove.");
		
		wurst.getKeybinds().remove(key);
		ChatUtils.message("Keybind removed: " + key + " -> " + oldCommands);
	}

	private void list(int page) throws CmdException
	{
		int keybindCount = wurst.getKeybinds().size();
		int pages = Math.max((int)Math.ceil(keybindCount / 8.0), 1);
		if(page > pages || page < 1)
			throw new CmdSyntaxError("Invalid page: " + page);
		
		ChatUtils.message("Total: " + keybindCount
			+ (keybindCount == 1 ? " keybind" : " keybinds"));
		ChatUtils.message("Keybind list (page " + page + "/" + pages + ")");
		
		for(int i = (page - 1) * 8; i < Math.min(page * 8, keybindCount); i++)
		{
			Keybind keybind = wurst.getKeybinds().get(i);
			ChatUtils.message(keybind.getKey() + " -> " + keybind.getCommands());
		}
	}

	private int parsePage(String value) throws CmdException
	{
		try
		{
			return Integer.parseInt(value);
		}catch(NumberFormatException e)
		{
			throw new CmdSyntaxError("Not a number: " + value);
		}
	}

	private String normalizeKey(String key) throws CmdException
	{
		key = key.toUpperCase();
		if(Keyboard.getKeyIndex(key) == Keyboard.KEY_NONE)
			throw new CmdSyntaxError("Unknown key: " + key);
		
		return key;
	}

	private String joinArgs(String[] args, int start) throws CmdException
	{
		StringBuilder builder = new StringBuilder();
		for(int i = start; i < args.length; i++)
		{
			if(builder.length() > 0)
				builder.append(' ');
			builder.append(args[i]);
		}
		
		if(builder.length() == 0)
			throw new CmdSyntaxError();
		
		return builder.toString();
	}
}
