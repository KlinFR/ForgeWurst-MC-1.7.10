/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GNU General Public License was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.hacks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.wurstclient.fmlevents.WUpdateEvent;
import net.wurstclient.forge.Category;
import net.wurstclient.forge.Hack;
import net.wurstclient.forge.compatibility.WChunk;
import net.wurstclient.forge.compatibility.WMinecraft;
import net.wurstclient.forge.settings.EnumSetting;
import net.wurstclient.forge.settings.SliderSetting;
import net.wurstclient.forge.settings.SliderSetting.ValueDisplay;
import net.wurstclient.forge.utils.BlockUtils;
import net.wurstclient.forge.utils.RenderUtils;
import net.wurstclient.forge.utils.RotationUtils;

public final class MobSpawnEspHack extends Hack
{
	private final EnumSetting<DrawDistance> drawDistance = new EnumSetting<>(
		"Draw distance", DrawDistance.values(), DrawDistance.D9);
	private final SliderSetting loadingSpeed =
		new SliderSetting("Loading speed", 1, 1, 5, 1, v -> (int)v + "x");

	private final HashMap<Chunk, ChunkScanner> scanners = new HashMap<>();
	private ExecutorService pool;

	public MobSpawnEspHack()
	{
		super("MobSpawnESP",
			"Highlights areas where mobs can spawn.\n" + EnumChatFormatting.YELLOW
				+ "yellow" + EnumChatFormatting.RESET
				+ " - mobs can spawn at night\n" + EnumChatFormatting.RED + "red"
				+ EnumChatFormatting.RESET + " - mobs can always spawn");
		setCategory(Category.RENDER);
		addSetting(drawDistance);
		addSetting(loadingSpeed);
	}

	@Override
	protected void onEnable()
	{
		pool = Executors.newFixedThreadPool(
			Math.max(1, Runtime.getRuntime().availableProcessors()),
			new MinPriorityThreadFactory());
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	protected void onDisable()
	{
		MinecraftForge.EVENT_BUS.unregister(this);

		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
		{
			if(scanner.displayList != 0)
				GL11.glDeleteLists(scanner.displayList, 1);

			scanners.remove(scanner.chunk);
		}

		if(pool != null)
		{
			pool.shutdownNow();
			pool = null;
		}
	}

	@SubscribeEvent
	public void onUpdate(WUpdateEvent event)
	{
WorldClient world = WMinecraft.getWorld();
		EntityPlayerSP player = WMinecraft.getPlayer();
		if(world == null || player == null)
			return;

		int playerX = (int)Math.floor(player.posX);
		int playerZ = (int)Math.floor(player.posZ);
		int chunkX = playerX >> 4;
		int chunkZ = playerZ >> 4;
		int chunkRange = drawDistance.getSelected().chunkRange;

		ArrayList<Chunk> chunks = new ArrayList<>();
		for(int x = chunkX - chunkRange; x <= chunkX + chunkRange; x++)
			for(int z = chunkZ - chunkRange; z <= chunkZ + chunkRange; z++)
				chunks.add(world.getChunkFromChunkCoords(x, z));

		for(Chunk chunk : chunks)
		{
			if(scanners.containsKey(chunk))
				continue;

			ChunkScanner scanner = new ChunkScanner(chunk);
			scanners.put(chunk, scanner);
			scanner.future = pool.submit(() -> scanner.scan());
		}

		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
		{
			if(Math.abs(WChunk.getX(scanner.chunk) - chunkX) <= chunkRange
				&& Math.abs(WChunk.getZ(scanner.chunk) - chunkZ) <= chunkRange)
				continue;

			if(scanner.displayList != 0)
				GL11.glDeleteLists(scanner.displayList, 1);

			if(scanner.future != null)
				scanner.future.cancel(true);

			scanners.remove(scanner.chunk);
		}

		Comparator<ChunkScanner> comparator = Comparator.comparingInt(s -> Math
			.abs(WChunk.getX(s.chunk) - chunkX) + Math.abs(WChunk.getZ(s.chunk)
				- chunkZ));
		List<ChunkScanner> sortedScanners = scanners.values().stream()
			.filter(s -> s.doneScanning).filter(s -> !s.doneCompiling).sorted(comparator)
			.limit(loadingSpeed.getValueI()).collect(Collectors.toList());

		for(ChunkScanner scanner : sortedScanners)
		{
			if(scanner.displayList == 0)
				scanner.displayList = GL11.glGenLists(1);

			scanner.compileDisplayList();
		}
	}

	@SubscribeEvent
	public void onRenderWorldLast(RenderWorldLastEvent event)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glLineWidth(2);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_CULL_FACE);

		GL11.glPushMatrix();
		GL11.glTranslated(-TileEntityRendererDispatcher.staticPlayerX,
			-TileEntityRendererDispatcher.staticPlayerY,
			-TileEntityRendererDispatcher.staticPlayerZ);

		for(ChunkScanner scanner : new ArrayList<>(scanners.values()))
			if(scanner.displayList != 0)
				GL11.glCallList(scanner.displayList);

		GL11.glPopMatrix();

		GL11.glColor4f(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}

	private class ChunkScanner
	{
		private Future<?> future;
		private final Chunk chunk;
		private final Set<BlockCoord> red = new HashSet<>();
		private final Set<BlockCoord> yellow = new HashSet<>();
		private int displayList;
		private boolean doneScanning;
		private boolean doneCompiling;

		private ChunkScanner(Chunk chunk)
		{
			this.chunk = chunk;
		}

		private void scan()
		{
			WorldClient world = WMinecraft.getWorld();
			if(world == null)
				return;

			int minX = WChunk.getX(chunk) << 4;
			int minZ = WChunk.getZ(chunk) << 4;

			for(int x = minX; x < minX + 16; x++)
			{
				for(int z = minZ; z < minZ + 16; z++)
				{
					for(int y = 0; y < 256; y++)
					{
						if(Thread.interrupted())
							return;

						if(BlockUtils.getMaterial(world, x, y, z).blocksMovement())
							continue;

						if(BlockUtils.getBlock(world, x, y, z) instanceof BlockLiquid)
							continue;

						if(!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z))
							continue;

						if(world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 8
							&& world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z) < 8)
							red.add(new BlockCoord(x, y, z));
					}
				}
			}

			for(int x = minX; x < minX + 16; x++)
			{
				for(int z = minZ; z < minZ + 16; z++)
				{
					for(int y = 0; y < 256; y++)
					{
						if(Thread.interrupted())
							return;

						BlockCoord pos = new BlockCoord(x, y, z);
						if(red.contains(pos))
							continue;

						if(BlockUtils.getMaterial(world, x, y, z).blocksMovement())
							continue;

						if(BlockUtils.getBlock(world, x, y, z) instanceof BlockLiquid)
							continue;

						if(!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z))
							continue;

						if(world.getSavedLightValue(EnumSkyBlock.Block, x, y, z) < 8)
							yellow.add(pos);
					}
				}
			}

			doneScanning = true;
		}

		private void compileDisplayList()
		{
			GL11.glNewList(displayList, GL11.GL_COMPILE);

			GL11.glColor4f(1, 0, 0, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			for(BlockCoord pos : red)
			{
				GL11.glVertex3d(pos.x, pos.y + 0.01, pos.z);
				GL11.glVertex3d(pos.x + 1, pos.y + 0.01, pos.z + 1);
				GL11.glVertex3d(pos.x + 1, pos.y + 0.01, pos.z);
				GL11.glVertex3d(pos.x, pos.y + 0.01, pos.z + 1);
			}
			GL11.glEnd();

			GL11.glColor4f(1, 1, 0, 0.5F);
			GL11.glBegin(GL11.GL_LINES);
			for(BlockCoord pos : yellow)
			{
				GL11.glVertex3d(pos.x, pos.y + 0.01, pos.z);
				GL11.glVertex3d(pos.x + 1, pos.y + 0.01, pos.z + 1);
				GL11.glVertex3d(pos.x + 1, pos.y + 0.01, pos.z);
				GL11.glVertex3d(pos.x, pos.y + 0.01, pos.z + 1);
			}
			GL11.glEnd();

			GL11.glEndList();
			doneCompiling = true;
		}

		private void reset()
		{
			if(future != null)
				future.cancel(true);

			red.clear();
			yellow.clear();
			doneScanning = false;
			doneCompiling = false;
		}
	}

	private static class MinPriorityThreadFactory implements ThreadFactory
	{
		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		private MinPriorityThreadFactory()
		{
			SecurityManager s = System.getSecurityManager();
			group = s != null ? s.getThreadGroup()
				: Thread.currentThread().getThreadGroup();
			namePrefix =
				"pool-min-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r)
		{
			Thread t = new Thread(group, r,
				namePrefix + threadNumber.getAndIncrement(), 0);
			if(t.isDaemon())
				t.setDaemon(false);
			if(t.getPriority() != Thread.MIN_PRIORITY)
				t.setPriority(Thread.MIN_PRIORITY);
			return t;
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

	private enum DrawDistance
	{
		D3("3x3 chunks", 1),
		D5("5x5 chunks", 2),
		D7("7x7 chunks", 3),
		D9("9x9 chunks", 4),
		D11("11x11 chunks", 5),
		D13("13x13 chunks", 6),
		D15("15x15 chunks", 7),
		D17("17x17 chunks", 8),
		D19("19x19 chunks", 9),
		D21("21x21 chunks", 10),
		D23("23x23 chunks", 11),
		D25("25x25 chunks", 12);

		private final String name;
		private final int chunkRange;

		private DrawDistance(String name, int chunkRange)
		{
			this.name = name;
			this.chunkRange = chunkRange;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}
}
