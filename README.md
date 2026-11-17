# Hardcore Lifeline

A pre-trip safety checklist for one-life players. Hardcore Lifeline warns you the moment you **leave the bank** or **step into the Wilderness** without the things that keep you alive: your escape crystal, a way out, enough food.

Built for Hardcore Worlds and Hardcore Ironmen, useful for anyone who has ever walked into danger with an empty inventory.

## Features
- **Modes**, each with its own checklist: Hardcore Worlds, Wilderness, Slayer, Bossing, Skilling and Custom.
- **Automatic Wilderness mode**: while you are in the Wilderness, your Wilderness checklist is used.
- **Triggers**: when you close the bank, when you cross into the Wilderness, and (optionally) when you log in.
- **Warnings your way**: chat message, RuneLite desktop notification, and an on-screen checklist panel.
- **Plain-text checklists** you can edit in the plugin settings.

## Writing a checklist
One rule per line (or separate rules with `;`). Item names match from the start and ignore case, so `Ring of dueling` also matches `Ring of dueling(3)`.

| Rule | Meaning |
|---|---|
| `equipped: Escape crystal` | Must be worn |
| `Shark x10` | At least 10, in your inventory or worn |
| `food x6` | At least 6 edible items in your inventory |
| `any: @teleport` | At least one teleport item from the built-in list |
| `any: Ring of dueling \| Games necklace` | At least one of these |
| `# note` | Ignored |

The built-in `@teleport` group covers common charged jewellery, teleport tablets, the escape crystal, the royal seed pod and the ectophial. Spell teleports need runes, so add those yourself, for example `Law rune x1`.

Default Hardcore Worlds list:
```
equipped: Escape crystal
any: @teleport
food x6
```

## What it does not do
Hardcore Lifeline only reads **your own** inventory and worn items. It never blocks a click, never acts for you, has no PvP features, makes no network requests and collects no data.

## Feedback
Ideas and bug reports: [GitHub issues](https://github.com/airboom-dev/hardcore-lifeline/issues) or #plugin-ideas on the [Airboom Discord](https://discord.gg/yTuC9cwgjk).

Made by [Airboom](https://airboom.dev), the Wilderness-only Ironman series. Not affiliated with Jagex.

## Licence
BSD 2-Clause. See `LICENSE`.
