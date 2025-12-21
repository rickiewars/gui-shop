package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import rickiewars.guishop.api.minecraft.IPlayer;
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
		);
	}

	private static int sellHand(CommandContext<ServerCommandSource> context) {
		IPlayer player = new MinecraftPlayer(context.getSource().getPlayer());
        ItemStack itemStack = player.getMainHandStack();

		if (itemStack.isEmpty()) {
			context.getSource().sendFeedback(() -> Text.literal("You are not holding any item in your main hand.").styled(style -> style.withColor(Formatting.RED)), false);
			return 1;
		}

		for (Shop shop : CommonMethods.getAllShops()) {
			for (ShopItem shopItem : shop.getItems()) {
				if (shopItem.matches(itemStack) && shopItem.sellItemPrice() > 0) {
					Transaction tx = new Transaction(player, shop);
					ItemStack returnedStack = tx.sellFromItemStack(itemStack, itemStack.getCount());
					player.setMainHandStack(returnedStack);
					return 0;
				}
			}
		}

		context.getSource().sendFeedback(() -> Text.literal("The item you are holding cannot be sold.").styled(style -> style.withColor(Formatting.RED)), false);
		return 1;
	}
}
