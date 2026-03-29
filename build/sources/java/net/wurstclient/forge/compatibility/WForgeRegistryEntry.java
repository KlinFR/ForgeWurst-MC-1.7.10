/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

public class WForgeRegistryEntry<T extends WForgeRegistryEntry<T>>
{
	private String registryName;

	@SuppressWarnings("unchecked")
	public T setRegistryName(String modid, String name)
	{
		registryName = modid + ":" + name;
		return (T)this;
	}

	public String getRegistryName()
	{
		return registryName;
	}
}
