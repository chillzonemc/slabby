package gg.mew.slabby.shop;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import gg.mew.slabby.audit.AuditDao;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.UUID;

@Builder
@DatabaseTable(tableName = "shop_owners", daoClass = AuditDao.class)
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class SQLiteShopOwner implements ShopOwner {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private SQLiteShop shop;

    @DatabaseField
    private UUID uniqueId;

    @DatabaseField
    private int share;

    @DatabaseField(canBeNull = false)
    private Date createdOn;

    @DatabaseField
    private Date lastModifiedOn;

    public static final class SQLiteShopOwnerBuilder implements ShopOwner.Builder {}

}
