<a href="https://discord.gg/abca3pDPvu" rel="nofollow"><img src="https://img.shields.io/discord/765363477186740234?label=Discord&amp;logo=discord&amp;logoColor=white&amp;style=for-the-badge" alt="Discord"></a>

A Minecraft mod that adds one simple game mechanic: small rooms inside of blocks. You can grab the latest build off [Curseforge] or on [Github Releases].

| Version         | Minecraft Version |      Released | Support | Support Ends   |
| :-------------- | :---------------: | ------------: | :-----: | -------------- |
| 5.2             |      1.19.2       |             - |    ✅    | -              |
| **5.1**         |      1.19.2       |     July 2022 |    ✅    | -              |
| 5.0             |      1.19.2       |     July 2022 |    ❌    | July 2022      |
| **4.5**         |      1.18.2       |     July 2022 |    ❌    | September 2022 |
| 4.4             |      1.18.2       |     July 2022 |    ❌    | -              |
| 4.3             |      1.18.2       |      May 2022 |    ❌    | -              |
| 4.2             |      1.18.2       |    April 2022 |    ❌    | -              |
| **4.0-beta2**   |      1.16.5       |    March 2021 |    ❌    | July 2022      |
| **3.0.19-b278** |      1.12.2       |      May 2019 |    ❌    | 2021           |
| **2.0.1-b141**  |      1.10.2       |  October 2017 |    ❌    | 2019           |
| **1.21**        |      1.7.10       | November 2015 |    ❌    | 2017           |

\* *Note - only the most recent versions are shown here for brevity.* 

Standard support policy is after a new version is released for the current Minecraft version, support for the previous version is dropped. If a new major Minecraft version is released and CM is updated, support for the previous major version is currently ***45*** days.

​

# Contributing

## Prerequisite: Github Packages
First of all, thank you for wanting to help! To get started, you will need to set up authentication for Github Packages. Github has a guide for how to set up authentication here:

https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#authenticating-to-github-packages

It is recommended to create a `gradle.properties` file in your user-level gradle folder to simplify working across multiple repositories. On macOS or Linux, that's `~/.gradle/gradle.properties`, which should look like this:

```
gpr.user=<your github username>
gpr.token=<github personal access token with packages:read scope>
```

## Modules
Compact Machines is split into multiple modules to make updating and version maintenance easier. The following is a quick summary of each module's purpose:

| Module | Description
| --- | ---
| core-api | Contains classes used across all other modules; modloader-independent.
| core | Contains data classes and core functionality not tied to a mod loader.
| tunnels-api | Shipped to end users as a baseline for working with tunnel integration.
| forge-tunnels-api | Contains Forge-specific API code for tunnels. (Mainly capability support)

If you are interested in working with `core-api`, `core`, or `tunnels-api`, you can find them in the [core repository](https://github.com/CompactMods/compactmachines-core).


---

[Curseforge]: https://www.curseforge.com/minecraft/mc-mods/compact-machines
[Github Releases]: https://github.com/CompactMods/CompactMachines/releases