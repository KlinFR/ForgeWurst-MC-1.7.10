/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.compatibility;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WChatOutputEvent;
import net.wurstclient.fmlevents.WCameraTransformViewBobbingEvent;
import net.wurstclient.fmlevents.WCanRenderInPassEvent;
import net.wurstclient.fmlevents.WEntityPlayerJumpEvent;
import net.wurstclient.fmlevents.WGetAmbientOcclusionLightValueEvent;
import net.wurstclient.fmlevents.WGetLiquidCollisionBoxEvent;
import net.wurstclient.fmlevents.WHurtCameraEffectEvent;
import net.wurstclient.fmlevents.WIsOpaqueCubeEvent;
import net.wurstclient.fmlevents.WIsNormalCubeEvent;
import net.wurstclient.fmlevents.WPostMotionEvent;
import net.wurstclient.fmlevents.WPacketInputEvent;
import net.wurstclient.fmlevents.WPacketOutputEvent;
import net.wurstclient.fmlevents.WPlayerDamageBlockEvent;
import net.wurstclient.fmlevents.WPlayerMoveEvent;
import net.wurstclient.fmlevents.WPreMotionEvent;
import net.wurstclient.fmlevents.WRenderBlockByRenderTypeEvent;
import net.wurstclient.fmlevents.WRenderBlockModelEvent;
import net.wurstclient.fmlevents.WRenderTileEntityEvent;
import net.wurstclient.fmlevents.WShouldLiquidBeSolidEvent;
import net.wurstclient.fmlevents.WShouldSideBeRenderedEvent;
import net.wurstclient.forge.ForgeWurst;
import net.wurstclient.forge.gui.GuiAltLogin;
import net.wurstclient.forge.utils.PacketOutputInterceptor;
import net.wurstclient.forge.compatibility.WMinecraft;

public final class WEventFactory
{
	private static boolean registered;

	private WEventFactory()
	{
	}

	public static void register()
	{
		if(registered)
			return;

		FMLCommonHandler.instance().bus().register(WTickBridge.INSTANCE);
		registered = true;
	}

	public static boolean hurtCameraEffect()
	{
		return !MinecraftForge.EVENT_BUS.post(new WHurtCameraEffectEvent());
	}

	public static boolean cameraTransformViewBobbing()
	{
		return !MinecraftForge.EVENT_BUS
			.post(new WCameraTransformViewBobbingEvent());
	}

	public static void onPreMotion(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPreMotionEvent(player));
	}

	public static void onPostMotion(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPostMotionEvent(player));
	}

	public static boolean onClientSentMessage(String message)
	{
		return !MinecraftForge.EVENT_BUS.post(new WChatOutputEvent(message));
	}

	public static boolean onGuiChatTabComplete(GuiScreen screen, int keyCode)
	{
		ForgeWurst wurst = ForgeWurst.getForgeWurst();
		if(wurst == null || wurst.getCmdProcessor() == null)
			return false;

		return wurst.getCmdProcessor().handleChatTabComplete(screen, keyCode);
	}

	public static void onPlayerMove(EntityPlayerSP player)
	{
		MinecraftForge.EVENT_BUS.post(new WPlayerMoveEvent(player));
	}

	public static boolean entityPlayerJump(
		net.minecraft.entity.player.EntityPlayer player)
	{
		return !MinecraftForge.EVENT_BUS.post(new WEntityPlayerJumpEvent(player));
	}

	public static void onGuiInventoryInit(java.util.List buttonList)
	{
		if(buttonList == null)
			return;

		ForgeWurst wurst = ForgeWurst.getForgeWurst();
		if(wurst == null || wurst.getHax() == null
			|| !wurst.getHax().clickGuiHack.isInventoryButton())
			return;

		Minecraft minecraft = Minecraft.getMinecraft();
		GuiScreen screen = minecraft.currentScreen;
		if(screen == null)
			return;

		buttonList.add(new GuiButton(-1, screen.width / 2 - 50,
			screen.height / 2 - 120, 100, 20, "ForgeWurst"));
	}

	public static void onGuiInventoryButtonPress(GuiButton button)
	{
		if(button == null || button.id != -1)
			return;

		ForgeWurst wurst = ForgeWurst.getForgeWurst();
		if(wurst == null || wurst.getGui() == null)
			return;

		wurst.getGui().open();
	}

	public static void onGuiMainMenuInit(GuiScreen screen, java.util.List buttonList)
	{
		if(screen == null || buttonList == null)
			return;

		buttonList.add(new GuiButton(-1, screen.width / 2 - 100,
			screen.height / 4 + 108, 100, 20, "Alt Login"));
	}

	public static void onGuiMainMenuButtonPress(GuiButton button)
	{
		if(button == null || button.id != -1)
			return;

		Minecraft.getMinecraft().displayGuiScreen(
			new GuiAltLogin(Minecraft.getMinecraft().currentScreen));
	}

	public static void onGuiMainMenuDraw(GuiScreen screen, int mouseX,
		int mouseY, float partialTicks)
	{
		if(screen == null)
			return;

		int x = screen.width / 2 - 100;
		int y = screen.height / 4 + 108;
		int width = 200;
		int height = 20;
		boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y
			&& mouseY < y + height;

		int background = hovered ? 0xA0909090 : 0x80606060;
		GuiScreen.drawRect(x, y, x + width, y + height, background);
		GuiScreen.drawRect(x, y, x + width, y + 1, 0xB0FFFFFF);
		GuiScreen.drawRect(x, y + height - 1, x + width, y + height,
			0xB0404040);
		GuiScreen.drawRect(x, y, x + 1, y + height, 0xB0FFFFFF);
		GuiScreen.drawRect(x + width - 1, y, x + width, y + height,
			0xB0404040);

		String text = "Alt Login";
		int textWidth = WMinecraft.getFontRenderer().getStringWidth(text);
		int textX = x + (width - textWidth) / 2;
		int textY = y + 6;
		WMinecraft.getFontRenderer().drawStringWithShadow(text, textX, textY,
			0xFFFFFF);
	}

	public static boolean onGuiMainMenuMouseClicked(GuiScreen screen,
		int mouseX, int mouseY, int mouseButton)
	{
		if(screen == null || mouseButton != 0)
			return false;

		int x = screen.width / 2 - 100;
		int y = screen.height / 4 + 108;
		int width = 200;
		int height = 20;
		if(mouseX < x || mouseX >= x + width || mouseY < y
			|| mouseY >= y + height)
			return false;

		Minecraft.getMinecraft().displayGuiScreen(new GuiAltLogin(screen));
		return true;
	}

	public static Packet onSendPacket(Packet packet)
	{
		if(PacketOutputInterceptor.isBypassActive())
			return packet;

		WPacketOutputEvent event = new WPacketOutputEvent(packet);
		return MinecraftForge.EVENT_BUS.post(event) ? null : event.getPacket();
	}

	public static boolean onReceivePacket(Packet packet)
	{
		return !MinecraftForge.EVENT_BUS.post(new WPacketInputEvent(packet));
	}

	public static boolean shouldSideBeRendered(boolean rendered, Block block)
	{
		WShouldSideBeRenderedEvent event =
			new WShouldSideBeRenderedEvent(block, rendered);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isRendered();
	}

	public static boolean canRenderInPass(boolean rendered, Block block, int pass)
	{
		WCanRenderInPassEvent event = new WCanRenderInPassEvent(block, pass,
			rendered);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isRendered();
	}

	public static float getAmbientOcclusionLightValue(float lightValue,
		Block block)
	{
		WGetAmbientOcclusionLightValueEvent event =
			new WGetAmbientOcclusionLightValueEvent(block, lightValue);
		MinecraftForge.EVENT_BUS.post(event);
		return event.getLightValue();
	}

	public static boolean isNormalCube(boolean normalCube, Block block)
	{
		WIsNormalCubeEvent event = new WIsNormalCubeEvent(block, normalCube);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isNormalCube();
	}

	public static boolean isOpaqueCube(boolean opaqueCube, Block block)
	{
		WIsOpaqueCubeEvent event = new WIsOpaqueCubeEvent(block, opaqueCube);
		MinecraftForge.EVENT_BUS.post(event);
		return event.isOpaqueCube();
	}

	public static boolean shouldLiquidBeSolid(Block block)
	{
		WShouldLiquidBeSolidEvent event =
			new WShouldLiquidBeSolidEvent(block, false);
		MinecraftForge.EVENT_BUS.post(event);

		WGetLiquidCollisionBoxEvent event2 = new WGetLiquidCollisionBoxEvent();
		MinecraftForge.EVENT_BUS.post(event2);

		return event.isSolid() || event2.isSolidCollisionBox();
	}

	public static boolean renderBlockByRenderType(Block block)
	{
		return !MinecraftForge.EVENT_BUS
			.post(new WRenderBlockByRenderTypeEvent(block));
	}

	public static boolean renderBlockModel(Block block)
	{
		return !MinecraftForge.EVENT_BUS.post(new WRenderBlockModelEvent(block));
	}

	public static boolean renderTileEntity(TileEntity tileEntity)
	{
		return !MinecraftForge.EVENT_BUS
			.post(new WRenderTileEntityEvent(tileEntity));
	}

	public static void onPlayerDamageBlock(int x, int y, int z, int side)
	{
		MinecraftForge.EVENT_BUS
			.post(new WPlayerDamageBlockEvent(x, y, z, side));
	}
}
