# ForgeWurst MC 1.7.10

This module is a compatibility adaptation of ForgeWurst for Minecraft Forge 1.7.10.

It was backported from the newer ForgeWurst codebase rather than written from scratch. The original project and upstream history can be found in the [Wurst-Imperium/ForgeWurst](https://github.com/Wurst-Imperium/ForgeWurst) repositories; this folder contains the 1.7.10-specific port and compatibility layer.

The goal of this branch is practical usability on 1.7.10:

- working Forge mod startup
- ClickGUI and HUD
- keybinds and commands
- config persistence
- a large set of hacks
- a 1.7.10-compatible coremod/ASM layer where needed

This is a real backport, not a perfect clone. Some features are adapted for old Forge/Minecraft internals, and some behaviors differ from newer versions when 1.7.10 does not expose the same hooks.

## Build

Use JDK 8.

```powershell
.\gradlew.bat clean build
```

The runnable JAR is written to:

`ForgeWurst MC 1.7.10\build\libs\ForgeWurst-0.11-MC1.7.10.jar`

## Notes

- The 1.7.10 tree is self-contained and does not depend on the newer shared sources.
- The port preserves the original ForgeWurst structure where practical, but old Forge/FML APIs sometimes require separate compatibility code.
- If you are looking for the main project documentation, use the repository root `README.md`.

## License

This module remains under GPLv3, matching the original project licensing.
