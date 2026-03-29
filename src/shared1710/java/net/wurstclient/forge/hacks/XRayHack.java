/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Collections;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.fmlevents.WGetAmbientOcclusionLightValueEvent;
import net.wurstclient.fmlevents.WCanRenderInPassEvent;
import net.wurstclient.fmlevents.WIsOpaqueCubeEvent;
import net.wurstclient.fmlevents.WIsNormalCubeEvent;
import net.wurstclient.fmlevents.WRenderBlockByRenderTypeEvent;
import net.wurstclient.fmlevents.WRenderBlockModelEvent;
import net.wurstclient.fmlevents.WRenderTileEntityEvent;
import net.wurstclient.fmlevents.WShouldLiquidBeSolidEvent;
import net.wurstclient.fmlevents.WShouldSideBeRenderedEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.settings.BlockListSetting;
import net.wurstclient.forge.utils.BlockUtils;

public final class XRayHack extends Hack
{
	private final BlockListSetting blocks = new BlockListSetting("Blocks",
		Blocks.coal_ore, Blocks.coal_block, Blocks.iron_ore, Blocks.iron_block,
		Blocks.gold_ore, Blocks.gold_block, Blocks.lapis_ore,
		Blocks.lapis_block, Blocks.redstone_ore, Blocks.lit_redstone_ore,
		Blocks.redstone_block, Blocks.diamond_ore, Blocks.diamond_block,
		Blocks.emerald_ore, Blocks.emerald_block, Blocks.quartz_ore,
		Blocks.lava, Blocks.mob_spawner, Blocks.portal, Blocks.end_portal,
		Blocks.end_portal_frame);
	private String blockListSnapshot = "";
	private boolean reloadRenderers;

	public XRayHack()
	{
		super("X-Ray", "Allows you to see ores through walls.");
		setCategory(Category.RENDER);
		addSetting(blocks);
	}

	@Override
	public String getRenderName()
	{
		return "X-Wurst";
	}

	@Override
	protected void onEnable()
	{
		blockListSnapshot = getBlockListSnapshot();
		MinecraftForge.EVENT_BUS.register(this);
		reloadRenderers = true;
		reloadRenderersIfReady();
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);
		if(mc.renderGlobal != null)
			mc.renderGlobal.loadRenderers();
		reloadRenderers = false;

		if(mc.gameSettings != null && !wurst.getHax().fullbrightHack.isEnabled())
			mc.gameSettings.gammaSetting = 0.5F;
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
String currentSnapshot = getBlockListSnapshot();
		if(!currentSnapshot.equals(blockListSnapshot))
		{
			blockListSnapshot = currentSnapshot;
			reloadRenderers = true;
		}

		reloadRenderersIfReady();

		if(mc.gameSettings != null)
			mc.gameSettings.gammaSetting = 16;
	}

	@SubscribeEvent
	public void onShouldSideBeRendered(WShouldSideBeRenderedEvent event)
	{
		event.setRendered(isVisible(event.getBlock()));
	}

	@SubscribeEvent
	public void onGetAmbientOcclusionLightValue(
		WGetAmbientOcclusionLightValueEvent event)
	{
		event.setLightValue(1);
	}

	@SubscribeEvent
	public void onIsNormalCube(WIsNormalCubeEvent event)
	{
		event.setNormalCube(false);
	}

	@SubscribeEvent
	public void onIsOpaqueCube(WIsOpaqueCubeEvent event)
	{
		if(!isVisible(event.getBlock()))
			event.setOpaqueCube(false);
	}

	@SubscribeEvent
	public void onCanRenderInPass(WCanRenderInPassEvent event)
	{
		if(!isVisible(event.getBlock()))
			event.setRendered(false);
	}

	@SubscribeEvent
	public void onShouldLiquidBeSolid(WShouldLiquidBeSolidEvent event)
	{
		event.setSolid(isVisible(event.getBlock()));
	}

	@SubscribeEvent
	public void onRenderBlockByRenderType(WRenderBlockByRenderTypeEvent event)
	{
		if(!isVisible(event.getBlock()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onRenderBlockModel(WRenderBlockModelEvent event)
	{
		if(!isVisible(event.getBlock()))
			event.setCanceled(true);
	}

	@SubscribeEvent
	public void onRenderTileEntity(WRenderTileEntityEvent event)
	{
		TileEntity tileEntity = event.getTileEntity();
		if(tileEntity == null)
			return;

		if(!isVisible(tileEntity.getBlockType()))
			event.setCanceled(true);
	}

	private boolean isVisible(Block block)
	{
		if(block == null)
			return false;

		String name = BlockUtils.getName(block);
		return Collections.binarySearch(blocks.getBlockNames(), name) >= 0;
	}

	private String getBlockListSnapshot()
	{
		return new ArrayList<>(blocks.getBlockNames()).toString();
	}

	private void reloadRenderersIfReady()
	{
		if(!reloadRenderers || mc.renderGlobal == null)
			return;

		mc.renderGlobal.loadRenderers();
		reloadRenderers = false;
	}
}
