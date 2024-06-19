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

        @Comment("Messages for shop client buy button")
        private BukkitBuy buy;

        @Comment("Messages for shop client sell button")
        private BukkitSell sell;

        @Comment("Title for shop client interface")
        private String title;

        @Comment("Title for the sellers note")
        private String sellersNote;

        @Comment("Title for the current funds")
        private String currentFundsTitle;

        @Comment("Current funds")
        private String currentFundsBalance;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component sellersNote() {
            return MiniMessage.miniMessage().deserialize(this.sellersNote);
        }

        @Override
        public Component currentFundsTitle() {
            return MiniMessage.miniMessage().deserialize(this.currentFundsTitle);
        }

        @Override
        public Component currentFundsBalance(final double balance) {
            return MiniMessage.miniMessage().deserialize(this.currentFundsBalance, Formatter.number("balance", balance));
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
    final static class BukkitDestroy implements Destroy {

        @Comment("Title for the destroy shop UI")
        private String title;

        @Comment("Title for the destroy shop button")
        private String confirm;

        @Comment("Warning text for the destroy shop button")
        private String confirmWarning;

        @Comment("Title for the cancel button")
        private String cancel;

        @Override
        public Component title() {
            return MiniMessage.miniMessage().deserialize(this.title);
        }

        @Override
        public Component confirm() {
            return MiniMessage.miniMessage().deserialize(this.confirm);
        }

        @Override
        public Component confirmWarning() {
            return MiniMessage.miniMessage().deserialize(this.confirmWarning);
        }

        @Override
        public Component cancel() {
            return MiniMessage.miniMessage().deserialize(this.cancel);
        }
    }

}
