/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.utils;

import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

public final class PacketOutputInterceptor
{
	private static final String HANDLER_NAME = "forgewurst_packet_output";
	private static final CopyOnWriteArrayList<Listener> listeners =
		new CopyOnWriteArrayList<>();
	private static final ThreadLocal<Boolean> bypass =
		new ThreadLocal<Boolean>()
		{
			@Override
			protected Boolean initialValue()
			{
				return false;
			}
		};

	private PacketOutputInterceptor()
	{
	}

	public static void addListener(Listener listener)
	{
		if(!listeners.contains(listener))
			listeners.add(listener);

		ensureInstalled();
	}

	public static void removeListener(Listener listener)
	{
		listeners.remove(listener);

		if(listeners.isEmpty())
			removeHandler();
	}

	public static void ensureInstalled()
	{
		Channel channel = getChannel();
		if(channel == null)
			return;

		if(channel.pipeline().get(HANDLER_NAME) != null)
			return;

		if(channel.pipeline().get("packet_handler") != null)
			channel.pipeline().addBefore("packet_handler", HANDLER_NAME,
				new Handler());
		else
			channel.pipeline().addLast(HANDLER_NAME, new Handler());
	}

	public static void sendBypass(Packet packet)
	{
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if(player == null || player.sendQueue == null)
			return;

		boolean previous = bypass.get();
		bypass.set(true);

		try
		{
			player.sendQueue.addToSendQueue(packet);
		}finally
		{
			bypass.set(previous);
		}
	}

	public static boolean isBypassActive()
	{
		return bypass.get();
	}

	private static void removeHandler()
	{
		Channel channel = getChannel();
		if(channel == null)
			return;

		if(channel.pipeline().get(HANDLER_NAME) != null)
			channel.pipeline().remove(HANDLER_NAME);
	}

	private static Channel getChannel()
	{
		EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
		if(player == null || player.sendQueue == null)
			return null;

		NetworkManager manager =
			findField(player.sendQueue.getClass(), NetworkManager.class,
				player.sendQueue);
		if(manager == null)
			return null;

		return findField(manager.getClass(), Channel.class, manager);
	}

	private static <T> T findField(Class<?> type, Class<T> fieldType,
		Object owner)
	{
		for(Class<?> current = type; current != null;
			current = current.getSuperclass())
		{
			for(Field field : current.getDeclaredFields())
			{
				if(!fieldType.isAssignableFrom(field.getType()))
					continue;

				field.setAccessible(true);
				try
				{
					return fieldType.cast(field.get(owner));
				}catch(ReflectiveOperationException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		return null;
	}

	public static interface Listener
	{
		public boolean onOutgoingPacket(Packet packet);
	}

	private static final class Handler extends ChannelDuplexHandler
	{
		@Override
		public void write(ChannelHandlerContext context, Object message,
			ChannelPromise promise) throws Exception
		{
			if(!(message instanceof Packet) || bypass.get())
			{
				super.write(context, message, promise);
				return;
			}

			Packet packet = (Packet)message;
			for(Listener listener : listeners)
				if(listener.onOutgoingPacket(packet))
				{
					promise.setSuccess();
					return;
				}

			super.write(context, message, promise);
		}
	}
}
