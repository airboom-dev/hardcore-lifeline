package com.hardcorelifeline.checklist;

import java.util.List;
import java.util.Locale;

/** One line of a checklist. Every requirement reads only what the player carries. */
public abstract class Requirement
{
	public abstract CheckResult check(List<HeldItem> items);

	static boolean matches(String itemName, String pattern)
	{
		return itemName != null && itemName.toLowerCase(Locale.ROOT).startsWith(pattern.toLowerCase(Locale.ROOT));
	}

	/** "Shark x10" or "equipped: Escape crystal": a named item, optionally a minimum count, optionally worn. */
	static final class Named extends Requirement
	{
		private final String pattern;
		private final int minimum;
		private final boolean mustBeEquipped;

		Named(String pattern, int minimum, boolean mustBeEquipped)
		{
			this.pattern = pattern;
			this.minimum = minimum;
			this.mustBeEquipped = mustBeEquipped;
		}

		@Override
		public CheckResult check(List<HeldItem> items)
		{
			int count = 0;
			for (HeldItem item : items)
			{
				if (matches(item.getName(), pattern) && (!mustBeEquipped || item.isEquipped()))
				{
					count += item.getQuantity();
				}
			}
			String label = pattern + (mustBeEquipped ? " (equipped)" : "") + (minimum > 1 ? " x" + minimum : "");
			String detail = minimum > 1 ? count + " / " + minimum : "";
			return new CheckResult(label, count >= minimum, detail);
		}
	}

	/** "any: Ring of dueling | Games necklace | @teleport": at least one of several items. */
	static final class AnyOf extends Requirement
	{
		private final String label;
		private final List<String> patterns;

		AnyOf(String label, List<String> patterns)
		{
			this.label = label;
			this.patterns = patterns;
		}

		@Override
		public CheckResult check(List<HeldItem> items)
		{
			for (HeldItem item : items)
			{
				for (String pattern : patterns)
				{
					if (matches(item.getName(), pattern))
					{
						return new CheckResult(label, true, item.getName());
					}
				}
			}
			return new CheckResult(label, false, "none");
		}
	}

	/** "food x8": at least N edible items in the inventory (worn items never count). */
	static final class Food extends Requirement
	{
		private final int minimum;

		Food(int minimum)
		{
			this.minimum = minimum;
		}

		@Override
		public CheckResult check(List<HeldItem> items)
		{
			int count = 0;
			for (HeldItem item : items)
			{
				if (item.isEdible() && !item.isEquipped())
				{
					count += item.getQuantity();
				}
			}
			return new CheckResult("Food x" + minimum, count >= minimum, count + " / " + minimum);
		}
	}
}
