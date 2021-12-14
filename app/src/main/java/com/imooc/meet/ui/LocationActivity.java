package com.imooc.meet.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.entity.Constants;
import com.imooc.meet.R;

public class LocationActivity extends BaseBackActivity {

    public static void startActivity(Activity activity, boolean isShow, double la, double lo, String address, int requestCode) {
        Intent intent = new Intent(activity, LocationActivity.class);
        intent.putExtra(Constants.INTENT_MENU_SHOW, isShow);
        intent.putExtra("la", la);
        intent.putExtra("lo", lo);
        intent.putExtra("address", address);
        activity.startActivityForResult(intent, requestCode);
    }

    private MapView mMapView;
    private EditText et_search;
    private ImageView iv_poi;

    private AMap aMap;

    private boolean isShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mMapView);
        et_search = findViewById(R.id.et_search);
        iv_poi = findViewById(R.id.iv_poi);

        mMapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mMapView.getMap();
        }

        MyLocationStyle myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
        myLocationStyle.interval(2000);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.setMyLocationEnabled(true);
        // 缩放
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));

        Intent intent = getIntent();
        isShow = intent.getBooleanExtra(Constants.INTENT_MENU_SHOW, false);
        if (!isShow) {
            // 如果不显示，则作为展示类地图，接收外界传递的地址显示
            double la = intent.getDoubleExtra("la", 0);
            double lo = intent.getDoubleExtra("lo", 0);
            String address = intent.getStringExtra("address");

            aMap.setMyLocationEnabled(false);
            supportInvalidateOptionsMenu();
            // 显示位置
            LatLng latLng = new LatLng(la, lo);
            aMap.clear();
            aMap.addMarker(new MarkerOptions().position(latLng).title("位置").snippet(address));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShow) {
            getMenuInflater().inflate(R.menu.location_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_send) {
            Intent intent = new Intent();
            intent.putExtra("la", aMap.getMyLocation().getLatitude());
        }
        return super.onOptionsItemSelected(item);
    }
}
