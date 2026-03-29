/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.wurstclient.forge.Command;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.Setting;

final class SettingCommandUtils
{
	private SettingCommandUtils()
	{
	}

	static Hack requireHack(Command command, String hackName)
		throws Command.CmdException
	{
		Hack hack = ForgeWurst.getForgeWurst().getHax().get(hackName);
		if(hack == null)
			throw command.new CmdError("Hack \"" + hackName + "\" could not be found.");

		return hack;
	}

	static Setting requireSetting(Command command, Hack hack, String settingName)
		throws Command.CmdException
	{
		Setting setting = hack.getSettings()
			.get(settingName.toLowerCase().replace("_", " "));
		if(setting == null)
			throw command.new CmdError("Setting \"" + hack.getName() + " "
				+ settingName + "\" could not be found.");

		return setting;
	}

	static void requireType(Command command, Hack hack, Setting setting,
		String simpleName, String description) throws Command.CmdException
	{
		if(isType(setting, simpleName))
			return;

		throw command.new CmdError(hack.getName() + " " + setting.getName()
			+ " is not " + description + ".");
	}

	static void saveSettings()
	{
		ForgeWurst.getForgeWurst().getHax().saveSettings();
	}

	static boolean isType(Setting setting, String simpleName)
	{
		Class<?> type = setting.getClass();
		while(type != null)
		{
			if(type.getSimpleName().equals(simpleName))
				return true;
			type = type.getSuperclass();
		}

		return false;
	}

	static void invokeVoid(Command command, Setting setting, String methodName,
		Class<?> argType, Object arg) throws Command.CmdException
	{
		try
		{
			Method method = setting.getClass().getMethod(methodName, argType);
			method.invoke(setting, arg);
		}catch(NoSuchMethodException e)
		{
			throw command.new CmdError(
				"Unsupported setting implementation: " + setting.getClass().getName());
		}catch(IllegalAccessException | InvocationTargetException e)
		{
			throw command.new CmdError(
				e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
		}
	}

	static double getDouble(Command command, Setting setting, String methodName)
		throws Command.CmdException
	{
		try
		{
			Method method = setting.getClass().getMethod(methodName);
			Object value = method.invoke(setting);
			if(value instanceof Number)
				return ((Number)value).doubleValue();
		}catch(NoSuchMethodException e)
		{
			throw command.new CmdError(
				"Unsupported setting implementation: " + setting.getClass().getName());
		}catch(IllegalAccessException | InvocationTargetException e)
		{
			throw command.new CmdError(
				e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
		}

		throw command.new CmdError("Unsupported slider value type.");
	}
}
