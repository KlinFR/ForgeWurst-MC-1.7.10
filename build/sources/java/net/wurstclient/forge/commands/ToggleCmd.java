/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.Hack;

public final class ToggleCmd extends Command
{
	public ToggleCmd()
	{
		super("toggle", "Toggles one or more hacks.",
			"/toggle <hack> [hack...]");
	}

	@Override
	public void call(String[] args) throws CmdException
	{
		if(args.length == 0)
			throw new CmdSyntaxError();

		for(String arg : args)
		{
			Hack hack = wurst.getHax().get(arg);
			if(hack == null)
				throw new CmdError("Unknown hack: " + arg);

			hack.setEnabled(!hack.isEnabled());
		}
	}
}
