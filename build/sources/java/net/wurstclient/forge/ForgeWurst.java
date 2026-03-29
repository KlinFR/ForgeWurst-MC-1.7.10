/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.wurstclient.forge.clickgui.ClickGui;
import net.wurstclient.forge.compatibility.WEventFactory;
import net.wurstclient.forge.compatibility.MainMenuLoginHandler;

@Mod(modid = ForgeWurst.MODID, version = ForgeWurst.VERSION,
	name = "ForgeWurst")
public final class ForgeWurst
{
	public static final String MODID = "forgewurst";
	public static final String VERSION = "0.11";

	@Instance(MODID)
	private static ForgeWurst forgeWurst;

	private Logger logger;
	private boolean obfuscated;
	private Path configFolder;

	private HackList hax;
	private CommandList cmds;
	private KeybindList keybinds;
	private ClickGui gui;

	private IngameHUD hud;
	private CommandProcessor cmdProcessor;
	private KeybindProcessor keybindProcessor;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		if(event.getSide() == Side.SERVER)
			return;

		forgeWurst = this;
		logger = event.getModLog();
		obfuscated = !Boolean.TRUE.equals(
			Launch.blackboard.get("fml.deobfuscatedEnvironment"));

		configFolder = resolveConfigFolder(event);
		try
		{
			Files.createDirectories(configFolder);
		}catch(IOException e)
		{
			throw new RuntimeException("Failed to create config directory", e);
		}

		hax = new HackList(configFolder.resolve("enabled-hacks.json"),
			configFolder.resolve("settings.json"));
		hax.loadEnabledHacks();
		hax.loadSettings();

		cmds = new CommandList();
		cmdProcessor = new CommandProcessor(cmds);
		keybinds = new KeybindList(configFolder.resolve("keybinds.json"));
		keybinds.init();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		if(event.getSide() == Side.SERVER)
			return;

		gui = new ClickGui(hax);
		hud = new IngameHUD(hax);
		keybindProcessor = new KeybindProcessor(hax, keybinds, cmdProcessor);

		WEventFactory.register();
		ClientCommandHandler.instance.registerCommand(cmds.wurstCmd);
		MinecraftForge.EVENT_BUS.register(new MainMenuLoginHandler());
		MinecraftForge.EVENT_BUS.register(hud);
		MinecraftForge.EVENT_BUS.register(cmdProcessor);
		FMLCommonHandler.instance().bus().register(keybindProcessor);

		logger.info("ForgeWurst 1.7.10 initialized. Config folder: {}",
			configFolder);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		if(event.getSide() == Side.SERVER)
			return;

		logger.info("ForgeWurst 1.7.10 ready.");
	}

	public static ForgeWurst getForgeWurst()
	{
		return forgeWurst;
	}

	public Logger getLogger()
	{
		return logger;
	}

	public boolean isObfuscated()
	{
		return obfuscated;
	}

	public Path getConfigFolder()
	{
		return configFolder;
	}

	public HackList getHax()
	{
		return hax;
	}

	public CommandList getCmds()
	{
		return cmds;
	}

	public KeybindList getKeybinds()
	{
		return keybinds;
	}

	public ClickGui getGui()
	{
		return gui;
	}

	public CommandProcessor getCmdProcessor()
	{
		return cmdProcessor;
	}

	private Path resolveConfigFolder(FMLPreInitializationEvent event)
	{
		Path preferred = Minecraft.getMinecraft().mcDataDir.toPath()
			.resolve("wurst1710");
		Path legacy =
			Paths.get(event.getModConfigurationDirectory().getPath(), "wurst1710");

		if(Files.exists(preferred) || !Files.exists(legacy))
			return preferred;

		return legacy;
	}
}
