package com.iclaude.scheduledrecorder.utils;


import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PermissionsManager {

    // Returns an array of the permissions to ask (not already granted).
    public static String[] checkPermissions(Fragment fragment, String... permissions) {
        // Check permissions.
        List<String> permissionsToAsk = new ArrayList<>();
        for (String permission : permissions) {
            boolean granted = ContextCompat.checkSelfPermission(Objects.requireNonNull(fragment.getActivity()), permission) == PackageManager.PERMISSION_GRANTED;
            if (!granted)
                permissionsToAsk.add(permission);
        }

        String[] arrPermissions = new String[permissionsToAsk.size()];
        arrPermissions = permissionsToAsk.toArray(arrPermissions);

        return arrPermissions;
    }
}
