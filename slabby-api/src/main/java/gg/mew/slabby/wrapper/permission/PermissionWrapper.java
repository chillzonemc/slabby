package gg.mew.slabby.wrapper.permission;

import java.util.UUID;

public interface PermissionWrapper {

    boolean hasPermission(final UUID uniqueId, final String permission);

}
