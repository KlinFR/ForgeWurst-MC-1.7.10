/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WCameraTransformViewBobbingEvent;
import net.wurstclient.fmlevents.WIsOpaqueCubeEvent;
import net.wurstclient.fmlevents.WIsNormalCubeEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WPacketOutputEvent;
import net.wurstclient.fmlevents.WPlayerMoveEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.EntityFakePlayer;
import net.wurstclient.forge.utils.KeyBindingUtils;

@Hack.DontSaveState
public final class FreecamHack extends Hack
{
	private final SliderSetting speed =
		new SliderSetting("Speed", 1, 0.05, 10, 0.05, ValueDisplay.DECIMAL);

	private EntityFakePlayer fakePlayer;

	public FreecamHack()
	{
		super("Freecam", "Allows you to move the camera\n"
			+ "without moving your character.");
		setCategory(Category.RENDER);
		addSetting(speed);
	}

	@Override
	protected void onEnable()
	{
		if(mc.thePlayer != null && mc.theWorld != null)
			fakePlayer = new EntityFakePlayer();

		MinecraftForge.EVENT_BUS.register(this);

		GameSettings gs = mc.gameSettings;
		KeyBinding[] bindings = {gs.keyBindForward, gs.keyBindBack,
			gs.keyBindLeft, gs.keyBindRight, gs.keyBindJump, gs.keyBindSneak};
		for(KeyBinding binding : bindings)
			KeyBindingUtils.resetPressed(binding);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		EntityClientPlayerMP player = mc.thePlayer;
		if(player != null)
		{
			player.noClip = false;
			player.motionX = 0;
			player.motionY = 0;
			player.motionZ = 0;
		}

		if(fakePlayer != null)
		{
			fakePlayer.resetPlayerPosition();
			fakePlayer.despawn();
			fakePlayer = null;
		}

		if(mc.renderGlobal != null)
			mc.renderGlobal.loadRenderers();
	}

	@SubscribeEvent
	public void onPacketOutput(WPacketOutputEvent event)
	{
		if(event.getPacket() instanceof C03PacketPlayer)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onPacketInput(WPacketInputEvent event)
	{
		if(event.getPacket() instanceof S08PacketPlayerPosLook)
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onPlayerMove(WPlayerMoveEvent event)
	{
		event.getPlayer().noClip = true;
	}

	@SubscribeEvent
	public void onIsNormalCube(WIsNormalCubeEvent event)
	{
		event.setNormalCube(false);
	}

	@SubscribeEvent
	public void onIsOpaqueCube(WIsOpaqueCubeEvent event)
	{
		event.setOpaqueCube(false);
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
		EntityClientPlayerMP player = mc.thePlayer;
		if(player == null || mc.theWorld == null)
			return;

		if(fakePlayer == null)
			fakePlayer = new EntityFakePlayer();

		player.noClip = true;
		player.onGround = false;
		player.fallDistance = 0;
		player.motionX = 0;
		player.motionY = 0;
		player.motionZ = 0;
		player.jumpMovementFactor = speed.getValueF();

		float forward = 0;
		float strafe = 0;
		if(mc.gameSettings.keyBindForward.getIsKeyPressed())
			forward += 1;
		if(mc.gameSettings.keyBindBack.getIsKeyPressed())
			forward -= 1;
		if(mc.gameSettings.keyBindLeft.getIsKeyPressed())
			strafe += 1;
		if(mc.gameSettings.keyBindRight.getIsKeyPressed())
			strafe -= 1;

		player.moveFlying(strafe, forward, speed.getValueF());

		if(mc.gameSettings.keyBindJump.getIsKeyPressed())
			player.motionY += speed.getValue();
		if(mc.gameSettings.keyBindSneak.getIsKeyPressed())
			player.motionY -= speed.getValue();

		player.moveEntity(player.motionX, player.motionY, player.motionZ);
	}

	@SubscribeEvent
	public void onCameraTransformViewBobbing(
		WCameraTransformViewBobbingEvent event)
	{
		event.setCanceled(true);
	}
}
