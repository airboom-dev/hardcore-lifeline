package com.hardcorelifeline.checklist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Built-in named groups that a checklist can reference with "@name".
 * Names are matched as case-insensitive prefixes, so "Ring of dueling" also matches "Ring of dueling(8)".
 */
public final class ItemGroups
{
	private static final Map<String, List<String>> GROUPS = new TreeMap<>();

	static
	{
		GROUPS.put("teleport", Collections.unmodifiableList(Arrays.asList(
			// Escape and respawn-style teleports
			"Escape crystal",
			"Royal seed pod",
			"Ectophial",
			// Charged jewellery
			"Ring of dueling",
			"Games necklace",
			"Amulet of glory",
			"Combat bracelet",
			"Skills necklace",
			"Ring of wealth",
			"Burning amulet",
			"Digsite pendant",
			"Necklace of passage",
			"Slayer ring",
			"Ring of the elements",
			"Xeric's talisman",
			"Drakan's medallion",
			"Teleport crystal",
			// Teleport tablets
			"Teleport to house",
			"Varrock teleport",
			"Lumbridge teleport",
			"Falador teleport",
			"Camelot teleport",
			"Ardougne teleport",
			"Watchtower teleport",
			"Kourend castle teleport"
		)));
	}

	private ItemGroups()
	{
	}

	/** Returns the item names in a built-in group, or null if the group does not exist. */
	public static List<String> get(String name)
	{
		return GROUPS.get(name.toLowerCase(Locale.ROOT));
	}

	public static Iterable<String> names()
	{
		return GROUPS.keySet();
	}
}
