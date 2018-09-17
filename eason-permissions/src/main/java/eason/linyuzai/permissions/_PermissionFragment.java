package eason.linyuzai.permissions;

import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class _PermissionFragment extends Fragment implements _PermissionRequest {

    private _Permission.Callback mCallback;

    public static _PermissionFragment newInstance() {
        return new _PermissionFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void requestPermissions(String[] permissions, int requestCode, _Permission.Callback callback) {
        mCallback = callback;
        if (callback == null) return;
        boolean[] result = new boolean[permissions.length];
        boolean allGranted = true;
        boolean needRequest = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), permissions[i])
                        == PackageManager.PERMISSION_GRANTED) {
                    result[i] = true;
                } else {
                    allGranted = false;
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        needRequest = true;
                    }
                }
            }
            if (allGranted) {
                callback.onFinalResult(true);
                callback.onPermissionsResult(permissions, result);
            } else {
                if (needRequest) {
                    requestPermissions(permissions, requestCode);
                } else {
                    callback.onFinalResult(false);
                    callback.onPermissionsResult(permissions, result);
                }
            }
        } else {
            for (int i = 0; i < result.length; i++) {
                result[i] = true;
            }
            callback.onFinalResult(true);
            callback.onPermissionsResult(permissions, result);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mCallback == null) return;
        boolean[] result = new boolean[grantResults.length];
        boolean finalResult = true;
        for (int i = 0; i < grantResults.length; i++) {
            result[i] = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            if (!result[i]) finalResult = false;
        }
        mCallback.onFinalResult(finalResult);
        mCallback.onPermissionsResult(permissions, result);
    }
}
