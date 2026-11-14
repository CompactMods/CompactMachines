package dev.compactmods.machines.upgrades.api.component;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.RoomUpgradeCodecs;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a set of room components as stored on an item.
 *
 * @param components
 */
public record RoomUpgradeComponentList(List<RoomUpgradeComponent> components) implements TooltipProvider {

    public static final Codec<RoomUpgradeComponentList> CODEC = RoomUpgradeCodecs.DISPATCH_CODEC.listOf()
            .xmap(RoomUpgradeComponentList::new, RoomUpgradeComponentList::components);

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomUpgradeComponentList> STREAM_CODEC = StreamCodec.composite(
            RoomUpgradeCodecs.STREAM_CODEC.apply(ByteBufCodecs.list()), RoomUpgradeComponentList::components,
            RoomUpgradeComponentList::new
    );

    @Override
    public void addToTooltip(Item.TooltipContext ctx, Consumer<Component> tooltips, TooltipFlag flags, DataComponentGetter componentsGetter) {
        tooltips.accept(Component.empty());

        tooltips.accept(Component.translatableWithFallback("cm.components.list_header", "Room Upgrades:")
                .withColor(CommonColors.GRAY)
                .withStyle(s -> s.withUnderlined(true)));

        Consumer<Component> tooltipsWInsert = (comp) -> {
            var reformatted = Component.literal(" - ")
                    .withColor(CommonColors.LIGHT_GRAY)
                    .append(comp.copy())
                    .withStyle(s -> s.withItalic(true));

            tooltips.accept(reformatted);
        };

        for (var upgrade : components)
            upgrade.addToTooltip(ctx, tooltipsWInsert, flags, componentsGetter);
    }
}
