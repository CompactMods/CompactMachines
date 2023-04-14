package dev.compactmods.machines.datagen.lang;

import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.core.Tooltips;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.forge.shrinking.Shrinking;
import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;

public class RussianLangGenerator extends BaseLangGenerator {
    public RussianLangGenerator(DataGenerator gen) {
        super(gen, "ru_ru");
    }

    @Override
    protected String getMachineTranslation() {
        return "Компактный механизм";
    }

    @Override
    protected String getSizeTranslation(RoomSize size) {
        return switch(size) {
            case TINY -> "Крошечный";
            case SMALL -> "Маленький";
            case NORMAL -> "Средний";
            case LARGE -> "Большой";
            case GIANT -> "Гигантский";
            case MAXIMUM -> "Максимальный";
        };
    }

    @Override
    protected String getDirectionTranslation(Direction dir) {
        return switch(dir) {
            case UP -> "Верх";
            case DOWN -> "Низ";
            case NORTH -> "Север";
            case SOUTH -> "Юг";
            case WEST -> "Запад";
            case EAST -> "Восток";
        };
    }

    @Override
    protected void addTranslations() {
        super.addTranslations();

        // Walls (Solid, Breakable, Tunnel)
        add(Walls.BLOCK_SOLID_WALL.get(), "Прочная стена компактного механизма");
        add(Walls.BLOCK_BREAKABLE_WALL.get(), "Стена компактного механизма");
        add(Tunnels.BLOCK_TUNNEL_WALL.get(), "Прочная стена компактного механизма (с Туннелем)");

        // Basics
        add(Constants.MOD_ID + ".connected_block", "Подключено: %s");
        add(Constants.MOD_ID + ".direction.side", "Сторона: %s");

        // PSD
        add(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), "Персональное сжимающее устройство");

        // Built-In Tunnels
        add(Tunnels.ITEM_TUNNEL.get(), "Предметный туннель");

        // Creative Tabs
        add("itemGroup." + Constants.MOD_ID, "Компактные механизмы");

        // PSD Guide Pages
        add("compactmachines.psd.pages.machines.title", "Компактные механизмы");
        add("compactmachines.psd.pages.machines", "Компактные механизмы являются основной механикой этого мода. " +
                "Они позволяют Вам строить большие комнаты в пространстве размером в один блок, соединённого с внешним миром.\n" +
                "Они бывают разных размеров, начиная от 3x3x3 и заканчивая 13x13x13.\n\nВы можете использовать Туннели, " +
                "чтобы соединять внешние стороны блока с внутренними стенами для транспортировки предметов, жидкостей и т.д." +
                "\n\nВойти в механизм можно, нажав по нему ПКМ Персональным сжимающим устройством. Пожалуйста, используйте " +
                "JEI для просмотра рецептов создания.");

        // JEI Info Page
        // add("jei.compactmachines.machines", "");
        // add("jei.compactmachines.shrinking_device", "");

        // Messages
        addMessage(Messages.CANNOT_ENTER_MACHINE, "Вы возитесь с персональным сжимающим устройством, но безрезультатно. Оно отказывается работать.");
        addMessage(Messages.NO_MACHINE_DATA, "Данные механизма не загружены; сообщите об этом разработчику.");
        addMessage(Messages.ROOM_SPAWNPOINT_SET, "Новая точка возрождения установлена.");

        // Tooltips
        addTooltip(Tooltips.Details.PERSONAL_SHRINKING_DEVICE, "Используется в качестве внутриигровой документации, а также для входа в Компактные механизмы.");
        addTooltip(Tooltips.Details.SOLID_WALL, "Внимание! Неразрушим для игроков без творческого режима!");

        addTooltip(Tooltips.HINT_HOLD_SHIFT, "Удерживайте Shift для просмотра деталей.");
        addTooltip(Tooltips.UNKNOWN_PLAYER_NAME, "Неизвестный игрок");

        addTooltip(Tooltips.Machines.ID, "ID механизма: %s");
        addTooltip(Tooltips.Machines.OWNER, "Владелец: %s");
        addTooltip(Tooltips.Machines.SIZE, "Размер: %1$sx%1$sx%1$s");
        // addTooltip(Tooltips.Machines.BOUND_TO, "Bound to: %1$s");

        addTooltip(Tooltips.TUNNEL_TYPE, "ID типа: %1$s");
        addTooltip(Tooltips.UNKNOWN_TUNNEL_TYPE, "Неизвестный тип канала (%s)");
    }
}
