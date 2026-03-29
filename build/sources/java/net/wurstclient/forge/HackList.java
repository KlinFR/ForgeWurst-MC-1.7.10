/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.wurstclient.forge.compatibility.WHackList;
import net.wurstclient.forge.hacks.AntiSpamHack;
import net.wurstclient.forge.hacks.AutoArmorHack;
import net.wurstclient.forge.hacks.AutoFarmHack;
import net.wurstclient.forge.hacks.AutoFishHack;
import net.wurstclient.forge.hacks.AutoSwimHack;
import net.wurstclient.forge.hacks.AutoToolHack;
import net.wurstclient.forge.hacks.AutoWalkHack;
import net.wurstclient.forge.hacks.AutoSprintHack;
import net.wurstclient.forge.hacks.BunnyHopHack;
import net.wurstclient.forge.hacks.BlinkHack;
import net.wurstclient.forge.hacks.ChestEspHack;
import net.wurstclient.forge.hacks.ClickGuiHack;
import net.wurstclient.forge.hacks.FreecamHack;
import net.wurstclient.forge.hacks.JesusHack;
import net.wurstclient.forge.hacks.FastPlaceHack;
import net.wurstclient.forge.hacks.FastBreakHack;
import net.wurstclient.forge.hacks.FastLadderHack;
import net.wurstclient.forge.hacks.FlightHack;
import net.wurstclient.forge.hacks.FullbrightHack;
import net.wurstclient.forge.hacks.GlideHack;
import net.wurstclient.forge.hacks.ItemEspHack;
import net.wurstclient.forge.hacks.RadarHack;
import net.wurstclient.forge.hacks.RainbowUiHack;
import net.wurstclient.forge.hacks.KillauraHack;
import net.wurstclient.forge.hacks.MobEspHack;
import net.wurstclient.forge.hacks.MobSpawnEspHack;
import net.wurstclient.forge.hacks.NoHurtcamHack;
import net.wurstclient.forge.hacks.NoFallHack;
import net.wurstclient.forge.hacks.NoWebHack;
import net.wurstclient.forge.hacks.NukerHack;
import net.wurstclient.forge.hacks.PlayerEspHack;
import net.wurstclient.forge.hacks.SpiderHack;
import net.wurstclient.forge.hacks.TunnellerHack;
import net.wurstclient.forge.hacks.TimerHack;
import net.wurstclient.forge.hacks.SneakHack;
import net.wurstclient.forge.hacks.XRayHack;
import net.wurstclient.forge.settings.Setting;
import net.wurstclient.forge.utils.JsonUtils;

public final class HackList extends WHackList
{
	public final ClickGuiHack clickGuiHack = register(new ClickGuiHack());
	public final AntiSpamHack antiSpamHack = register(new AntiSpamHack());
	public final AutoArmorHack autoArmorHack = register(new AutoArmorHack());
	public final AutoFarmHack autoFarmHack = register(new AutoFarmHack());
	public final AutoFishHack autoFishHack = register(new AutoFishHack());
	public final FullbrightHack fullbrightHack = register(new FullbrightHack());
	public final RainbowUiHack rainbowUiHack = register(new RainbowUiHack());
	public final KillauraHack killauraHack = register(new KillauraHack());
	public final JesusHack jesusHack = register(new JesusHack());
	public final AutoSprintHack autoSprintHack = register(new AutoSprintHack());
	public final BunnyHopHack bunnyHopHack = register(new BunnyHopHack());
	public final BlinkHack blinkHack = register(new BlinkHack());
	public final FlightHack flightHack = register(new FlightHack());
	public final GlideHack glideHack = register(new GlideHack());
	public final FreecamHack freecamHack = register(new FreecamHack());
	public final SpiderHack spiderHack = register(new SpiderHack());
	public final FastLadderHack fastLadderHack = register(new FastLadderHack());
	public final NoFallHack noFallHack = register(new NoFallHack());
	public final NoHurtcamHack noHurtcamHack = register(new NoHurtcamHack());
	public final NoWebHack noWebHack = register(new NoWebHack());
	public final AutoSwimHack autoSwimHack = register(new AutoSwimHack());
	public final AutoWalkHack autoWalkHack = register(new AutoWalkHack());
	public final AutoToolHack autoToolHack = register(new AutoToolHack());
	public final FastBreakHack fastBreakHack = register(new FastBreakHack());
	public final FastPlaceHack fastPlaceHack = register(new FastPlaceHack());
	public final ChestEspHack chestEspHack = register(new ChestEspHack());
	public final MobSpawnEspHack mobSpawnEspHack = register(new MobSpawnEspHack());
	public final ItemEspHack itemEspHack = register(new ItemEspHack());
	public final RadarHack radarHack = register(new RadarHack());
	public final MobEspHack mobEspHack = register(new MobEspHack());
	public final NukerHack nukerHack = register(new NukerHack());
	public final PlayerEspHack playerEspHack = register(new PlayerEspHack());
	public final TunnellerHack tunnellerHack = register(new TunnellerHack());
	public final XRayHack xRayHack = register(new XRayHack());
	public final TimerHack timerHack = register(new TimerHack());
	public final SneakHack sneakHack = register(new SneakHack());

	private final Path enabledHacksFile;
	private final Path settingsFile;
	private boolean disableSaving;

	public HackList(Path enabledHacksFile, Path settingsFile)
	{
		this.enabledHacksFile = enabledHacksFile;
		this.settingsFile = settingsFile;
	}

	public void loadEnabledHacks()
	{
		JsonArray json;
		try(BufferedReader reader = Files.newBufferedReader(enabledHacksFile))
		{
			json = JsonUtils.jsonParser.parse(reader).getAsJsonArray();
		}catch(NoSuchFileException e)
		{
			saveEnabledHacks();
			return;
		}catch(Exception e)
		{
			System.out.println("Failed to load "
				+ enabledHacksFile.getFileName());
			e.printStackTrace();
			saveEnabledHacks();
			return;
		}

		disableSaving = true;
		for(JsonElement element : json)
		{
			if(!element.isJsonPrimitive()
				|| !element.getAsJsonPrimitive().isString())
				continue;

			Hack hack = get(element.getAsString());
			if(hack == null || !hack.isStateSaved())
				continue;

			hack.setEnabled(true);
		}
		disableSaving = false;

		saveEnabledHacks();
	}

	public void saveEnabledHacks()
	{
		if(disableSaving)
			return;

		JsonArray enabledHacks = new JsonArray();
		for(Hack hack : getRegistry())
			if(hack.isEnabled() && hack.isStateSaved())
				enabledHacks.add(new JsonPrimitive(hack.getName()));

		try(BufferedWriter writer = Files.newBufferedWriter(enabledHacksFile))
		{
			JsonUtils.prettyGson.toJson(enabledHacks, writer);
		}catch(IOException e)
		{
			System.out.println("Failed to save " + enabledHacksFile.getFileName());
			e.printStackTrace();
		}
	}

	public void loadSettings()
	{
		JsonObject json;
		try(BufferedReader reader = Files.newBufferedReader(settingsFile))
		{
			json = JsonUtils.jsonParser.parse(reader).getAsJsonObject();
		}catch(NoSuchFileException e)
		{
			saveSettings();
			return;
		}catch(Exception e)
		{
			System.out.println("Failed to load " + settingsFile.getFileName());
			e.printStackTrace();
			saveSettings();
			return;
		}

		disableSaving = true;
		for(Entry<String, JsonElement> entry : json.entrySet())
		{
			if(!entry.getValue().isJsonObject())
				continue;

			Hack hack = get(entry.getKey());
			if(hack == null)
				continue;

			Map<String, Setting> settings = hack.getSettings();
			for(Entry<String, JsonElement> entry2 : entry.getValue()
				.getAsJsonObject().entrySet())
			{
				String key = entry2.getKey().toLowerCase();
				if(!settings.containsKey(key))
					continue;

				settings.get(key).fromJson(entry2.getValue());
			}
		}
		disableSaving = false;

		saveSettings();
	}

	public void saveSettings()
	{
		if(disableSaving)
			return;

		JsonObject json = new JsonObject();
		for(Hack hack : getRegistry())
		{
			if(hack.getSettings().isEmpty())
				continue;

			JsonObject settings = new JsonObject();
			for(Setting setting : hack.getSettings().values())
				settings.add(setting.getName(), setting.toJson());

			json.add(hack.getName(), settings);
		}

		try(BufferedWriter writer = Files.newBufferedWriter(settingsFile))
		{
			JsonUtils.prettyGson.toJson(json, writer);
		}catch(IOException e)
		{
			System.out.println("Failed to save " + settingsFile.getFileName());
			e.printStackTrace();
		}
	}
}
