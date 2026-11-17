package com.hardcorelifeline.checklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;

/**
 * A checklist written as plain text, one rule per line (or separated by ';').
 *
 * <pre>
 * # comments start with '#'
 * equipped: Escape crystal      must be worn
 * Shark x10                     at least 10 (inventory or worn)
 * any: @teleport                at least one item from a built-in group
 * any: Ring of dueling | Games necklace
 * food x6                       at least 6 edible items
 * </pre>
 *
 * Item names match as case-insensitive prefixes, so "Ring of dueling" also matches "Ring of dueling(3)".
 */
public final class Checklist
{
	private static final Pattern COUNT = Pattern.compile("^(.*?)\\s+x\\s*(\\d+)$", Pattern.CASE_INSENSITIVE);

	@Getter
	private final List<Requirement> requirements;
	@Getter
	private final List<String> errors;

	private Checklist(List<Requirement> requirements, List<String> errors)
	{
		this.requirements = Collections.unmodifiableList(requirements);
		this.errors = Collections.unmodifiableList(errors);
	}

	public static Checklist parse(String text)
	{
		List<Requirement> requirements = new ArrayList<>();
		List<String> errors = new ArrayList<>();
		if (text == null)
		{
			return new Checklist(requirements, errors);
		}

		for (String raw : text.split("[\\r\\n;]+"))
		{
			String line = raw.trim();
			if (line.isEmpty() || line.startsWith("#"))
			{
				continue;
			}

			String lower = line.toLowerCase(Locale.ROOT);
			if (lower.startsWith("any:"))
			{
				parseAny(line.substring(4).trim(), requirements, errors);
				continue;
			}

			boolean equipped = false;
			if (lower.startsWith("equipped:") || lower.startsWith("worn:"))
			{
				equipped = true;
				line = line.substring(line.indexOf(':') + 1).trim();
			}

			int minimum = 1;
			Matcher m = COUNT.matcher(line);
			if (m.matches())
			{
				line = m.group(1).trim();
				minimum = Math.max(1, Integer.parseInt(m.group(2)));
			}

			if (line.isEmpty())
			{
				errors.add("Empty item name in: " + raw.trim());
			}
			else if (!equipped && line.equalsIgnoreCase("food"))
			{
				requirements.add(new Requirement.Food(minimum));
			}
			else
			{
				requirements.add(new Requirement.Named(line, minimum, equipped));
			}
		}
		return new Checklist(requirements, errors);
	}

	private static void parseAny(String body, List<Requirement> requirements, List<String> errors)
	{
		int errorsBefore = errors.size();
		List<String> patterns = new ArrayList<>();
		List<String> labelParts = new ArrayList<>();
		for (String part : body.split("\\|"))
		{
			String p = part.trim();
			if (p.isEmpty())
			{
				continue;
			}
			if (p.startsWith("@"))
			{
				List<String> group = ItemGroups.get(p.substring(1));
				if (group == null)
				{
					errors.add("Unknown group " + p);
					continue;
				}
				patterns.addAll(group);
				labelParts.add("any " + p.substring(1).toLowerCase(Locale.ROOT));
			}
			else
			{
				patterns.add(p);
				labelParts.add(p);
			}
		}
		if (patterns.isEmpty())
		{
			if (errors.size() == errorsBefore)
			{
				errors.add("'any:' needs at least one item");
			}
			return;
		}
		String label = labelParts.size() == 1 ? capitalise(labelParts.get(0)) : "One of: " + String.join(" / ", labelParts);
		requirements.add(new Requirement.AnyOf(label, patterns));
	}

	private static String capitalise(String s)
	{
		return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
	}

	public List<CheckResult> evaluate(List<HeldItem> items)
	{
		List<CheckResult> results = new ArrayList<>(requirements.size());
		for (Requirement r : requirements)
		{
			results.add(r.check(items));
		}
		return results;
	}
}
