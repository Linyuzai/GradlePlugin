package eason.linyuzai.permissions;

public interface _PermissionRequest {

    void requestPermissions(String[] permissions, int requestCode, _Permission.Callback callback);

}
