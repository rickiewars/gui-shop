package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.economy.Transaction;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;
import rickiewars.guishop.util.CommonMethods;

public class SellCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
		dispatcher.register(CommandManager.literal("sell")
				.requires(GuiShopPermission.SELL.require())
				.then(CommandManager.literal("hand")
						.requires(GuiShopPermission.SELL_HAND.require())
						.executes(SellCommand::sellHand))
//				.then(CommandManager.literal("all")
//						.requires(GuiShopPermission.SELL_ALL.require())
//						.executes(SellCommand::sellAll))
		);
	}

	private static int sellHand(CommandContext<ServerCommandSource> context) {
		ServerPlayerEntity player = context.getSource().getPlayer();
        assert player != null;
        ItemStack itemStack = player.getMainHandStack();

		if (itemStack.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.literal("You are not holding any item in your main hand.").styled(style -> style.withColor(Formatting.RED)), false);
			return 0;
		}

		for (Shop shop : CommonMethods.getAllShops()) {
			for (ShopItem shopItem : shop.getItems()) {
				if (shopItem.matches(itemStack) && shopItem.sellItemPrice() > 0) {
					Transaction transaction = new Transaction(new MinecraftPlayer(player), shop);
					return transaction.sellStack(itemStack, true) ? 1 : 0;
				}
			}
		}

		context.getSource().sendFeedback(() -> Text.literal("The item you are holding cannot be sold.").styled(style -> style.withColor(Formatting.RED)), false);
		return 0;
	}

	// For now, I don't need this command, but keep it for future reference
//	private static int sellAll(CommandContext<ServerCommandSource> context) {
//		ServerPlayerEntity player = context.getSource().getPlayer();
//		double totalPrice = 0;
//		int totalCount = 0;
//		MutableText hoverText = Text.literal("Breakdown:\n").formatted(Formatting.AQUA);
//
//		for (int i = 0; i < player.getInventory().size(); i++) {
//			ItemStack itemStack = player.getInventory().getStack(i);
//			if (!itemStack.isEmpty()) {
//				for (Shop shop : CommonMethods.getAllShops()) {
//					for (ShopItem shopItem : shop.getItems()) {
//						if (isItemEqual(itemStack, shopItem) && shopItem.getSellItemPrice() > 0) {
//							int count = player.getInventory().count(itemStack.getItem());
//							double price = shopItem.getSellItemPrice() * count;
//							player.getInventory().remove(stack -> isItemEqual(stack, shopItem), count, player.getInventory());
//							totalPrice += price;
//							totalCount += count;
//							hoverText.append(Text.literal(count + " x " + shopItem.getItemName() + " - $" + String.format("%.2f", price) + "\n").formatted(Formatting.GRAY));
//							break;
//						}
//					}
//				}
//			}
//		}
//
//		if (totalCount > 0) {
//			GUIShop.economyService.add(player.getUuid(), totalPrice);
//			MutableText message = Text.literal("Sold " + totalCount + " items for $" + String.format("%.2f", totalPrice))
//					.styled(style -> style.withColor(Formatting.GREEN).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));
//			context.getSource().sendFeedback(() -> message, false);
//		} else {
//			context.getSource().sendFeedback(() -> Text.literal("You don't have any sellable items in your inventory.").styled(style -> style.withColor(Formatting.RED)), false);
//		}
//		return totalCount;
//	}

}
