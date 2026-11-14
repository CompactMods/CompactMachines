package dev.compactmods.machines.machine.client;

import net.neoforged.neoforge.common.ModConfigSpec;

/// Machine-render client settings (the Pride shader toggle + default flag).
///
/// The values are owned by the mod's client config spec but consumed here by
/// {@code MachineShaderRenderer}. Holding references in :machines — populated by
/// {@code dev.compactmods.machines.client.config.ClientConfig} once the spec is
/// built — keeps :machines from depending upward on :compactmachines' config
/// class while leaving the user-facing config file unchanged.
public final class MachineClientConfig {
    private MachineClientConfig() {}

    public static ModConfigSpec.BooleanValue ENABLE_PRIDE;
    public static ModConfigSpec.ConfigValue<String> DEFAULT_PRIDE_FLAG;
}
