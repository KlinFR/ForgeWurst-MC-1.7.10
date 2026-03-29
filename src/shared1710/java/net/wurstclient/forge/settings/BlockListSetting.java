/*
 * Copyright (C) 2017 - 2019 | Wurst-Imperium | All rights reserved.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.forge.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.wurstclient.forge.clickgui.BlockListEditButton;
import net.wurstclient.forge.clickgui.Component;

public final class BlockListSetting extends Setting
{
	private final ArrayList<String> blockNames = new ArrayList<>();
	private final String[] defaultNames;

	public BlockListSetting(String name, String description, Block... blocks)
	{
		super(name, description);

		for(Block block : blocks)
			addInternal(getName(block));

		defaultNames = blockNames.toArray(new String[0]);
	}

	public BlockListSetting(String name, Block... blocks)
	{
		this(name, null, blocks);
	}

	public List<String> getBlockNames()
	{
		return Collections.unmodifiableList(blockNames);
	}

	public List<Block> getBlocks()
	{
		ArrayList<Block> blocks = new ArrayList<>();
		for(String name : blockNames)
		{
			Block block = Block.getBlockFromName(name);
			if(block != null)
				blocks.add(block);
		}

		return Collections.unmodifiableList(blocks);
	}

	public void add(Block block)
	{
		add(getName(block));
	}

	public void add(String blockName)
	{
		if(addInternal(blockName))
			changed();
	}

	public void remove(int index)
	{
		if(index < 0 || index >= blockNames.size())
			return;

		blockNames.remove(index);
		changed();
	}

	public void resetToDefaults()
	{
		blockNames.clear();
		blockNames.addAll(Arrays.asList(defaultNames));
		changed();
	}

	@Override
	public Component getComponent()
	{
		return new BlockListEditButton(this);
	}

	@Override
	public void fromJson(JsonElement json)
	{
		if(!json.isJsonArray())
			return;

		blockNames.clear();
		for(JsonElement element : json.getAsJsonArray())
		{
			if(!element.isJsonPrimitive()
				|| !element.getAsJsonPrimitive().isString())
				continue;

			addInternal(element.getAsString());
		}
	}

	@Override
	public JsonElement toJson()
	{
		JsonArray json = new JsonArray();
		for(String name : blockNames)
			json.add(new JsonPrimitive(name));
		return json;
	}

	private boolean addInternal(String blockName)
	{
		if(blockName == null)
			return false;

		Block block = Block.getBlockFromName(blockName);
		if(block == null)
			return false;

		String canonicalName = getName(block);
		if(Collections.binarySearch(blockNames, canonicalName) >= 0)
			return false;

		blockNames.add(canonicalName);
		Collections.sort(blockNames);
		return true;
	}

	private static String getName(Block block)
	{
		Object name = Block.blockRegistry.getNameForObject(block);
		return name == null ? null : name.toString();
	}
}
