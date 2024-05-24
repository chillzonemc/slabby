package gg.mew.slabby.audit;

import com.j256.ormlite.dao.BaseDaoImpl;
import gg.mew.slabby.SlabbyHelper;

import java.sql.SQLException;
import java.util.Collection;

public final class AuditDao<T extends Auditable> extends BaseDaoImpl<T, Integer> {

    public AuditDao(final Class<T> dataClass) throws SQLException {
        super(dataClass);
    }

    @Override
    public int create(final T data) throws SQLException {
        data.createdOn(SlabbyHelper.api().now());
        return super.create(data);
    }

    @Override
    public int create(final Collection<T> datas) throws SQLException {
        final var now = SlabbyHelper.api().now();
        datas.forEach(it -> it.createdOn(now));
        return super.create(datas);
    }

    @Override
    public synchronized CreateOrUpdateStatus createOrUpdate(final T data) throws SQLException {
        final var now = SlabbyHelper.api().now();

        if (data.createdOn() == null)
            data.createdOn(now);
        else {
            data.lastModifiedOn(now);
        }
        return super.createOrUpdate(data);
    }

    @Override
    public int update(final T data) throws SQLException {
        data.lastModifiedOn(SlabbyHelper.api().now());
        return super.update(data);
    }

}
