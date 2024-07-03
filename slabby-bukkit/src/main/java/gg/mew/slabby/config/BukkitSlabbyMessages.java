package gg.mew.slabby.config;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import java.util.Date;

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
        final static class BukkitNote implements Note {
            @Comment("Title for the note")
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

        @Comment("Messages for shop client note button")
        private BukkitNote note;

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
    @Accessors(fluent = true, chain = false)
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
    @Accessors(fluent = true, chain = false)
    @Getter
    final static class BukkitLog implements Log {

        @ConfigSerializable
        final static class BukkitBuy implements Buy {

            private String title;
            private String amount;
            private String quantity;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }

            @Override
            public Component quantity(final int quantity) {
                return MiniMessage.miniMessage().deserialize(this.quantity, Formatter.number("quantity", quantity));
            }
        }

        @ConfigSerializable
        final static class BukkitSell implements Sell {

            private String title;
            private String amount;
            private String quantity;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }

            @Override
            public Component quantity(final int quantity) {
                return MiniMessage.miniMessage().deserialize(this.quantity, Formatter.number("quantity", quantity));
            }
        }

        @ConfigSerializable
        final static class BukkitDeposit implements Deposit {

            private String title;

            private String amount;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final int amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitWithdraw implements Withdraw {

            private String title;

            private String amount;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final int amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitLocationChanged implements LocationChanged {

            private String title;
            private String x;
            private String y;
            private String z;
            private String world;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component x(final int x) {
                return MiniMessage.miniMessage().deserialize(this.x, Formatter.number("x", x));
            }

            @Override
            public Component y(final int y) {
                return MiniMessage.miniMessage().deserialize(this.y, Formatter.number("y", y));
            }

            @Override
            public Component z(final int z) {
                return MiniMessage.miniMessage().deserialize(this.z, Formatter.number("z", z));
            }

            @Override
            public Component world(final String world) {
                return MiniMessage.miniMessage().deserialize(this.world, Placeholder.unparsed("world", world));
            }
        }

        @ConfigSerializable
        final static class BukkitLinkedInventory implements LinkedInventory {

            private String title;
            private String x;
            private String y;
            private String z;
            private String world;
            private String removed;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component x(final int x) {
                return MiniMessage.miniMessage().deserialize(this.x, Formatter.number("x", x));
            }

            @Override
            public Component y(final int y) {
                return MiniMessage.miniMessage().deserialize(this.y, Formatter.number("y", y));
            }

            @Override
            public Component z(final int z) {
                return MiniMessage.miniMessage().deserialize(this.z, Formatter.number("z", z));
            }

            @Override
            public Component world(final String world) {
                return MiniMessage.miniMessage().deserialize(this.world, Placeholder.unparsed("world", world));
            }

            @Override
            public Component removed() {
                return MiniMessage.miniMessage().deserialize(this.removed);
            }
        }

        @ConfigSerializable
        final static class BukkitBuyPriceChanged implements BuyPriceChanged {

            private String title;
            private String from;
            private String to;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component from(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.from, Formatter.number("amount", amount));
            }

            @Override
            public Component to(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.to, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitSellPriceChanged implements SellPriceChanged {

            private String title;
            private String from;
            private String to;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component from(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.from, Formatter.number("amount", amount));
            }

            @Override
            public Component to(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.to, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitQuantityChanged implements QuantityChanged {

            private String title;
            private String from;
            private String to;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component from(final int amount) {
                return MiniMessage.miniMessage().deserialize(this.from, Formatter.number("amount", amount));
            }

            @Override
            public Component to(final int amount) {
                return MiniMessage.miniMessage().deserialize(this.to, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitNoteChanged implements NoteChanged {

            private String title;
            private String from;
            private String to;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component from(final String note) {
                return MiniMessage.miniMessage().deserialize(this.from, Placeholder.unparsed("note", note));
            }

            @Override
            public Component to(final String note) {
                return MiniMessage.miniMessage().deserialize(this.to, Placeholder.unparsed("note", note));
            }
        }

        @ConfigSerializable
        final static class BukkitNameChanged implements NameChanged {

            private String title;
            private String from;
            private String to;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component from(final String name) {
                return MiniMessage.miniMessage().deserialize(this.from, Placeholder.unparsed("name", name));
            }

            @Override
            public Component to(final String name) {
                return MiniMessage.miniMessage().deserialize(this.to, Placeholder.unparsed("name", name));
            }
        }

        private BukkitBuy buy;

        private BukkitSell sell;

        private BukkitDeposit deposit;

        private BukkitWithdraw withdraw;

        private BukkitLocationChanged locationChanged;

        private BukkitLinkedInventory linkedInventory;

        private BukkitBuyPriceChanged buyPriceChanged;

        private BukkitSellPriceChanged sellPriceChanged;

        private BukkitQuantityChanged quantityChanged;

        private BukkitNoteChanged noteChanged;

        private BukkitNameChanged nameChanged;

        @Comment("Title for the shop logs interface")
        private String title;

        @Comment("Format for the player name")
        private String player;

        @Comment("Format for dates")
        private String date;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component player(final Component displayName) {
            return MiniMessage.miniMessage().deserialize(this.player, Placeholder.component("player", displayName));
        }

        @Override
        public Component date(final Date date) {
            return MiniMessage.miniMessage().deserialize(this.date, Formatter.date("created_on", date.toInstant()));
        }
    }

    @ConfigSerializable
    @Accessors(fluent = true, chain = false)
    @Getter
    final static class BukkitModify implements Modify {

        @ConfigSerializable
        final static class BukkitNote implements Note {
            @Comment("Title for the note item")
            private String title;

            @Comment("Message for requesting a note")
            private String request;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component request() {
                return MiniMessage.miniMessage().deserialize(this.request);
            }

        }

        @ConfigSerializable
        final static class BukkitBuy implements Buy {
            @Comment("Title for the buy button")
            private String title;

            @Comment("Format for the current buy amount")
            private String amount;

            @Comment("Format for not for sale")
            private String notForSale;

            @Comment("Message for requesting a buy price")
            private String request;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }

            @Override
            public Component notForSale() {
                return MiniMessage.miniMessage().deserialize(this.notForSale);
            }

            @Override
            public Component request() {
                return MiniMessage.miniMessage().deserialize(this.request);
            }
        }

        @ConfigSerializable
        final static class BukkitSell implements Sell {

            @Comment("Title for the sell price button")
            private String title;

            @Comment("Format for the current sell price amount")
            private String amount;

            @Comment("Format for not buying")
            private String notBuying;

            @Comment("Message for requesting a sell price")
            private String request;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final double amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }

            @Override
            public Component notBuying() {
                return MiniMessage.miniMessage().deserialize(this.notBuying);
            }

            @Override
            public Component request() {
                return MiniMessage.miniMessage().deserialize(this.request);
            }
        }

        @ConfigSerializable
        final static class BukkitQuantity implements Quantity {
            @Comment("Format for the quantity button")
            private String title;

            @Comment("Format for amount per transaction")
            private String amount;

            @Comment("Format for amount per transaction description")
            private String description;

            @Comment("Message for requesting a quantity")
            private String request;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final int quantity) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("quantity", quantity));
            }

            @Override
            public Component description() {
                return MiniMessage.miniMessage().deserialize(this.description);
            }

            @Override
            public Component request() {
                return MiniMessage.miniMessage().deserialize(this.request);
            }
        }

        @ConfigSerializable
        final static class BukkitConfirm implements Confirm {
            @Comment("Title for the confirm button")
            private String title;

            @Comment("Format for the save shop description")
            private String description;

            @Comment("Format for the location")
            private String location;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component description() {
                return MiniMessage.miniMessage().deserialize(this.description);
            }

            @Override
            public Component location(final String world, final int x, final int y, final int z) {
                return MiniMessage.miniMessage().deserialize(this.location,
                        Placeholder.unparsed("world", world),
                        Formatter.number("x", x),
                        Formatter.number("y", y),
                        Formatter.number("z", z)
                );
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

        @Comment("Title for the shop modify interface")
        private String title;

        @Comment("Format for click to set")
        private String clickToSet;

        @Comment("Messages for shop modify note button")
        private BukkitNote note;

        @Comment("Messages for shop modify buy button")
        private BukkitBuy buy;

        @Comment("Messages for shop modify sell button")
        private BukkitSell sell;

        @Comment("Messages for shop modify quantity button")
        private BukkitQuantity quantity;

        @Comment("Messages for shop modify confirm button")
        private BukkitConfirm confirm;

        @Comment("Messages for shop modify cancel button")
        private BukkitCancel cancel;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component clickToSet() {
            return MiniMessage.miniMessage().deserialize(this.clickToSet);
        }
    }

    @ConfigSerializable
    @Accessors(fluent = true, chain = false)
    @Getter
    final static class BukkitOwner implements Owner {

        @ConfigSerializable
        final static class BukkitDeposit implements Deposit {
            @Comment("Title for the shop deposit button")
            private String title;

            @Comment("Format for the shift bulk deposit message")
            private String bulk;

            @Override
            public Component title(final Component displayName) {
                return MiniMessage.miniMessage().deserialize(this.title, Placeholder.component("item", displayName));
            }

            @Override
            public Component bulk() {
                return MiniMessage.miniMessage().deserialize(this.bulk);
            }
        }

        @ConfigSerializable
        final static class BukkitWithdraw implements Withdraw {
            @Comment("Title for the shop withdraw button")
            private String title;

            @Comment("Format for the shift bulk withdraw message")
            private String bulk;

            @Override
            public Component title(final Component displayName) {
                return MiniMessage.miniMessage().deserialize(this.title, Placeholder.component("item", displayName));
            }

            @Override
            public Component bulk() {
                return MiniMessage.miniMessage().deserialize(this.bulk);
            }
        }

        @ConfigSerializable
        final static class BukkitChangeRate implements ChangeRate {
            @Comment("Title for the change rate button")
            private String title;

            @Comment("Format for the amount per click message")
            private String amount;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component amount(final int amount) {
                return MiniMessage.miniMessage().deserialize(this.amount, Formatter.number("amount", amount));
            }
        }

        @ConfigSerializable
        final static class BukkitLogs implements Logs {
            @Comment("Title for the shop logs button")
            private String title;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }
        }

        @ConfigSerializable
        @Accessors(fluent = true, chain = false)
        @Getter
        final static class BukkitInventory implements Inventory {

            @ConfigSerializable
            final static class BukkitCancel implements Cancel {
                @Comment("Title for the cancel chest link button")
                private String title;

                @Comment("Format for the cancel chest linking message")
                private String message;

                @Override
                public Component title() {
                    return MiniMessage.miniMessage().deserialize(this.title);
                }

                @Override
                public Component message() {
                    return MiniMessage.miniMessage().deserialize(this.message);
                }

            }

            @Comment("Title for the chest link button")
            private String title;

            @Comment("Format for the chest link description")
            private String description;

            @Comment("Format for the chest link message")
            private String message;

            @Comment("Messages for shop owner inventory-link cancel button")
            private BukkitCancel cancel;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }

            @Override
            public Component description() {
                return MiniMessage.miniMessage().deserialize(this.description);
            }

            @Override
            public Component message() {
                return MiniMessage.miniMessage().deserialize(this.message);
            }

        }

        @ConfigSerializable
        final static class BukkitModify implements Modify {
            @Comment("Title for the modify shop button")
            private String title;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }
        }

        @ConfigSerializable
        final static class BukkitCustomer implements Customer {
            @Comment("Title for the view as customer button")
            private String title;

            @Override
            public Component title() {
                return MiniMessage.miniMessage().deserialize(this.title);
            }
        }

        @Comment("Title for the shop owner interface")
        private String title;

        @Comment("Format for the stock message")
        private String stock;

        @Comment("Format for the stacks message")
        private String stacks;

        @Comment("Messages for shop owner deposit button")
        private BukkitDeposit deposit;

        @Comment("Messages for shop owner deposit button")
        private BukkitWithdraw withdraw;

        @Comment("Messages for shop owner change rate button")
        private BukkitChangeRate changeRate;

        @Comment("Messages for shop owner logs button")
        private BukkitLogs logs;

        @Comment("Messages for shop owner inventory button")
        private BukkitInventory inventory;

        @Comment("Messages for shop owner modify button")
        private BukkitModify modify;

        @Comment("Title for the view as customer button")
        private BukkitCustomer customer;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component stock(final int stock) {
            return MiniMessage.miniMessage().deserialize(this.stock, Formatter.number("stock", stock));
        }

        @Override
        public Component stacks(final int stacks) {
            return MiniMessage.miniMessage().deserialize(this.stacks, Formatter.number("stacks", stacks));
        }

    }

}
