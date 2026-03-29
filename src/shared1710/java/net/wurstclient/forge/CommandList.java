/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import net.wurstclient.forge.commands.BindsCmd;
import net.wurstclient.forge.commands.BindCmd;
import net.wurstclient.forge.commands.ClickGuiCmd;
import net.wurstclient.forge.commands.ClearCmd;
import net.wurstclient.forge.commands.GmCmd;
import net.wurstclient.forge.commands.HelpCmd;
import net.wurstclient.forge.commands.SayCmd;
import net.wurstclient.forge.commands.SetCheckboxCmd;
import net.wurstclient.forge.commands.SetEnumCmd;
import net.wurstclient.forge.commands.SetSliderCmd;
import net.wurstclient.forge.commands.TacoCmd;
import net.wurstclient.forge.commands.TCmd;
import net.wurstclient.forge.commands.ToggleCmd;
import net.wurstclient.forge.commands.VClipCmd;
import net.wurstclient.forge.commands.VrTweaksCmd;
import net.wurstclient.forge.commands.WurstCmd;
import net.wurstclient.forge.compatibility.WCommandList;

public final class CommandList extends WCommandList
{
	public final BindCmd bindCmd = register(new BindCmd());
	public final BindsCmd bindsCmd = register(new BindsCmd());
	public final ClickGuiCmd clickGuiCmd = register(new ClickGuiCmd());
	public final ClearCmd clearCmd = register(new ClearCmd());
	public final GmCmd gmCmd = register(new GmCmd());
	public final HelpCmd helpCmd = register(new HelpCmd());
	public final SayCmd sayCmd = register(new SayCmd());
	public final SetCheckboxCmd setCheckboxCmd =
		register(new SetCheckboxCmd());
	public final SetEnumCmd setEnumCmd = register(new SetEnumCmd());
	public final SetSliderCmd setSliderCmd = register(new SetSliderCmd());
	public final TacoCmd tacoCmd = register(new TacoCmd());
	public final TCmd tCmd = register(new TCmd());
	public final ToggleCmd toggleCmd = register(new ToggleCmd());
	public final VClipCmd vClipCmd = register(new VClipCmd());
	public final VrTweaksCmd vrTweaksCmd = register(new VrTweaksCmd());
	public final WurstCmd wurstCmd = register(new WurstCmd());
}
