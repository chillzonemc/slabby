package gg.mew.slabby.shop;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.*;
import lombok.experimental.Accessors;

@DatabaseTable(tableName = "shop_logs")
@Builder
@Accessors(fluent = true, chain = false)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class SQLiteShopLog implements ShopLog {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true)
    private SQLiteShop shop;

    @DatabaseField(canBeNull = false)
    private Action action;

    @DatabaseField(canBeNull = false)
    private String oldValue;

    @DatabaseField(canBeNull = false)
    private String newValue;

    public static final class SQLiteShopLogBuilder implements ShopLog.Builder {}

}
