package gg.mew.slabby.shop;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import gg.mew.slabby.SlabbyAPI;

import java.io.Closeable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

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
    public void close() throws IOException {
        try {
            this.connectionSource.close();
        } catch (Exception e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Shop.Builder shopBuilder() {
        return SQLiteShop.builder();
    }

    @Override
    public ShopOwner.Builder shopOwnerBuilder() {
        return SQLiteShopOwner.builder();
    }

    @Override
    public void create(final Shop shop) {
        try {
            this.shopDao.create((SQLiteShop) shop);
        } catch (SQLException e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void refresh(final Shop shop) {
        try {
            this.shopDao.refresh((SQLiteShop) shop);
        } catch (SQLException e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

    public void create(final Collection<Shop> shops) {
        try {
            this.shopDao.create(shops.stream().map(it -> (SQLiteShop)it).toList());
        } catch (SQLException e) {
            api.exceptionService().log(e);
            throw new RuntimeException(e);
        }
    }

}
