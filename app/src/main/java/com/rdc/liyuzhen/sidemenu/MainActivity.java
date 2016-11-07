package com.rdc.liyuzhen.sidemenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SideMenu.OnMenuItemClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ArrayList<String> menuList = new ArrayList();
        menuList.add("基本信息") ;
        menuList.add("作业准备");
        menuList.add("作业风险");
        menuList.add("作业过程");
        menuList.add("作业终结");
        ((SideMenu)findViewById(R.id.sm)).setMenuItem(menuList);
        ((SideMenu)findViewById(R.id.sm)).setOnMenuItemClickListener(this);
    }

    private Toast mToast;
    @Override
    public void onMenuItemClick(int index) {
        if (mToast== null) {
            mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        }
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setText("index = " + index);
        mToast.show();
    }
}
