package rickiewars.guishop.api.gui;

public record MenuConfig(
    int rows,
    int columns,

    int balanceSlot,
    int previousPageSlot,
    int pageIndicatorSlot,
    int nextPageSlot,
    int exitSlot,

    boolean reserveRowForNavigation
){
    public int availableRows() {
        return reserveRowForNavigation ? rows - 1 : rows; // Reserve one row for navigation
    }

    public int availableSlots() {
        return availableRows() * columns;
    }

    public int totalSlots() {
        return rows * columns;
    }
}
