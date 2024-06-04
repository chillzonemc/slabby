package gg.mew.slabby.shop;

import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.support.ConnectionSource;
import gg.mew.slabby.audit.AuditDao;

import java.sql.SQLException;
import java.util.Collection;

public final class ShopDao extends AuditDao<SQLiteShop> {

    public ShopDao(final ConnectionSource connectionSource, final Class<SQLiteShop> dataClass) throws SQLException {
        super(connectionSource, dataClass);
    }

    @Override
    public int delete(final SQLiteShop data) throws SQLException {
        data.owners().clear();
        return super.delete(data);
    }

    @Override
    public int delete(Collection<SQLiteShop> datas) throws SQLException {
        datas.forEach(it -> it.owners().clear());
        return super.delete(datas);
    }

    @Override
    public int delete(PreparedDelete<SQLiteShop> preparedDelete) throws SQLException {
        return super.delete(preparedDelete);
    }

    @Override
    public int deleteById(Integer integer) throws SQLException {
        return super.deleteById(integer);
    }

    @Override
    public int deleteIds(Collection<Integer> integers) throws SQLException {
        return super.deleteIds(integers);
    }

}
