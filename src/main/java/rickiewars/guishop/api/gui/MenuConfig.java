package rickiewars.guishop.api.gui;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import org.jetbrains.annotations.TestOnly;

import java.util.Map;
import java.util.OptionalInt;

public record MenuConfig<E>(
    ScreenHandlerType<? extends ScreenHandler> handlerType,
    Map<Integer, E> fixedSlots
){
    public int rows() {
        if (handlerType == ScreenHandlerType.GENERIC_9X1) return 1;
        if (handlerType == ScreenHandlerType.GENERIC_9X2) return 2;
        if (handlerType == ScreenHandlerType.GENERIC_9X3) return 3;
        if (handlerType == ScreenHandlerType.GENERIC_9X4) return 4;
        if (handlerType == ScreenHandlerType.GENERIC_9X5) return 5;
        if (handlerType == ScreenHandlerType.GENERIC_9X6) return 6;
        if (handlerType == ScreenHandlerType.GENERIC_3X3) return 3;

        throw new IllegalStateException("Unsupported handler type");
    }

    public int columns() {
        if (handlerType == ScreenHandlerType.GENERIC_9X1) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_9X2) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_9X3) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_9X4) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_9X5) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_9X6) return 9;
        if (handlerType == ScreenHandlerType.GENERIC_3X3) return 3;

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
