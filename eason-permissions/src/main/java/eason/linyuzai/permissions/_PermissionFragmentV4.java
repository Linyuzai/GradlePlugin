package eason.linyuzai.permissions;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

@Deprecated
public class _PermissionFragmentV4 extends Fragment implements _PermissionRequest {

    private _Permission.Callback mCallback;

    public static _PermissionFragmentV4 newInstance() {
        return new _PermissionFragmentV4();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode, _Permission.Callback callback) {
        mCallback = callback;
        requestPermissions(permissions, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean[] result = new boolean[grantResults.length];
        boolean finalResult = true;
        for (int i = 0; i < grantResults.length; i++) {
            result[i] = grantResults[i] == PackageManager.PERMISSION_GRANTED;
            if (!result[i]) finalResult = false;
        }
        if (mCallback != null) {
            mCallback.onFinalResult(finalResult);
            mCallback.onPermissionsResult(permissions, result);
        }
    }
}
