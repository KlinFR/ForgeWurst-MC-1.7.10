/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.settings;

import java.util.Objects;

import com.google.gson.JsonElement;

import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.clickgui.Component;

public abstract class Setting
{
	private final String name;
	private final String description;

	public Setting(String name, String description)
	{
		this.name = Objects.requireNonNull(name);
		this.description = description;
	}

	public final String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	protected final void changed()
	{
		ForgeWurst wurst = ForgeWurst.getForgeWurst();
		if(wurst != null && wurst.getHax() != null)
			wurst.getHax().saveSettings();
	}

	public abstract Component getComponent();

	public abstract void fromJson(JsonElement json);

	public abstract JsonElement toJson();
}
