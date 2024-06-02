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

@DatabaseTable(tableName = "shops", daoClass = AuditDao.class)
@Builder
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class SQLiteShop implements Shop {

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

    @DatabaseField
    private Double buyPrice;

    @DatabaseField
    private Double sellPrice;

    @DatabaseField(canBeNull = false)
    private int quantity;

    @DatabaseField(canBeNull = false)
    private int stock;

    @DatabaseField
    private String note;

    @DatabaseField
    private String name;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<SQLiteShopOwner> owners;

    @SuppressWarnings("unchecked")
    @Override
    public Collection<ShopOwner> owners() {
        return (Collection<ShopOwner>) (Collection<? extends ShopOwner>) this.owners;
    }

    @DatabaseField(canBeNull = false)
    private Date createdOn;

    @DatabaseField
    private Date lastModifiedOn;

    public static final class SQLiteShopBuilder implements Shop.Builder {}

}
