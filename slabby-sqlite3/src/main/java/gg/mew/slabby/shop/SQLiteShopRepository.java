package gg.mew.slabby.shop;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import gg.mew.slabby.SlabbyAPI;

import java.io.Closeable;
import java.sql.SQLException;

public final class SQLiteShopRepository implements ShopRepository, Closeable {

    private final SlabbyAPI api;

    private final ConnectionSource connectionSource;

    private final Dao<SQLiteShop, Integer> shopDao;
    private final Dao<SQLiteShopOwner, Integer> shopOwnerDao;

    public SQLiteShopRepository(final SlabbyAPI api) throws SQLException {
        this.api = api;

        this.connectionSource = new JdbcConnectionSource(api.configuration().database().url());

        this.shopDao = DaoManager.createDao(this.connectionSource, SQLiteShop.class);
        this.shopOwnerDao = DaoManager.createDao(this.connectionSource, SQLiteShopOwner.class);
    }

    public void initialize() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, SQLiteShop.class);
        TableUtils.createTableIfNotExists(this.connectionSource, SQLiteShopOwner.class);
    }

    @Override
    public void close() {
        try {
            this.connectionSource.close();
        } catch (final Exception e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T builder(final Class<?> builderType) {
        if (builderType == Shop.Builder.class)
            return (T) SQLiteShop.builder();

        if (builderType == ShopOwner.Builder.class)
            return (T) SQLiteShopOwner.builder();

        throw new IllegalArgumentException();
    }

    @Override
    public void create(final Shop shop) {
        try {
            this.shopDao.create((SQLiteShop) shop);
            this.shopDao.refresh((SQLiteShop) shop);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create(final ShopOwner shopOwner) {
        try {
            this.shopOwnerDao.create((SQLiteShopOwner) shopOwner);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

}
