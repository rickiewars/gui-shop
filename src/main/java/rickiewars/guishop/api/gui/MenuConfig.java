package rickiewars.guishop.api.gui;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.TestOnly;

import java.util.Map;
import java.util.OptionalInt;

public record MenuConfig<E>(
    MenuType<? extends AbstractContainerMenu> handlerType,
    Map<Integer, E> fixedSlots
){
    public int rows() {
        if (handlerType == MenuType.GENERIC_9x1) return 1;
        if (handlerType == MenuType.GENERIC_9x2) return 2;
        if (handlerType == MenuType.GENERIC_9x3) return 3;
        if (handlerType == MenuType.GENERIC_9x4) return 4;
        if (handlerType == MenuType.GENERIC_9x5) return 5;
        if (handlerType == MenuType.GENERIC_9x6) return 6;
        if (handlerType == MenuType.GENERIC_3x3) return 3;

        throw new IllegalStateException("Unsupported handler type");
    }

    public int columns() {
        if (handlerType == MenuType.GENERIC_9x1) return 9;
        if (handlerType == MenuType.GENERIC_9x2) return 9;
        if (handlerType == MenuType.GENERIC_9x3) return 9;
        if (handlerType == MenuType.GENERIC_9x4) return 9;
        if (handlerType == MenuType.GENERIC_9x5) return 9;
        if (handlerType == MenuType.GENERIC_9x6) return 9;
        if (handlerType == MenuType.GENERIC_3x3) return 3;

        throw new IllegalStateException("Unsupported handler type");
    }

    public int availableSlots() {
        return totalSlots() - fixedSlots.size();
    }

    public int totalSlots() {
        return rows() * columns();
    }

    @TestOnly
    public OptionalInt indexOf(E key) {
        return fixedSlots.entrySet().stream()
            .filter(e -> e.getValue().equals(key))
            .mapToInt(Map.Entry::getKey)
            .findFirst();
    }
}
