/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;

public final class WSession
{
	private WSession()
	{
	}

	public static boolean setOfflineUsername(String username)
	{
		if(username == null)
			return false;

		username = username.trim();
		if(username.isEmpty())
			return false;

		UUID uuid = UUID.nameUUIDFromBytes(
			("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
		Session session = new Session(username, uuid.toString(), "", "legacy");
		return setSession(session);
	}

	public static boolean setSession(Session session)
	{
		if(session == null)
			return false;

		try
		{
			Minecraft mc = Minecraft.getMinecraft();
			Field sessionField = null;

			for(Field field : Minecraft.class.getDeclaredFields())
			{
				if(field.getType() == Session.class)
				{
					sessionField = field;
					break;
				}
			}

			if(sessionField == null)
				return false;

			sessionField.setAccessible(true);

			try
			{
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(sessionField,
					sessionField.getModifiers() & ~Modifier.FINAL);
			}catch(NoSuchFieldException ignored)
			{
				// Java 8 exposes Field.modifiers, but keep going if the runtime
				// differs.
			}

			sessionField.set(mc, session);
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
}
