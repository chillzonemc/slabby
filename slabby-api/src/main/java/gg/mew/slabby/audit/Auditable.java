package gg.mew.slabby.audit;

import java.time.LocalDateTime;

public interface Auditable {

    LocalDateTime createdOn();

    void createdOn(final LocalDateTime createdOn);

    LocalDateTime lastModifiedOn();

    void lastModifiedOn(final LocalDateTime lastModifiedOn);

}
