package com.hardcorelifeline;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(HardcoreLifelineConfig.GROUP)
public interface HardcoreLifelineConfig extends Config
{
	String GROUP = "hardcorelifeline";

	String HELP = "One rule per line. Examples:<br>"
		+ "equipped: Escape crystal<br>"
		+ "Shark x10<br>"
		+ "any: @teleport<br>"
		+ "any: Ring of dueling | Games necklace<br>"
		+ "food x6<br>"
		+ "Lines starting with # are ignored.";

	@ConfigSection(name = "Mode", description = "Which checklist is active", position = 0)
	String modeSection = "mode";

	@ConfigSection(name = "When to check", description = "Moments that trigger a check", position = 1)
	String triggerSection = "triggers";

	@ConfigSection(name = "Warnings", description = "How you are warned", position = 2)
	String alertSection = "alerts";

	@ConfigSection(name = "Checklists", description = HELP, position = 3, closedByDefault = true)
	String listSection = "lists";

	// ---- Mode ----

	@ConfigItem(keyName = "activeMode", name = "Active mode", description = "The checklist used for checks", section = modeSection, position = 0)
	default Mode activeMode()
	{
		return Mode.HARDCORE_WORLDS;
	}

	@ConfigItem(keyName = "autoWilderness", name = "Use Wilderness mode in the Wilderness",
		description = "While you are inside the Wilderness, check with the Wilderness list instead of the active mode", section = modeSection, position = 1)
	default boolean autoWilderness()
	{
		return true;
	}

	// ---- Triggers ----

	@ConfigItem(keyName = "onBankClose", name = "When I close the bank", description = "Check as you leave the bank", section = triggerSection, position = 0)
	default boolean onBankClose()
	{
		return true;
	}

	@ConfigItem(keyName = "onEnterWilderness", name = "When I enter the Wilderness", description = "Check the moment you cross into the Wilderness", section = triggerSection, position = 1)
	default boolean onEnterWilderness()
	{
		return true;
	}

	@ConfigItem(keyName = "onLogin", name = "When I log in", description = "Check once after logging in", section = triggerSection, position = 2)
	default boolean onLogin()
	{
		return false;
	}

	// ---- Alerts ----

	@ConfigItem(keyName = "chatWarning", name = "Chat message", description = "Post a warning in your chat box", section = alertSection, position = 0)
	default boolean chatWarning()
	{
		return true;
	}

	@ConfigItem(keyName = "notify", name = "Desktop notification", description = "Send a RuneLite notification (uses your RuneLite notification settings)", section = alertSection, position = 1)
	default boolean notifyWarning()
	{
		return true;
	}

	@ConfigItem(keyName = "panelMode", name = "Checklist panel", description = "When to show the on-screen checklist", section = alertSection, position = 2)
	default PanelMode panelMode()
	{
		return PanelMode.ON_ALERT;
	}

	@ConfigItem(keyName = "showAllClear", name = "Say when all is set", description = "Post a short 'all set' message when every rule passes", section = alertSection, position = 3)
	default boolean showAllClear()
	{
		return false;
	}

	// ---- Checklists ----

	@ConfigItem(keyName = "listHardcoreWorlds", name = "Hardcore Worlds", description = HELP, section = listSection, position = 0)
	default String listHardcoreWorlds()
	{
		return "equipped: Escape crystal\nany: @teleport\nfood x6";
	}

	@ConfigItem(keyName = "listWilderness", name = "Wilderness", description = HELP, section = listSection, position = 1)
	default String listWilderness()
	{
		return "any: @teleport\nfood x8";
	}

	@ConfigItem(keyName = "listSlayer", name = "Slayer", description = HELP, section = listSection, position = 2)
	default String listSlayer()
	{
		return "any: @teleport\nfood x4";
	}

	@ConfigItem(keyName = "listBossing", name = "Bossing", description = HELP, section = listSection, position = 3)
	default String listBossing()
	{
		return "any: @teleport\nfood x10";
	}

	@ConfigItem(keyName = "listSkilling", name = "Skilling", description = HELP, section = listSection, position = 4)
	default String listSkilling()
	{
		return "any: @teleport";
	}

	@ConfigItem(keyName = "listCustom", name = "Custom", description = HELP, section = listSection, position = 5)
	default String listCustom()
	{
		return "# Write your own list here\nany: @teleport";
	}
}
