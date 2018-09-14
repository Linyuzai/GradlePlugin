package eason.linyuzai.gradleplugin;

import android.Manifest;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Manifest.permission.ACCESS_CHECKIN_PROPERTIES;
    }

    public void aa(){

    }
}
