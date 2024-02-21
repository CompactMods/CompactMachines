# Contributing

## Project Setup
- Pull the main branch.
- Initialize the git submodule `core`.
- Let NeoGradle initialize everything.
- Rebuild the main mod source, `neoforge-main`. You should get no errors.
- Check the `Neoforge-main: Client` run configuration.

## Temporary IDE run configuration fix
Update the `MOD_CLASSES` environment variable. Right now there is a NG bug that causes prefixes to use project names, and not read the core project
MANIFEST files correctly. In order to run the game, the generated prefixes for paths must change:

It will generate something like this: (newlines and `PROJECT_ROOT_HERE` added for readability)
```
MOD_CLASSES=
    compactmachines%%PROJECT_ROOT_HERE\neoforge-main\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\neoforge-main\build\classes\java\main\;
    core%%PROJECT_ROOT_HERE\core\core\build\resources\main\;
    core%%PROJECT_ROOT_HERE\core\core\build\classes\java\main\;
    room-api%%PROJECT_ROOT_HERE\core\room-api\build\resources\main\;
    room-api%%PROJECT_ROOT_HERE\core\room-api\build\classes\java\main\;
    room-upgrade-api%%PROJECT_ROOT_HERE\core\room-upgrade-api\build\resources\main\;
    room-upgrade-api%%PROJECT_ROOT_HERE\core\room-upgrade-api\build\classes\java\main\;
    core-api%%PROJECT_ROOT_HERE\core\core-api\build\resources\main\;
    core-api%%PROJECT_ROOT_HERE\core\core-api\build\classes\java\main
```

You must change this to be:
```
MOD_CLASSES=
    compactmachines%%PROJECT_ROOT_HERE\neoforge-main\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\neoforge-main\build\classes\java\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\core\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\core\build\classes\java\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\room-api\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\room-api\build\classes\java\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\room-upgrade-api\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\room-upgrade-api\build\classes\java\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\core-api\build\resources\main\;
    compactmachines%%PROJECT_ROOT_HERE\core\core-api\build\classes\java\main
```

It is recommended to duplicate this profile after fixing it so it is not overridden accidentally.
