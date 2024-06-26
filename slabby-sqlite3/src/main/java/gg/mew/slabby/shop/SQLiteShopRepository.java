package gg.mew.slabby.shop;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.logger.Logger;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import gg.mew.slabby.SlabbyAPI;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

public final class SQLiteShopRepository implements ShopRepository, Closeable {

    private final SlabbyAPI api;

    private final ConnectionSource connectionSource;

    private final Dao<SQLiteShop, Integer> shopDao;
    private final Dao<SQLiteShopOwner, Integer> shopOwnerDao;
    private final Dao<SQLiteShopLog, Integer> shopLogDao;

    public SQLiteShopRepository(final SlabbyAPI api) throws SQLException {
        this.api = api;

        this.connectionSource = new JdbcConnectionSource(api.configuration().database().url());

        this.shopDao = DaoManager.createDao(this.connectionSource, SQLiteShop.class);
        this.shopOwnerDao = DaoManager.createDao(this.connectionSource, SQLiteShopOwner.class);
        this.shopLogDao = DaoManager.createDao(this.connectionSource, SQLiteShopLog.class);

        //TODO: when rendering client side items, this will be very useful
        // this.shopDao.setObjectCache(true);
    }

    public void initialize() throws SQLException {
        TableUtils.createTableIfNotExists(this.connectionSource, SQLiteShop.class);
        TableUtils.createTableIfNotExists(this.connectionSource, SQLiteShopOwner.class);
        TableUtils.createTableIfNotExists(this.connectionSource, SQLiteShopLog.class);
    }

    //TODO: Verify data that goes to the database. Using SQL when possible, otherwise Dao.
    //TODO: Any DB action needs a result that I can act on.

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

        if (builderType == ShopLog.Builder.class)
            return (T) SQLiteShopLog.builder();

        throw new IllegalArgumentException();
    }

    @Override
    public void createOrUpdate(final Shop shop) throws Exception {
        try {
            this.shopDao.createOrUpdate((SQLiteShop) shop);
            //NOTE: Required because the owners collection is not eagerly loaded
            this.shopDao.refresh((SQLiteShop) shop);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void delete(final Shop shop) throws Exception {
        try {
            this.shopDao.delete((SQLiteShop) shop);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void delete(ShopOwner shopOwner) throws Exception {
        try {
            this.shopOwnerDao.delete((SQLiteShopOwner) shopOwner);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void createOrUpdate(final ShopOwner shopOwner) throws Exception {
        try {
            this.shopOwnerDao.createOrUpdate((SQLiteShopOwner) shopOwner);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void update(final Shop shop) throws Exception {
        try {
            this.shopDao.update((SQLiteShop) shop);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void update(final ShopOwner shopOwner) throws Exception {
        try {
            this.shopOwnerDao.update((SQLiteShopOwner) shopOwner);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void refresh(final Shop shop) throws Exception {
        try {
            this.shopDao.refresh((SQLiteShop) shop);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public void refresh(final ShopOwner shopOwner) throws Exception {
        try {
            this.shopOwnerDao.refresh((SQLiteShopOwner) shopOwner);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public Optional<Shop> shopAt(final int x, final int y, final int z, final String world) throws Exception {
        try {
            final var result = this.shopDao.queryBuilder()
                    .where()
                    .eq(Shop.Names.X, x)
                    .and()
                    .eq(Shop.Names.Y, y)
                    .and()
                    .eq(Shop.Names.Z, z)
                    .and()
                    .eq(Shop.Names.WORLD, world)
                    .queryForFirst();
            return Optional.ofNullable(result);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public Optional<Shop> shopWithInventoryAt(final int x, final int y, final int z, final String world) throws Exception {
        try {
            final var result = this.shopDao.queryBuilder()
                    .where()
                    .eq(Shop.Names.INVENTORY_X, x)
                    .and()
                    .eq(Shop.Names.INVENTORY_Y, y)
                    .and()
                    .eq(Shop.Names.INVENTORY_Z, z)
                    .and()
                    .eq(Shop.Names.INVENTORY_WORLD, world)
                    .queryForFirst();
            return Optional.ofNullable(result);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }

    @Override
    public <T> T transaction(final Callable<T> transaction) throws Exception {
        try {
            return TransactionManager.callInTransaction(this.connectionSource, transaction);
        } catch (final SQLException e) {
            api.exceptionService().log(e);
            throw e;
        }
    }
}
