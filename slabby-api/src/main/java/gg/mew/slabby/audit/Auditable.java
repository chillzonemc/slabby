package gg.mew.slabby.audit;

import java.util.Date;

public interface Auditable {

    Date createdOn();

    void createdOn(final Date createdOn);

    Date lastModifiedOn();

    void lastModifiedOn(final Date lastModifiedOn);

}
