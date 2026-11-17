package com.hardcorelifeline.checklist;

import lombok.Value;

/**
 * One stack of items the player is carrying, as seen by the checklist.
 * Built from the player's own inventory and worn equipment only.
 */
@Value
public class HeldItem
{
	String name;
	int quantity;
	boolean equipped;
	boolean edible;
}
