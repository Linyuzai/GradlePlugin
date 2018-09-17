package eason.linyuzai.permissions;

import android.app.Activity;
import android.os.Build;

public class _Permission {
    private static final String TAG = "_Permission";
    private static final int REQUEST_CODE = 0;
    private _PermissionRequest mPermissionRequest;

    public static _Permission get(Activity activity) {
        return new _Permission(activity);
    }

    public static _Permission get(android.app.Fragment fragment) {
        return new _Permission(fragment);
    }

    public static _Permission get(android.support.v4.app.Fragment fragment) {
        return new _Permission(fragment);
    }

    public static String[] getPermissionArray(String permissions) {
        return permissions.split(",");
    }

    private _Permission(Activity activity) {
        mPermissionRequest = getPermissionFragment(activity);
    }

    private _Permission(android.app.Fragment fragment) {
        this(fragment.getActivity());
    }

    private _Permission(android.support.v4.app.Fragment fragment) {
        this(fragment.getActivity());
    }

    private _PermissionRequest getPermissionFragment(Activity activity) {
        /*if (activity instanceof android.support.v4.app.FragmentActivity) {
            _PermissionFragmentV4 fragment = (_PermissionFragmentV4) ((android.support.v4.app.FragmentActivity) activity)
                    .getSupportFragmentManager().findFragmentByTag(TAG);
            if (fragment == null) {
                fragment = _PermissionFragmentV4.newInstance();
                android.support.v4.app.FragmentManager fragmentManager = ((android.support.v4.app.FragmentActivity) activity).getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .add(fragment, TAG)
                        .commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            }
            return fragment;
        } else */
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        } else {
            return null;
            //throw new RuntimeException("Must be instanceof FragmentActivity or Api >= 23");
        }*/
        _PermissionFragment fragment = (_PermissionFragment) activity
                .getFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = _PermissionFragment.newInstance();
            android.app.FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(fragment, TAG)
                    .commitAllowingStateLoss();
            fragmentManager.executePendingTransactions();
        }
        return fragment;
    }

    public void requestPermissions(String[] permissions, Callback callback) {
        if (mPermissionRequest == null) return;
        mPermissionRequest.requestPermissions(permissions, REQUEST_CODE, callback);
    }

    public interface Callback {
        void onFinalResult(boolean grant);

        void onPermissionsResult(String[] permissions, boolean[] grants);
    }
}
