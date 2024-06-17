package gg.mew.slabby.shop;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import gg.mew.slabby.dao.ShopDao;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;

@DatabaseTable(tableName = "shops", daoClass = ShopDao.class)
@Builder
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class SQLiteShop implements Shop {

    //TODO: Store money as integer? money * 100 for store, money * 0.01 for retrieve

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false, index = true)
    private String item;

    @DatabaseField(canBeNull = false, uniqueIndexName = "location")
    private int x;

    @DatabaseField(canBeNull = false, uniqueIndexName = "location")
    private int y;

    @DatabaseField(canBeNull = false, uniqueIndexName = "location")
    private int z;

    @DatabaseField(canBeNull = false, uniqueIndexName = "location")
    private String world;

    @DatabaseField(canBeNull = true)
    private Double buyPrice;

    @DatabaseField(canBeNull = true)
    private Double sellPrice;

    @DatabaseField(canBeNull = false)
    private int quantity; //TODO: > 0

    @DatabaseField(canBeNull = false)
    private int stock;

    @DatabaseField(canBeNull = true)
    private String note;

    @DatabaseField(canBeNull = true) //TODO: unique?
    private String name;

    @DatabaseField(canBeNull = true, uniqueIndexName = "inventory_location")
    private Integer inventoryX;

    @DatabaseField(canBeNull = true, uniqueIndexName = "inventory_location")
    private Integer inventoryY;

    @DatabaseField(canBeNull = true, uniqueIndexName = "inventory_location")
    private Integer inventoryZ;

    @DatabaseField(canBeNull = true, uniqueIndexName = "inventory_location")
    private String inventoryWorld;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SQLiteShopOwner> owners;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SQLiteShopLog> logs;

    @DatabaseField(canBeNull = false)
    private Date createdOn;

    @DatabaseField(canBeNull = true)
    private Date lastModifiedOn;

    @Override
    public void inventory(final Integer x, final Integer y, final Integer z, final String world) {
        inventoryX = x;
        inventoryY = y;
        inventoryZ = z;
        inventoryWorld = world;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ShopOwner> owners() {
        return (Collection<ShopOwner>) (Collection<? extends ShopOwner>) this.owners;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ShopLog> logs() {
        return (Collection<ShopLog>) (Collection<? extends ShopLog>) this.logs;
    }

    @Override
    public boolean isOwner(final UUID uniqueId) {
        return this.owners.stream().anyMatch(it -> it.uniqueId().equals(uniqueId));
    }

    public static final class SQLiteShopBuilder implements Shop.Builder {}

}
