package com.hardcorelifeline;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HardcoreLifelinePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HardcoreLifelinePlugin.class);
		RuneLite.main(args);
	}
}
