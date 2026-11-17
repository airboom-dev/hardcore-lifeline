package com.hardcorelifeline;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Mode
{
	HARDCORE_WORLDS("Hardcore Worlds"),
	WILDERNESS("Wilderness"),
	SLAYER("Slayer"),
	BOSSING("Bossing"),
	SKILLING("Skilling"),
	CUSTOM("Custom");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
