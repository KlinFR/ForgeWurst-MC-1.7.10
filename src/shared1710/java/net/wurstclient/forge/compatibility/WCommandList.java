/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.ForgeWurst;

public abstract class WCommandList
{
	private final Map<String, Command> registry = new LinkedHashMap<>();

	protected final <T extends Command> T register(T cmd)
	{
		cmd.setRegistryName(ForgeWurst.MODID, cmd.getName().toLowerCase());
		registry.put(cmd.getName().toLowerCase(), cmd);
		return cmd;
	}

	public final Collection<Command> getRegistry()
	{
		return Collections.unmodifiableCollection(registry.values());
	}

	public final Collection<Command> getValues()
	{
		return getRegistry();
	}

	public final Command get(String name)
	{
		if(name == null)
			return null;

		return registry.get(name.toLowerCase());
	}
}
