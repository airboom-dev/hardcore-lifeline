package com.hardcorelifeline;

import com.hardcorelifeline.checklist.CheckResult;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

/** On-screen checklist. Reads cached results only; no game queries per frame. */
class HardcoreLifelineOverlay extends OverlayPanel
{
	private static final Color PASS = new Color(0x2E9E4F);
	private static final Color FAIL = new Color(0xE3243F);

	private final HardcoreLifelinePlugin plugin;
	private final HardcoreLifelineConfig config;

	@Inject
	HardcoreLifelineOverlay(HardcoreLifelinePlugin plugin, HardcoreLifelineConfig config)
	{
		super(plugin);
		this.plugin = plugin;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
		panelComponent.setPreferredSize(new Dimension(215, 0));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		PanelMode panelMode = config.panelMode();
		List<CheckResult> results = plugin.getResults();
		if (panelMode == PanelMode.OFF || results.isEmpty())
		{
			return null;
		}
		if (panelMode == PanelMode.ON_ALERT && !plugin.isAlertActive())
		{
			return null;
		}

		boolean allPassed = results.stream().allMatch(CheckResult::isPassed);
		panelComponent.getChildren().add(TitleComponent.builder()
			.text("Lifeline: " + plugin.getShownMode())
			.color(allPassed ? PASS : FAIL)
			.build());

		for (CheckResult r : results)
		{
			panelComponent.getChildren().add(LineComponent.builder()
				.left((r.isPassed() ? "✓ " : "✗ ") + r.getLabel())
				.leftColor(r.isPassed() ? PASS : FAIL)
				.right(r.getDetail())
				.build());
		}
		return super.render(graphics);
	}
}
