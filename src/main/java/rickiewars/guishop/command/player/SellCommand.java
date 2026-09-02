package rickiewars.guishop.command.player;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import rickiewars.guishop.GUIShop;
import rickiewars.guishop.api.minecraft.IPlayer;
import rickiewars.guishop.api.minecraft.impl.MinecraftPlayer;
import rickiewars.guishop.command.GuiShopPermission;
import rickiewars.guishop.economy.Transaction;
import rickiewars.guishop.errors.CommandErrors;
import rickiewars.guishop.shop.Shop;
import rickiewars.guishop.shop.ShopItem;

public class SellCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
		dispatcher.register(CommandManager.literal("sell")
				.requires(GuiShopPermission.SELL.require())
				.then(CommandManager.literal("hand")
						.requires(GuiShopPermission.SELL_HAND.require())
						.executes(SellCommand::sellHand))
		);
	}

	private static int sellHand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var mcPlayer = context.getSource().getPlayer();
		if (mcPlayer == null) throw CommandErrors.NEED_PLAYER.create();

		IPlayer player = new MinecraftPlayer(mcPlayer);
        ItemStack itemStack = player.getMainHandStack();

		if (itemStack.isEmpty()) throw CommandErrors.HAND_EMPTY.create();

		for (Shop shop : GUIShop.shops) {
			for (ShopItem shopItem : shop.getItems()) {
				if (shopItem.resembles(itemStack) && shopItem.sellPrice() >= 0) {
					Transaction tx = new Transaction(player, shop);
					ItemStack returnedStack = tx.sellFromItemStack(itemStack, itemStack.getCount());
					player.setMainHandStack(returnedStack);
					return 0;
				}
			}
		}

		throw CommandErrors.ITEM_NOT_SELLABLE.create();
	}
}
