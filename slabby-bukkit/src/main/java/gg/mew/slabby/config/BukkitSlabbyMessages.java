package gg.mew.slabby.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
@Accessors(fluent = true, chain = false)
@Getter
public final class BukkitSlabbyMessages implements SlabbyMessages {

    @Comment("Messages for the client shop interface")
    private BukkitClient client;

    @Comment("Messages for the create shop interface")
    private BukkitCreate create;

    @Comment("Messages for the destroy shop interface")
    private BukkitDestroy destroy;

    @Comment("Messages for the command block interface item")
    private BukkitCommandBlock commandBlock;

    @Comment("Messages for the shop logs interface")
    private BukkitLog log;

    @Comment("Messages for the modify shop interface")
    private BukkitModify modify;

    @Comment("Messages for the owner shop interface")
    private BukkitOwner owner;

    @ConfigSerializable
    @Accessors(fluent = true, chain = false)
    @Getter
    final static class BukkitClient implements Client {

        @ConfigSerializable
        final static class BukkitBuy implements Buy {

            @Comment("Title for the buy button")
            private String title;

            @Comment("Price for the buy button")
            private String price;

            @Comment("Stock for the buy button")
            private String stock;

            @Comment("Stacks for the buy button")
            private String stacks;

            @Comment("Success buy message")
            private String message;

            @Override
            public Component title(final Component displayName, final int quantity) {
                return MiniMessage.miniMessage().deserialize(this.title, Placeholder.component("item", displayName), Formatter.number("quantity", quantity));
            }

            @Override
            public Component price(final double price) {
                return MiniMessage.miniMessage().deserialize(this.price, Formatter.number("price", price));
            }

            @Override
            public Component stock(final int stock) {
                return MiniMessage.miniMessage().deserialize(this.stock, Formatter.number("stock", stock));
            }

            @Override
            public Component stacks(final int stacks) {
                return MiniMessage.miniMessage().deserialize(this.stacks, Formatter.number("stacks", stacks));
            }

            @Override
            public Component message(final Component displayName, final int quantity, final double buyPrice) {
                return MiniMessage.miniMessage().deserialize(this.message,
                        Placeholder.component("item", displayName),
                        Formatter.number("quantity", quantity),
                        Formatter.number("price", buyPrice));
            }
        }

        @ConfigSerializable
        final static class BukkitSell implements Sell {

            @Comment("Title for the sell button")
            private String title;

            @Comment("Price for the sell button")
            private String price;

            @Comment("Stock for the sell button")
            private String stock;

            @Comment("Stacks for the sell button")
            private String stacks;

            @Comment("Success sell message")
            private String message;

            @Override
            public Component title(final Component displayName, final int quantity) {
                return MiniMessage.miniMessage().deserialize(this.title, Placeholder.component("item", displayName), Formatter.number("quantity", quantity));
            }

            @Override
            public Component price(final double price) {
                return MiniMessage.miniMessage().deserialize(this.price, Formatter.number("price", price));
            }

            @Override
            public Component stock(final int stock) {
                return MiniMessage.miniMessage().deserialize(this.stock, Formatter.number("stock", stock));
            }

            @Override
            public Component stacks(final int stacks) {
                return MiniMessage.miniMessage().deserialize(this.stacks, Formatter.number("stacks", stacks));
            }

            @Override
            public Component message(final Component displayName, final int quantity, final double sellPrice) {
                return MiniMessage.miniMessage().deserialize(this.message,
                        Placeholder.component("item", displayName),
                        Formatter.number("quantity", quantity),
                        Formatter.number("price", sellPrice));
            }
        }

        @ConfigSerializable
        final static class BukkitFunds implements Funds {

            @Comment("Title for the current funds")
            private String title;

            @Comment("Current funds")
            private String balance;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component balance(final double balance) {
                return MiniMessage.miniMessage().deserialize(this.balance, Formatter.number("balance", balance));
            }
        }

        @ConfigSerializable
        final static class BukkitSellersNote implements SellersNote {
            @Comment("Title for the sellers note")
            private String title;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }
        }

        @Comment("Messages for shop client buy button")
        private BukkitBuy buy;

        @Comment("Messages for shop client sell button")
        private BukkitSell sell;

        @Comment("Messages for shop client funds button")
        private BukkitFunds funds;

        @Comment("Messages for shop client sellers note button")
        private BukkitSellersNote sellersNote;

        @Comment("Title for shop client interface")
        private String title;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

    }

    @ConfigSerializable
    final static class BukkitCommandBlock implements CommandBlock {

        @Comment("Title for the command block item")
        private String title;

        @Comment("Owners of the shop")
        private String owners;

        @Comment("Item being sold")
        private String selling;

        @Comment("Buy price of the item being sold")
        private String buyPrice;

        @Comment("Sell price of the item being sold")
        private String sellPrice;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component owners(final String[] names) {
            final var namesString = String.join(", ", names);
            return MiniMessage.miniMessage().deserialize(this.owners, Placeholder.unparsed("names", namesString));
        }

        @Override
        public Component selling(final Component displayName) {
            return MiniMessage.miniMessage().deserialize(this.selling, Placeholder.component("item", displayName));
        }

        @Override
        public Component buyPrice(int quantity, double buyPrice, double eachPrice) {
            return MiniMessage.miniMessage().deserialize(this.buyPrice,
                    Formatter.number("quantity", quantity),
                    Formatter.number("price", buyPrice),
                    Formatter.number("each", eachPrice)
            );
        }

        @Override
        public Component sellPrice(int quantity, double sellPrice, double eachPrice) {
            return MiniMessage.miniMessage().deserialize(this.sellPrice,
                    Formatter.number("quantity", quantity),
                    Formatter.number("price", sellPrice),
                    Formatter.number("each", eachPrice)
            );
        }
    }

    @ConfigSerializable
    final static class BukkitCreate implements Create {

        @Comment("Title for the create shop UI")
        private String title;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }
    }

    @ConfigSerializable
    @Accessors(fluent = true, chain = true)
    @Getter
    final static class BukkitDestroy implements Destroy {

        @ConfigSerializable
        final static class BukkitConfirm implements Confirm {
            @Comment("Title for the destroy shop button")
            private String title;

            @Comment("Warning text for the destroy shop button")
            private String description;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component description() {
                return MiniMessage.miniMessage().deserialize(this.description);
            }

        }

        @ConfigSerializable
        final static class BukkitCancel implements Cancel {
            @Comment("Title for the cancel button")
            private String title;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }
        }

        @Comment("Title for the destroy shop UI")
        private String title;

        @Comment("Messages for shop destroy confirm button")
        private BukkitConfirm confirm;

        @Comment("Messages for shop destroy cancel button")
        private BukkitCancel cancel;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

    }

    @ConfigSerializable
    final static class BukkitLog implements Log {

        @Comment("Title for the shop logs interface")
        private String title;

        @Comment("Format for the player name")
        private String player;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component player(final Component displayName) {
            return MiniMessage.miniMessage().deserialize(this.player, Placeholder.component("player", displayName));
        }
    }

    @ConfigSerializable
    final static class BukkitModify implements Modify {

        @Comment("Title for the shop modify interface")
        private String title;

        @Comment("Title for the sellers note item")
        private String sellersNote;

        @Comment("Message for requesting a note")
        private String requestNote;

        @Comment("Message for requesting a buy price")
        private String requestBuyPrice;

        @Comment("Message for requesting a sell price")
        private String requestSellPrice;

        @Comment("Message for requesting a quantity")
        private String requestQuantity;

        @Comment("Title for the buy price button")
        private String buyPriceTitle;

        @Comment("Format for the current buy price amount")
        private String buyPriceAmount;

        @Comment("Format for click to set")
        private String clickToSet;

        @Comment("Format for not for sale")
        private String notForSale;

        @Comment("Format for not buying")
        private String notBuying;

        @Comment("Title for the sell price button")
        private String sellPriceTitle;

        @Comment("Format for the current sell price amount")
        private String sellPriceAmount;

        @Comment("Format for the quantity button")
        private String quantityTitle;

        @Comment("Format for amount per transaction")
        private String amountPerTransaction;

        @Comment("Format for amount per transaction description")
        private String amountPerTransactionDescription;

        @Comment("Title for the confirm button")
        private String confirmTitle;

        @Comment("Format for the save shop description")
        private String confirmDescription;

        @Comment("Format for the location")
        private String confirmLocation;

        @Comment("Title for the cancel button")
        private String cancelTitle;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component sellersNote() {
            return MiniMessage.miniMessage().deserialize(this.sellersNote);
        }

        @Override
        public Component requestNote() {
            return MiniMessage.miniMessage().deserialize(this.requestNote);
        }

        @Override
        public Component requestBuyPrice() {
            return MiniMessage.miniMessage().deserialize(this.requestBuyPrice);
        }

        @Override
        public Component requestSellPrice() {
            return MiniMessage.miniMessage().deserialize(this.requestSellPrice);
        }

        @Override
        public Component buyPriceTitle() {
            return MiniMessage.miniMessage().deserialize(this.buyPriceTitle);
        }

        @Override
        public Component buyPriceAmount(final double amount) {
            return MiniMessage.miniMessage().deserialize(this.buyPriceAmount, Formatter.number("amount", amount));
        }

        @Override
        public Component clickToSet() {
            return MiniMessage.miniMessage().deserialize(this.clickToSet);
        }

        @Override
        public Component notForSale() {
            return MiniMessage.miniMessage().deserialize(this.notForSale);
        }

        @Override
        public Component notBuying() {
            return MiniMessage.miniMessage().deserialize(this.notBuying);
        }

        @Override
        public Component sellPriceTitle() {
            return MiniMessage.miniMessage().deserialize(this.sellPriceTitle);
        }

        @Override
        public Component sellPriceAmount(final double amount) {
            return MiniMessage.miniMessage().deserialize(this.sellPriceAmount, Formatter.number("amount", amount));
        }

        @Override
        public Component quantityTitle() {
            return MiniMessage.miniMessage().deserialize(this.quantityTitle);
        }

        @Override
        public Component amountPerTransaction(final int quantity) {
            return MiniMessage.miniMessage().deserialize(this.amountPerTransaction, Formatter.number("quantity", quantity));
        }

        @Override
        public Component amountPerTransactionDescription() {
            return MiniMessage.miniMessage().deserialize(this.amountPerTransactionDescription);
        }

        @Override
        public Component requestQuantity() {
            return MiniMessage.miniMessage().deserialize(this.requestQuantity);
        }

        @Override
        public Component confirmTitle() {
            return MiniMessage.miniMessage().deserialize(this.confirmTitle);
        }

        @Override
        public Component confirmDescription() {
            return MiniMessage.miniMessage().deserialize(this.confirmDescription);
        }

        @Override
        public Component confirmLocation(final String world, final int x, final int y, final int z) {
            return MiniMessage.miniMessage().deserialize(this.confirmLocation,
                    Placeholder.unparsed("world", world),
                    Formatter.number("x", x),
                    Formatter.number("y", y),
                    Formatter.number("z", z)
            );
        }

        @Override
        public Component cancelTitle() {
            return MiniMessage.miniMessage().deserialize(this.cancelTitle);
        }
    }

    @ConfigSerializable
    final static class BukkitOwner implements Owner {

        @Comment("Title for the shop owner interface")
        private String title;

        @Comment("Title for the shop deposit button")
        private String depositTitle;

        @Comment("Format for the shift bulk deposit message")
        private String shiftBulkDeposit;

        @Comment("Format for the in stock message")
        private String inStock;

        @Comment("Format for the stacks message")
        private String stacks;

        @Comment("Title for the shop withdraw button")
        private String withdrawTitle;

        @Comment("Format for the shift bulk withdraw message")
        private String shiftBulkWithdraw;

        @Comment("Title for the change rate button")
        private String changeRateTitle;

        @Comment("Format for the amount per click message")
        private String amountPerClick;

        @Comment("Title for the shop logs button")
        private String logsTitle;

        @Comment("Title for the cancel chest link button")
        private String cancelChestLinkTitle;

        @Comment("Format for the cancel chest linking message")
        private String cancelChestLinkMessage;

        @Comment("Title for the chest link button")
        private String chestLinkTitle;

        @Comment("Format for the chest link description")
        private String chestLinkDescription;

        @Comment("Format for the chest link message")
        private String chestLinkMessage;

        @Comment("Title for the modify shop button")
        private String modifyShopTitle;

        @Comment("Title for the view as customer button")
        private String viewAsCustomerTitle;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component depositTitle(final Component displayName) {
            return MiniMessage.miniMessage().deserialize(this.depositTitle, Placeholder.component("item", displayName));
        }

        @Override
        public Component shiftBulkDeposit() {
            return MiniMessage.miniMessage().deserialize(this.shiftBulkDeposit);
        }

        @Override
        public Component inStock(final int stock) {
            return MiniMessage.miniMessage().deserialize(this.inStock, Formatter.number("stock", stock));
        }

        @Override
        public Component stacks(final int stacks) {
            return MiniMessage.miniMessage().deserialize(this.stacks, Formatter.number("stacks", stacks));
        }

        @Override
        public Component withdrawTitle(final Component displayName) {
            return MiniMessage.miniMessage().deserialize(this.withdrawTitle, Placeholder.component("item", displayName));
        }

        @Override
        public Component shiftBulkWithdraw() {
            return MiniMessage.miniMessage().deserialize(this.shiftBulkWithdraw);
        }

        @Override
        public Component changeRateTitle() {
            return MiniMessage.miniMessage().deserialize(this.changeRateTitle);
        }

        @Override
        public Component amountPerClick(final int amount) {
            return MiniMessage.miniMessage().deserialize(this.amountPerClick, Formatter.number("amount", amount));
        }

        @Override
        public Component logsTitle() {
            return MiniMessage.miniMessage().deserialize(this.logsTitle);
        }

        @Override
        public Component cancelChestLinkTitle() {
            return MiniMessage.miniMessage().deserialize(this.cancelChestLinkTitle);
        }

        @Override
        public Component cancelChestLinkingMessage() {
            return MiniMessage.miniMessage().deserialize(this.cancelChestLinkMessage);
        }

        @Override
        public Component chestLinkTitle() {
            return MiniMessage.miniMessage().deserialize(this.chestLinkTitle);
        }

        @Override
        public Component chestLinkDescription() {
            return MiniMessage.miniMessage().deserialize(this.chestLinkDescription);
        }

        @Override
        public Component chestLinkMessage() {
            return MiniMessage.miniMessage().deserialize(this.chestLinkMessage);
        }

        @Override
        public Component modifyShopTitle() {
            return MiniMessage.miniMessage().deserialize(this.modifyShopTitle);
        }

        @Override
        public Component viewAsCustomerTitle() {
            return MiniMessage.miniMessage().deserialize(this.viewAsCustomerTitle);
        }
    }

}
