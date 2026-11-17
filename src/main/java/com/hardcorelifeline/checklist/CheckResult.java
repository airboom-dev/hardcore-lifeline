package com.hardcorelifeline.checklist;

import lombok.Value;

/** The outcome of one checklist line. */
@Value
public class CheckResult
{
	/** Short label shown in the panel, e.g. "Escape crystal (equipped)". */
	String label;
	boolean passed;
	/** What the player actually has, e.g. "3 / 6". Empty when not useful. */
	String detail;
}
