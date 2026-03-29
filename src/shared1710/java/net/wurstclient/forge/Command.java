/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.wurstclient.forge.compatibility.WForgeRegistryEntry;
import net.wurstclient.forge.utils.ChatUtils;

public abstract class Command extends WForgeRegistryEntry<Command>
	implements ICommand
{
	protected static final ForgeWurst wurst = ForgeWurst.getForgeWurst();
	protected static final Minecraft mc = Minecraft.getMinecraft();

	private final String name;
	private final String description;
	private final String[] syntax;

	public Command(String name, String description, String... syntax)
	{
		this.name = name;
		this.description = description;
		this.syntax = syntax;
	}

	public abstract void call(String[] args) throws CmdException;

	@Override
	public final String getCommandName()
	{
		return name.toLowerCase();
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		if(syntax.length == 0)
			return "." + getCommandName();

		return formatForDisplay(syntax[0]);
	}

	@Override
	public final List<String> getCommandAliases()
	{
		return Collections.emptyList();
	}

	@Override
	public final void processCommand(ICommandSender sender, String[] args)
		throws CommandException
	{
		try
		{
			call(args);
		}catch(CmdException e)
		{
			e.printToChat();
		}
	}

	@Override
	public final boolean canCommandSenderUseCommand(ICommandSender sender)
	{
		return true;
	}

	@Override
	public final List<String> addTabCompletionOptions(ICommandSender sender,
		String[] args)
	{
		return Collections.emptyList();
	}

	@Override
	public final boolean isUsernameIndex(String[] args, int index)
	{
		return false;
	}

	@Override
	public final int compareTo(Object other)
	{
		if(!(other instanceof ICommand))
			return 0;
		
		return getCommandName()
			.compareToIgnoreCase(((ICommand)other).getCommandName());
	}

	public final String getName()
	{
		return name;
	}

	public final String getDescription()
	{
		return description;
	}

	public final String[] getSyntax()
	{
		return syntax;
	}

	public final String formatForDisplay(String text)
	{
		if(text == null || !text.startsWith("/"))
			return text;

		return "." + text.substring(1);
	}

	public abstract class CmdException extends Exception
	{
		public CmdException()
		{
			super();
		}

		public CmdException(String message)
		{
			super(message);
		}

		public abstract void printToChat();
	}

	public final class CmdError extends CmdException
	{
		public CmdError(String message)
		{
			super(message);
		}

		@Override
		public void printToChat()
		{
			ChatUtils.error(getMessage());
		}
	}

	public final class CmdSyntaxError extends CmdException
	{
		public CmdSyntaxError()
		{
			super();
		}

		public CmdSyntaxError(String message)
		{
			super(message);
		}

		@Override
		public void printToChat()
		{
			if(getMessage() != null)
				ChatUtils.message("\u00a74Syntax error:\u00a7r " + getMessage());

			for(String line : syntax)
				ChatUtils.message(formatForDisplay(line));
		}
	}
}
