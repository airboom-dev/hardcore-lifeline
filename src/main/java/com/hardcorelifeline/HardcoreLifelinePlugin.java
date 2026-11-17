package com.hardcorelifeline;

import com.google.inject.Provides;
import com.hardcorelifeline.checklist.CheckResult;
import com.hardcorelifeline.checklist.Checklist;
import com.hardcorelifeline.checklist.HeldItem;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.client.Notifier;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@Slf4j
@PluginDescriptor(
	name = "Hardcore Lifeline",
	description = "Warns you when you leave the bank or enter the Wilderness without your escape crystal, teleport or food",
	tags = {"hardcore", "hcim", "one life", "escape crystal", "checklist", "reminder", "teleport", "wilderness", "safety"}
)
public class HardcoreLifelinePlugin extends Plugin
{
	private static final Color WARNING_RED = new Color(0xE3243F);

	@Inject
	private Client client;

	@Inject
	private HardcoreLifelineConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private Notifier notifier;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private HardcoreLifelineOverlay overlay;

	/** The latest results, read by the overlay. Only written on the client thread. */
	@Getter
	private volatile List<CheckResult> results = Collections.emptyList();
	@Getter
	private volatile Mode shownMode = Mode.HARDCORE_WORLDS;
	@Getter
	private volatile boolean alertActive;

	private boolean inWilderness;
	private boolean checkOnNextTick;
	private String pendingReason;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		inWilderness = false;
		alertActive = false;
		results = Collections.emptyList();
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			inWilderness = client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) == 1;
			refresh();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		results = Collections.emptyList();
		alertActive = false;
		checkOnNextTick = false;
	}

	@Subscribe
	public void onWidgetClosed(WidgetClosed event)
	{
		if (event.getGroupId() == InterfaceID.BANKMAIN && config.onBankClose())
		{
			runCheck("leaving the bank");
		}
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		if (event.getVarbitId() != VarbitID.INSIDE_WILDERNESS)
		{
			return;
		}
		boolean nowInside = event.getValue() == 1;
		boolean entered = nowInside && !inWilderness;
		inWilderness = nowInside;
		if (entered && config.onEnterWilderness())
		{
			runCheck("entering the Wilderness");
		}
		else
		{
			refresh();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN && config.onLogin())
		{
			// Containers are not populated yet on the login frame; check on the first game tick.
			checkOnNextTick = true;
			pendingReason = "logging in";
		}
		else if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			alertActive = false;
			inWilderness = false;
		}
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (checkOnNextTick)
		{
			checkOnNextTick = false;
			runCheck(pendingReason);
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		int id = event.getContainerId();
		if (id == InventoryID.INV || id == InventoryID.WORN)
		{
			refresh();
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (HardcoreLifelineConfig.GROUP.equals(event.getGroup()) && client.getGameState() == GameState.LOGGED_IN)
		{
			refresh();
		}
	}

	/** Re-evaluate silently (keeps the panel current; never posts a warning). */
	private void refresh()
	{
		Mode mode = currentMode();
		List<CheckResult> fresh = checklistFor(mode).evaluate(snapshot());
		shownMode = mode;
		results = fresh;
		if (alertActive && allPassed(fresh))
		{
			alertActive = false;
			if (config.showAllClear())
			{
				chat("All set for " + mode + ".", false);
			}
		}
	}

	/** Evaluate and warn if anything is missing. */
	private void runCheck(String reason)
	{
		Mode mode = currentMode();
		Checklist checklist = checklistFor(mode);
		List<CheckResult> fresh = checklist.evaluate(snapshot());
		shownMode = mode;
		results = fresh;

		List<String> missing = fresh.stream().filter(r -> !r.isPassed()).map(CheckResult::getLabel).collect(Collectors.toList());
		if (missing.isEmpty())
		{
			alertActive = false;
			if (config.showAllClear())
			{
				chat("All set for " + mode + ".", false);
			}
			return;
		}

		alertActive = true;
		String message = "Missing " + String.join(", ", missing) + " (" + mode + ", " + reason + ")";
		log.debug("Hardcore Lifeline warning: {}", message);
		if (config.chatWarning())
		{
			chat(message, true);
		}
		if (config.notifyWarning())
		{
			notifier.notify("Hardcore Lifeline: " + message);
		}
		if (!checklist.getErrors().isEmpty() && config.chatWarning())
		{
			chat("Checklist problem: " + String.join("; ", checklist.getErrors()), true);
		}
	}

	private Mode currentMode()
	{
		if (config.autoWilderness() && inWilderness)
		{
			return Mode.WILDERNESS;
		}
		return config.activeMode();
	}

	private Checklist checklistFor(Mode mode)
	{
		switch (mode)
		{
			case WILDERNESS:
				return Checklist.parse(config.listWilderness());
			case SLAYER:
				return Checklist.parse(config.listSlayer());
			case BOSSING:
				return Checklist.parse(config.listBossing());
			case SKILLING:
				return Checklist.parse(config.listSkilling());
			case CUSTOM:
				return Checklist.parse(config.listCustom());
			case HARDCORE_WORLDS:
			default:
				return Checklist.parse(config.listHardcoreWorlds());
		}
	}

	/** The player's own inventory and worn items. Must run on the client thread. */
	private List<HeldItem> snapshot()
	{
		List<HeldItem> items = new ArrayList<>();
		addContainer(items, client.getItemContainer(InventoryID.INV), false);
		addContainer(items, client.getItemContainer(InventoryID.WORN), true);
		return items;
	}

	private void addContainer(List<HeldItem> out, ItemContainer container, boolean equipped)
	{
		if (container == null)
		{
			return;
		}
		for (Item item : container.getItems())
		{
			if (item == null || item.getId() <= 0 || item.getQuantity() <= 0)
			{
				continue;
			}
			ItemComposition comp = itemManager.getItemComposition(item.getId());
			if (comp.getNote() != -1)
			{
				// Noted items can't be eaten or used to teleport.
				continue;
			}
			out.add(new HeldItem(comp.getName(), item.getQuantity(), equipped, isEdible(comp)));
		}
	}

	private static boolean isEdible(ItemComposition comp)
	{
		String[] actions = comp.getInventoryActions();
		if (actions == null)
		{
			return false;
		}
		for (String action : actions)
		{
			if ("Eat".equalsIgnoreCase(action))
			{
				return true;
			}
		}
		return false;
	}

	private static boolean allPassed(List<CheckResult> list)
	{
		for (CheckResult r : list)
		{
			if (!r.isPassed())
			{
				return false;
			}
		}
		return true;
	}

	private void chat(String text, boolean warning)
	{
		String body = new ChatMessageBuilder()
			.append(warning ? WARNING_RED : new Color(0x2E9E4F), "[Hardcore Lifeline] ")
			.append(ChatColorType.NORMAL)
			.append(text)
			.build();
		chatMessageManager.queue(QueuedMessage.builder()
			.type(ChatMessageType.GAMEMESSAGE)
			.runeLiteFormattedMessage(body)
			.build());
	}

	@Provides
	HardcoreLifelineConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HardcoreLifelineConfig.class);
	}
}
