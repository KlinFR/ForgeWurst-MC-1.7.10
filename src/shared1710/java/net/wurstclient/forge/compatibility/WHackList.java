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

import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.Hack;

public abstract class WHackList
{
	private final Map<String, Hack> registry = new LinkedHashMap<>();

	protected final <T extends Hack> T register(T hack)
	{
		hack.setRegistryName(ForgeWurst.MODID, hack.getName().toLowerCase());
		registry.put(hack.getName().toLowerCase(), hack);
		return hack;
	}

	public final Collection<Hack> getRegistry()
	{
		return Collections.unmodifiableCollection(registry.values());
	}

	public final Collection<Hack> getValues()
	{
		return getRegistry();
	}

	public final Hack get(String name)
	{
		if(name == null)
			return null;

		return registry.get(name.toLowerCase());
	}
}
