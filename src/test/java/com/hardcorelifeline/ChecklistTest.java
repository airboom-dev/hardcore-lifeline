package com.hardcorelifeline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hardcorelifeline.checklist.CheckResult;
import com.hardcorelifeline.checklist.Checklist;
import com.hardcorelifeline.checklist.HeldItem;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ChecklistTest
{
	private static HeldItem inv(String name, int qty)
	{
		return new HeldItem(name, qty, false, false);
	}

	private static HeldItem food(String name, int qty)
	{
		return new HeldItem(name, qty, false, true);
	}

	private static HeldItem worn(String name)
	{
		return new HeldItem(name, 1, true, false);
	}

	@Test
	public void escapeCrystalMustBeWorn()
	{
		Checklist c = Checklist.parse("equipped: Escape crystal");
		assertFalse(c.evaluate(Arrays.asList(inv("Escape crystal", 1))).get(0).isPassed());
		assertTrue(c.evaluate(Arrays.asList(worn("Escape crystal"))).get(0).isPassed());
	}

	@Test
	public void teleportGroupMatchesChargedJewellery()
	{
		Checklist c = Checklist.parse("any: @teleport");
		assertTrue(c.evaluate(Arrays.asList(inv("Ring of dueling(3)", 1))).get(0).isPassed());
		assertTrue(c.evaluate(Arrays.asList(worn("Amulet of glory(4)"))).get(0).isPassed());
		assertFalse(c.evaluate(Arrays.asList(inv("Shark", 5))).get(0).isPassed());
	}

	@Test
	public void foodCountsOnlyEdibleInventoryItems()
	{
		Checklist c = Checklist.parse("food x6");
		List<CheckResult> r = c.evaluate(Arrays.asList(food("Shark", 4), food("Karambwan", 1), inv("Coins", 500)));
		assertFalse(r.get(0).isPassed());
		assertEquals("5 / 6", r.get(0).getDetail());
		assertTrue(c.evaluate(Arrays.asList(food("Shark", 6))).get(0).isPassed());
	}

	@Test
	public void quantitiesAndCaseInsensitivePrefixes()
	{
		Checklist c = Checklist.parse("prayer potion x3");
		assertTrue(c.evaluate(Arrays.asList(inv("Prayer potion(4)", 2), inv("Prayer potion(2)", 1))).get(0).isPassed());
		assertFalse(c.evaluate(Arrays.asList(inv("Prayer potion(4)", 2))).get(0).isPassed());
	}

	@Test
	public void anyOfExplicitNames()
	{
		Checklist c = Checklist.parse("any: Ring of dueling | Games necklace");
		assertTrue(c.evaluate(Arrays.asList(inv("Games necklace(8)", 1))).get(0).isPassed());
		assertEquals("One of: Ring of dueling / Games necklace", c.getRequirements().size() == 1
			? c.evaluate(Arrays.asList(inv("Shark", 1))).get(0).getLabel() : "");
	}

	@Test
	public void commentsSemicolonsAndBadGroups()
	{
		Checklist c = Checklist.parse("# my list\nany: @nope; Shark x2 ;food x1");
		assertEquals(2, c.getRequirements().size());
		assertEquals(1, c.getErrors().size());
	}

	@Test
	public void defaultHardcoreListOnAGoodSetup()
	{
		Checklist c = Checklist.parse("equipped: Escape crystal\nany: @teleport\nfood x6");
		List<CheckResult> r = c.evaluate(Arrays.asList(worn("Escape crystal"), inv("Varrock teleport", 3), food("Monkfish", 8)));
		assertTrue(r.stream().allMatch(CheckResult::isPassed));
	}
}
