/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.minecraft.client.entity.EntityClientPlayerMP;
import net.wurstclient.forge.Command;

public final class SayCmd extends Command
{
	public SayCmd()
	{
		super("say", "Sends a chat message or command.", ".say <message>");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0)
			throw new CmdSyntaxError();

		StringBuilder sb = new StringBuilder();
		for(String arg : args)
		{
			if(sb.length() > 0)
				sb.append(' ');
			sb.append(arg);
		}

		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null)
			throw new CmdError("Player not available.");

		String message = sb.toString();
		player.sendChatMessage(message);
	}
}
