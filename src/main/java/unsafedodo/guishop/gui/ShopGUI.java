package unsafedodo.guishop.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.api.gui.SlotGuiInterface;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import unsafedodo.guishop.GUIShop;
import unsafedodo.guishop.economy.Transaction;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;

import java.util.concurrent.ExecutionException;

public class ShopGUI extends SimpleGui{

    protected static final ScreenHandlerType<?> GUI_LAYOUT = ScreenHandlerType.GENERIC_9X6;
    protected static final int ROWS = 6;
    protected static final int COLUMNS = 9;
    protected static final int ITEM_ROWS = ROWS - 1; // At least one row must be reserved for menu buttons
    protected static final int MENU_OFFSET = 5 * COLUMNS - 1;
    protected static final int TOTAL_SLOTS = ROWS * COLUMNS;
    protected static final int ITEM_SLOTS = ITEM_ROWS * COLUMNS;
    protected static final int PLAYER_BALANCE_SLOT = MENU_OFFSET + 1;
    protected static final int EXIT_BUTTON_SLOT = MENU_OFFSET + 9;

    protected Shop shop;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     * @param shop                  the shop
     *                              will be treated as slots of this gui
     */
    public ShopGUI(ServerPlayerEntity player, Shop shop) throws ExecutionException, InterruptedException {
        super(GUI_LAYOUT, player, false);
        this.shop = shop;
        this.setTitle(Text.of(shop.getName()));

        for(int i = 0; i < TOTAL_SLOTS; i++){
            renderEmptySlot(i);
        }

        this.renderPlayerBalanceSlot();

        this.setSlot(EXIT_BUTTON_SLOT, new GuiElementBuilder()
                .setItem(Items.BARRIER)
                .setName(Text.literal("Exit").setStyle(Style.EMPTY.withItalic(true)))
                .setCallback(((index, clickType, action) -> this.close())));

        for(int i = 0; i < Math.min(shop.getItems().size(), ITEM_SLOTS); i++){
            renderItemSlot(i);
        }
    }

    protected void renderItemSlot(int slotIndex) {
        renderItemSlot(slotIndex, slotIndex);
    }
    protected void renderItemSlot(int slotIndex, int shopItemIndex) {
        ShopItem item = shop.getItems().get(shopItemIndex);
        ItemStack guiItem = new ItemStack(Registries.ITEM.get(Identifier.of(item.itemId())));
        guiItem.applyChanges(item.componentChanges());
        Text name = Text.literal(item.itemName());
        this.setSlot(slotIndex, GuiElementBuilder.from(guiItem)
                .setName(name)
                .setLore(item.getDescriptionAsText())
                .addLoreLine(item.getLoreBuyPrice())
                .addLoreLine(item.getLoreSellPrice())
                .addLoreLine(item.getLoreTradeStackInstruction())
                .setCallback((int index, ClickType clickType, SlotActionType action, SlotGuiInterface gui) -> {
                    ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
                    boolean tradeMany = clickType.shift;

                    if (!cursorStack.isEmpty()) {
                        sellCursorStack(cursorStack, tradeMany);
                    } else if (clickType.isLeft) {
                        buyItem(item, tradeMany);
                    } else if (clickType.isRight) {
                        sellItem(item, tradeMany);
                    }
                }));
    }

    protected void renderEmptySlot(int slotIndex) {
        this.setSlot(slotIndex, new GuiElementBuilder(Items.GRAY_STAINED_GLASS_PANE)
                .setName(Text.empty())
                .setCallback((int index, ClickType clickType, SlotActionType action, SlotGuiInterface gui) -> {
                    ItemStack cursorStack = player.currentScreenHandler.getCursorStack();
                    boolean tradeMany = clickType.shift;

                    if (!cursorStack.isEmpty()) {
                        sellCursorStack(cursorStack, tradeMany);
                    }
                }));
    }

    private void renderPlayerBalanceSlot() {
        try {
            this.setSlot(PLAYER_BALANCE_SLOT, new GuiElementBuilder()
                    .setItem(Items.PLAYER_HEAD)
                    .setName(Text.literal("Your balance: ").setStyle(Style.EMPTY.withItalic(true)).formatted(Formatting.GREEN)
                            .append(Text.literal(String.format(
                                    "%.2f $",
                                    GUIShop.economyService.getBalance(player.getUuid()))
                            ).setStyle(Style.EMPTY.withItalic(true)).formatted(Formatting.YELLOW)))
                    .setSkullOwner(HeadTextures.MONEY_SYMBOL, null, null));
        } catch (ExecutionException | InterruptedException ignored) {}
    }

    private void buyItem(ShopItem item, boolean tradeMany) {
        Transaction transaction = new Transaction(player, shop);
        if (transaction.buyItem(item, tradeMany)) {
            this.renderPlayerBalanceSlot();
        }
    }

    private void sellItem(ShopItem item, boolean tradeMany) {
        Transaction transaction = new Transaction(player, shop);
        if (transaction.sellItem(item, tradeMany)) {
            this.renderPlayerBalanceSlot();
        }
    }

    private void sellCursorStack(ItemStack items, boolean tradeMany) {
        Transaction transaction = new Transaction(player, shop);
        if (transaction.sellStack(items, tradeMany)) {
            this.renderPlayerBalanceSlot();
        }
    }
}


