package dev.compactmods.machines.api.attachment;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import dev.compactmods.machines.api.room.upgrade.component.RoomUpgradeComponentList;
import dev.compactmods.machines.api.room.upgrade.inventory.RoomUpgradeInventory;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.CommonColors;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public interface CMDataAttachments {

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachines.MOD_ID);

    Supplier<AttachmentType<RoomEntryPoint>> LAST_ROOM_ENTRYPOINT = ATTACHMENT_TYPES.register("last_entrypoint", () -> AttachmentType.builder(() -> RoomEntryPoint.INVALID)
            .serialize(RoomEntryPoint.CODEC)
            .build());

    Supplier<AttachmentType<RoomUpgradeInventory>> UPGRADE_ITEMS = ATTACHMENT_TYPES.register("components", () -> AttachmentType
            .serializable(() -> new RoomUpgradeInventory())
            .build());

    Supplier<AttachmentType<GlobalPos>> OPEN_MACHINE_POS = ATTACHMENT_TYPES.register("open_machine", () -> AttachmentType
            .builder(() -> GlobalPos.of(Level.OVERWORLD, BlockPos.ZERO))
            .serialize(GlobalPos.CODEC, Predicates.alwaysFalse())
            .build());

    Supplier<AttachmentType<RoomUpgradeComponentList>> PERMANENT_UPGRADES = ATTACHMENT_TYPES.register("permanent_upgrades", () -> AttachmentType
            .builder(() -> new RoomUpgradeComponentList(List.of()))
            .serialize(RoomUpgradeComponentList.CODEC)
            .build());

    Supplier<AttachmentType<String>> CURRENT_ROOM_CODE = ATTACHMENT_TYPES.register("current_room_code", () -> AttachmentType
            .<String>builder(() -> null)
            .serialize(Codec.STRING)
            .build());

    Supplier<AttachmentType<UUID>> ROOM_OWNER = ATTACHMENT_TYPES.register("room_owner", () -> AttachmentType
            .builder(() -> Util.NIL_UUID)
            .serialize(UUIDUtil.CODEC)
            .build());

    Supplier<AttachmentType<MachineColor>> MACHINE_COLOR = ATTACHMENT_TYPES.register("machine_color", () -> AttachmentType
            .builder(() -> MachineColor.fromARGB(CommonColors.WHITE))
            .serialize(MachineColor.CODEC)
            .build());

    Supplier<AttachmentType<RoomUpgradeInstance>> UPGRADE_INSTANCE = ATTACHMENT_TYPES.register("room_upgrade_instance", () -> AttachmentType
            .<RoomUpgradeInstance>builder(() -> null)
            .build());

    static void prepare() {
    }

}
