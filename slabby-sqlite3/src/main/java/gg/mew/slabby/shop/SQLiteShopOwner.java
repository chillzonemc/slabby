package gg.mew.slabby.shop;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import gg.mew.slabby.audit.AuditDao;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@DatabaseTable(tableName = "shop_owners", daoClass = AuditDao.class)
@Accessors(fluent = true)
@Getter
@Setter
public final class SQLiteShopOwner implements ShopOwner {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private SQLiteShop shop;

    @DatabaseField
    private UUID uniqueId;

    @DatabaseField
    private int share;

    @DatabaseField
    private LocalDateTime createdOn;

    @DatabaseField
    private LocalDateTime lastModifiedOn;

    public static final class SQLiteShopOwnerBuilder implements ShopOwner.Builder {}

}
