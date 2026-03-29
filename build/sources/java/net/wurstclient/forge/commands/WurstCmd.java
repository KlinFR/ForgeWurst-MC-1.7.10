/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.minecraft.command.ICommandSender;
import net.wurstclient.forge.Command;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.utils.ChatUtils;

public final class WurstCmd extends Command
{
	public WurstCmd()
	{
		super("wurst", "Shows information about ForgeWurst.", "/wurst");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length > 0)
			throw new CmdSyntaxError();

		ChatUtils.message("ForgeWurst " + ForgeWurst.VERSION + " for 1.7.10");
		ChatUtils.message("Use .help to list Wurst commands.");
		ChatUtils.message("Use /help wurst for vanilla help.");
		ChatUtils.message("RCONTROL opens ClickGUI.");
	}

	@Override
	public String getCommandUsage(ICommandSender sender)
	{
		return "/wurst";
	}
}
