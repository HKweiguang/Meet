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
        // ??????
        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));

        Intent intent = getIntent();
        isShow = intent.getBooleanExtra(Constants.INTENT_MENU_SHOW, false);
        if (!isShow) {
            // ??????????????????????????????????????????????????????????????????????????????
            double la = intent.getDoubleExtra("la", 0);
            double lo = intent.getDoubleExtra("lo", 0);
            String address = intent.getStringExtra("address");

            aMap.setMyLocationEnabled(false);
            supportInvalidateOptionsMenu();
            // ????????????
            LatLng latLng = new LatLng(la, lo);
            aMap.clear();
            aMap.addMarker(new MarkerOptions().position(latLng).title("??????").snippet(address));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //???activity??????onDestroy?????????mMapView.onDestroy()???????????????
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //???activity??????onResume?????????mMapView.onResume ()???????????????????????????
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //???activity??????onPause?????????mMapView.onPause ()????????????????????????
        mMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //???activity??????onSaveInstanceState?????????mMapView.onSaveInstanceState (outState)??????????????????????????????
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
