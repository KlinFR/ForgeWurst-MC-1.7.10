/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import java.util.ArrayList;
import java.util.Comparator;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.utils.ChatUtils;

public final class HelpCmd extends Command
{
	public HelpCmd()
	{
		super("help", "Shows help.",
			"Syntax: .help <command>", "List commands: .help [page]");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 1)
			throw new CmdSyntaxError();
		
		String arg = args.length == 0 ? "1" : args[0];
		if(isInteger(arg))
		{
			list(Integer.parseInt(arg));
			return;
		}
		
		help(arg);
	}

	private void list(int page) throws CmdException
	{
		ArrayList<Command> commands = new ArrayList<>();
		commands.addAll(ForgeWurst.getForgeWurst().getCmds().getValues());
		commands.sort(Comparator.comparing(Command::getName));
		
		int size = commands.size();
		int pages = Math.max((int)Math.ceil(size / 8.0), 1);
		if(page > pages || page < 1)
			throw new CmdSyntaxError("Invalid page: " + page);
		
		ChatUtils
			.message("Total: " + size + (size == 1 ? " command" : " commands"));
		ChatUtils.message("Command list (page " + page + "/" + pages + ")");
		
		for(int i = (page - 1) * 8; i < Math.min(page * 8, size); i++)
		{
			Command cmd = commands.get(i);
			ChatUtils.message("." + cmd.getName() + " - " + cmd.getDescription());
		}
	}

	private void help(String name) throws CmdException
	{
		if(name.startsWith(".") || name.startsWith("/"))
			name = name.substring(1);
		
		Command cmd = ForgeWurst.getForgeWurst().getCmds().get(name);
		if(cmd == null)
			throw new CmdSyntaxError("Unknown command: " + name);

		ChatUtils.message("." + cmd.getName() + " - " + cmd.getDescription());
		for(String line : cmd.getSyntax())
			ChatUtils.message(cmd.formatForDisplay(line));
	}

	private boolean isInteger(String value)
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e)
		{
			return false;
		}
	}
}
