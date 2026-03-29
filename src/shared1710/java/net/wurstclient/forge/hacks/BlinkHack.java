/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayDeque;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WPacketOutputEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.utils.EntityFakePlayer;
import net.wurstclient.forge.utils.PacketOutputInterceptor;

@Hack.DontSaveState
public final class BlinkHack extends Hack
{
	private final SliderSetting limit = new SliderSetting("Limit",
		"Automatically restarts Blink once\n" + "the given number of packets\n"
			+ "have been suspended.\n\n" + "0 = no limit",
		0, 0, 500, 1,
		value -> value == 0 ? "disabled" : (int)value + "");

	private final ArrayDeque<Packet> packets = new ArrayDeque<>();
	private EntityFakePlayer fakePlayer;

	public BlinkHack()
	{
		super("Blink", "Suspends all motion updates while enabled.");
		setCategory(Category.MOVEMENT);
		addSetting(limit);
	}

	@Override
	public String getRenderName()
	{
		if(limit.getValueI() == 0)
			return getName() + " [" + packets.size() + "]";

		return getName() + " [" + packets.size() + "/" + limit.getValueI()
			+ "]";
	}

	@Override
	protected void onEnable()
	{
		if(mc.thePlayer != null && mc.theWorld != null)
			fakePlayer = new EntityFakePlayer();

		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		if(fakePlayer != null)
		{
			fakePlayer.despawn();
			fakePlayer = null;
		}

		if(mc.thePlayer == null || mc.thePlayer.sendQueue == null)
			packets.clear();
		else
			while(!packets.isEmpty())
				PacketOutputInterceptor.sendBypass(packets.removeFirst());
	}

	@SubscribeEvent
	public void onPacketOutput(WPacketOutputEvent event)
	{
		Packet packet = event.getPacket();
		if(!(packet instanceof C03PacketPlayer))
			return;

		packets.addLast(packet);
		event.setCanceled(true);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		if(mc.thePlayer == null || mc.theWorld == null)
			return;

		if(fakePlayer == null)
			fakePlayer = new EntityFakePlayer();

		if(limit.getValueI() == 0)
			return;

		if(packets.size() >= limit.getValueI())
		{
			setEnabled(false);
			setEnabled(true);
		}
	}
}
