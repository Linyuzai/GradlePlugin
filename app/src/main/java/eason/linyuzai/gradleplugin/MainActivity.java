package eason.linyuzai.gradleplugin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;

import eason.linyuzai.permissions.EasonPermissions;
import eason.linyuzai.permissions._Permission;

public class MainActivity extends AppCompatActivity {
    //implements _Permission.Callback {

    int _i = 1;

    //boolean needRequest = true;

    //_Permission _permission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Manifest.permission.ACCESS_CHECKIN_PROPERTIES;
        //_permission = new _Permission(this);
        aa();
    }

    @EasonPermissions({android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA})
    public void aa() {

        /*if (needRequest) {
            if (_permission == null)
                _permission = eason.linyuzai.permissions._Permission.get(MainActivity.this);
            _permission.requestPermissions(eason.linyuzai.permissions._Permission.getPermissionArray(""), MainActivity.this);
            return;
        }*/
        for (Field f : getClass().getDeclaredFields()) {
            Log.d("MainActivity", f.getName());
        }
        //needRequest = false;
    }

    /*public void onFinalResult(boolean grant) {
        if (grant) {
            needRequest = false;
            aa();
        } else {
            needRequest = true;
        }
    }

    public void onPermissionsResult(String[] permissions, boolean[] grants) {

    }*/
}
