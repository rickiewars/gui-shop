package unsafedodo.guishop.gui;

import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import unsafedodo.guishop.shop.Shop;
import unsafedodo.guishop.shop.ShopItem;

import java.util.concurrent.ExecutionException;

public class PagedShopGUI extends ShopGUI {
    protected int page = 1;
    protected int maxPage;
    public static final int MAX_PAGE_ITEMS = ITEM_SLOTS;
    public static final int PREVIOUS_PAGE_BUTTON_SLOT = MENU_OFFSET + 4;
    public static final int PAGE_NUMBER_SLOT = MENU_OFFSET + 5;
    public static final int NEXT_PAGE_BUTTON_SLOT = MENU_OFFSET + 6;


    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param player                the player to server this gui to
     * @param shop                  the shop opened
     *
     */
    public PagedShopGUI(ServerPlayerEntity player, Shop shop) throws ExecutionException, InterruptedException {
        super(player, shop);
        maxPage = (int) Math.ceil((double) shop.getItems().size()/MAX_PAGE_ITEMS);

        renderEmptySlot(PREVIOUS_PAGE_BUTTON_SLOT);

        this.setSlot(PAGE_NUMBER_SLOT, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal(String.format("Current page: %d", getPage()))
                        .setStyle(Style.EMPTY.withItalic(true))
                            .formatted(Formatting.AQUA))
                .glow()
                .setCount(getPage()));

        this.setSlot(NEXT_PAGE_BUTTON_SLOT, new GuiElementBuilder(Items.PLAYER_HEAD)
                .setSkullOwner(HeadTextures.GUI_NEXT_PAGE, null, null)
                .setName(Text.literal("Next page")
                        .setStyle(Style.EMPTY.withItalic(true))
                            .formatted(Formatting.AQUA))
                .setCallback(((index, type1, action) -> {
                    int oldPage = this.page;
                    this.page = getNextPage();
                    if(oldPage != page)
                        updateGUI();
                })));
    }

    public void updateGUI(){
        int n = MAX_PAGE_ITEMS*(page);

        for (int i = MAX_PAGE_ITEMS*(page-1); i < n; i++) {
            int slotIndex = i-(MAX_PAGE_ITEMS*(page-1));
            if(i < shop.getItems().size()){
                renderItemSlot(slotIndex, i);
            } else {
                renderEmptySlot(slotIndex);
            }
        }

        this.setSlot(PAGE_NUMBER_SLOT, new GuiElementBuilder(Items.PAPER)
                .setName(Text.literal(String.format("Current page: %d", getPage()))
                        .setStyle(Style.EMPTY.withItalic(true))
                        .formatted(Formatting.AQUA))
                .glow()
                .setCount(getPage()));

        if(page == 1){
            renderEmptySlot(PREVIOUS_PAGE_BUTTON_SLOT);
        } else {
            this.setSlot(PREVIOUS_PAGE_BUTTON_SLOT, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(HeadTextures.GUI_PREVIOUS_PAGE, null, null)
                    .setName(Text.literal("Previous page")
                            .setStyle(Style.EMPTY.withItalic(true))
                            .formatted(Formatting.AQUA))
                    .setCallback(((index, type1, action) -> {
                        int oldPage = this.page;
                        this.page = getPreviousPage();
                        if(oldPage != page)
                            updateGUI();
                    })));
        }

        if(page == maxPage){
            renderEmptySlot(NEXT_PAGE_BUTTON_SLOT);
        } else {
            this.setSlot(NEXT_PAGE_BUTTON_SLOT, new GuiElementBuilder(Items.PLAYER_HEAD)
                    .setSkullOwner(HeadTextures.GUI_NEXT_PAGE, null, null)
                    .setName(Text.literal("Next page")
                            .setStyle(Style.EMPTY.withItalic(true))
                            .formatted(Formatting.AQUA))
                    .setCallback(((index, type1, action) -> {
                        int oldPage = this.page;
                        this.page = getNextPage();
                        if(oldPage != page)
                            updateGUI();
                    })));
        }
    }

    public int getPage(){
        return this.page;
    }

    public int getPreviousPage(){
        return Math.max(1, page-1);
    }

    public int getNextPage(){
        return Math.min(page+1, maxPage);
    }
}
