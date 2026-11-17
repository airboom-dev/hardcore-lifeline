package com.hardcorelifeline;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PanelMode
{
	OFF("Off"),
	ON_ALERT("After a warning, until fixed"),
	ALWAYS("Always");

	private final String displayName;

	@Override
	public String toString()
	{
		return displayName;
	}
}
