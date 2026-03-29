/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.compatibility.WPlayer;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.PlayerControllerUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class NukerHack extends Hack
{
	private final SliderSetting range =
		new SliderSetting("Range", 5, 1, 6, 0.05, ValueDisplay.DECIMAL);
	private final EnumSetting<Mode> mode =
		new EnumSetting<>("Mode", Mode.values(), Mode.NORMAL);

	private final ArrayDeque<Set<BlockCoord>> prevBlocks = new ArrayDeque<>();
	private BlockCoord currentBlock;
	private float progress;
	private float prevProgress;
	private int id;

	public NukerHack()
	{
		super("Nuker", "Automatically breaks blocks around you.");
		setCategory(Category.BLOCKS);
		addSetting(range);
		addSetting(mode);
	}

	@Override
	public String getRenderName()
	{
		return mode.getSelected().getRenderName(this);
	}

	@Override
	protected void onEnable()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		if(currentBlock != null)
			try
			{
				PlayerControllerUtils.setIsHittingBlock(true);
				mc.playerController.resetBlockRemoving();
				currentBlock = null;
			}catch(ReflectiveOperationException e)
			{
				throw new RuntimeException(e);
			}

		prevBlocks.clear();
		id = 0;
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
EntityPlayerSP player = mc.thePlayer;
		if(player == null || mc.theWorld == null)
			return;

		currentBlock = null;
		Vec3 eyesPos = RotationUtils.getEyesPos();
		double rangeSq = range.getValue() * range.getValue();
		int blockRange = (int)Math.ceil(range.getValue());
		int playerY = (int)Math.floor(player.posY);

		List<BlockCoord> blocks = new ArrayList<>();
		for(int x = (int)Math.floor(eyesPos.xCoord) - blockRange;
			x <= (int)Math.floor(eyesPos.xCoord) + blockRange; x++)
			for(int y = playerY - blockRange; y <= playerY + blockRange; y++)
				for(int z = (int)Math.floor(eyesPos.zCoord) - blockRange;
					z <= (int)Math.floor(eyesPos.zCoord) + blockRange; z++)
				{
					BlockCoord pos = new BlockCoord(x, y, z);
					if(eyesPos.squareDistanceTo(Vec3.createVectorHelper(x + 0.5,
						y + 0.5, z + 0.5)) > rangeSq)
						continue;

					if(!BlockUtils.canBeClicked(mc.theWorld, x, y, z))
						continue;

					if(!mode.getSelected().getValidator(this).test(pos))
						continue;

					blocks.add(pos);
				}

		blocks.sort(Comparator.comparingDouble(pos -> eyesPos.squareDistanceTo(
			Vec3.createVectorHelper(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5))));

		if(player.capabilities.isCreativeMode)
		{
			ArrayList<BlockCoord> blocks2 = new ArrayList<>(blocks);
			for(Set<BlockCoord> set : prevBlocks)
				blocks2.removeIf(set::contains);

			prevBlocks.addLast(new HashSet<>(blocks2));
			while(prevBlocks.size() > 5)
				prevBlocks.removeFirst();

			if(!blocks2.isEmpty())
				currentBlock = blocks2.get(0);

			mc.playerController.resetBlockRemoving();
			progress = 1;
			prevProgress = 1;
			BlockUtils.breakBlocksPacketSpam(mc.theWorld, toIntArrays(blocks2));
			return;
		}

		for(BlockCoord pos : blocks)
			if(BlockUtils.breakBlockSimple(mc.theWorld, pos.x, pos.y, pos.z))
			{
				currentBlock = pos;
				break;
			}

		if(currentBlock == null)
			mc.playerController.resetBlockRemoving();

		if(currentBlock != null
			&& BlockUtils.getHardness(mc.theWorld, currentBlock.x, currentBlock.y,
				currentBlock.z) < 1)
			try
			{
				prevProgress = progress;
				progress = PlayerControllerUtils.getCurBlockDamageMP();

				if(progress < prevProgress)
					prevProgress = progress;
			}catch(ReflectiveOperationException e)
			{
				setEnabled(false);
				throw new RuntimeException(e);
			}
		else
		{
			progress = 1;
			prevProgress = 1;
		}
	}

	@SubscribeEvent
	public void onLeftClickBlock(PlayerInteractEvent event)
	{
		if(event.action != Action.LEFT_CLICK_BLOCK)
			return;

		EntityPlayer player = event.entityPlayer;
		if(player == null || !WPlayer.getWorld(player).isRemote)
			return;

		if(mode.getSelected() == Mode.ID)
			id = BlockUtils.getId(WPlayer.getWorld(player), event.x, event.y,
				event.z);
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		if(currentBlock == null)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glDisable(GL11.GL_DEPTH_TEST);

		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);

		AxisAlignedBB box = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		float p =
			prevProgress + (progress - prevProgress) * event.partialTicks;
		float red = p * 2F;
		float green = 2 - red;

		GL11.glTranslated(currentBlock.x, currentBlock.y, currentBlock.z);
		if(p < 1)
		{
			GL11.glTranslated(0.5, 0.5, 0.5);
			GL11.glScaled(p, p, p);
			GL11.glTranslated(-0.5, -0.5, -0.5);
		}

		GL11.glColor4f(red, green, 0, 0.25F);
		GL11.glBegin(GL11.GL_QUADS);
		RenderUtils.drawSolidBox(box);
		GL11.glEnd();

		GL11.glColor4f(red, green, 0, 0.5F);
		GL11.glBegin(GL11.GL_LINES);
		RenderUtils.drawOutlinedBox(box);
		GL11.glEnd();

		GL11.glPopMatrix();

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	private List<int[]> toIntArrays(List<BlockCoord> blocks)
	{
		List<int[]> result = new ArrayList<>();
		for(BlockCoord pos : blocks)
			result.add(new int[]{pos.x, pos.y, pos.z});
		return result;
	}

	private enum Mode
	{
		NORMAL("Normal", n -> n.getName(), (n, p) -> true),

		ID("ID", n -> "IDNuker [" + n.id + "]",
			(n, p) -> BlockUtils.getId(mc.theWorld, p.x, p.y, p.z) == n.id),

		FLAT("Flat", n -> "FlatNuker", (n, p) -> p.y >= mc.thePlayer.posY),

		SMASH("Smash", n -> "SmashNuker",
			(n, p) -> BlockUtils.getHardness(mc.theWorld, p.x, p.y, p.z) >= 1);

		private final String name;
		private final Function<NukerHack, String> renderName;
		private final BiPredicate<NukerHack, BlockCoord> validator;

		private Mode(String name, Function<NukerHack, String> renderName,
			BiPredicate<NukerHack, BlockCoord> validator)
		{
			this.name = name;
			this.renderName = renderName;
			this.validator = validator;
		}

		@Override
		public String toString()
		{
			return name;
		}

		public String getRenderName(NukerHack n)
		{
			return renderName.apply(n);
		}

		public Predicate<BlockCoord> getValidator(NukerHack n)
		{
			return p -> validator.test(n, p);
		}
	}

	private static final class BlockCoord
	{
		private final int x;
		private final int y;
		private final int z;

		private BlockCoord(int x, int y, int z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(this == obj)
				return true;
			if(!(obj instanceof BlockCoord))
				return false;

			BlockCoord other = (BlockCoord)obj;
			return x == other.x && y == other.y && z == other.z;
		}

		@Override
		public int hashCode()
		{
			int result = Integer.hashCode(x);
			result = 31 * result + Integer.hashCode(y);
			result = 31 * result + Integer.hashCode(z);
			return result;
		}
	}
}
