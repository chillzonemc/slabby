package gg.mew.slabby.shop;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import gg.mew.slabby.audit.AuditDao;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Collection;
import java.util.Date;

@DatabaseTable(tableName = "shops", daoClass = ShopDao.class)
@Builder
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class SQLiteShop implements Shop {

    //TODO: Create Index: x, y, z and world
    //TODO: Create Unique Constraint for x, y, z, world

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(canBeNull = false)
    private String item;

    @DatabaseField(canBeNull = false)
    private int x;

    @DatabaseField(canBeNull = false)
    private int y;

    @DatabaseField(canBeNull = false)
    private int z;

    @DatabaseField(canBeNull = false)
    private String world;

    @DatabaseField(canBeNull = true)
    private Double buyPrice;

    @DatabaseField(canBeNull = true)
    private Double sellPrice;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private int stock;

    @DatabaseField(canBeNull = true)
    private String note;

    @DatabaseField(canBeNull = true)
    private String name;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SQLiteShopOwner> owners;

    @DatabaseField(canBeNull = false)
    private Date createdOn;

    @DatabaseField(canBeNull = true)
    private Date lastModifiedOn;

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ShopOwner> owners() {
        return (Collection<ShopOwner>) (Collection<? extends ShopOwner>) this.owners;
    }

    public static final class SQLiteShopBuilder implements Shop.Builder {}

}
