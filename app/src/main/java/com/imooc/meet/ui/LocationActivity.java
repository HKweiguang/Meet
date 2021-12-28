package com.imooc.meet.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.imooc.framework.adapter.CommonAdapter;
import com.imooc.framework.adapter.CommonViewHolder;
import com.imooc.framework.base.BaseBackActivity;
import com.imooc.framework.entity.Constants;
import com.imooc.framework.manager.DialogManager;
import com.imooc.framework.manager.MapManager;
import com.imooc.framework.view.DialogView;
import com.imooc.framework.view.LoadingView;
import com.imooc.meet.R;

import java.util.ArrayList;

public class LocationActivity extends BaseBackActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener {

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

    private DialogView mPoiView;
    private RecyclerView mConstellationnView;
    private TextView tv_cancel;

    private CommonAdapter<PoiItem> mPoiListAdapter;
    private ArrayList<PoiItem> mList = new ArrayList<>();

    private PoiSearch.Query query;
    private PoiSearch poiSearch;

    private LoadingView mLoadingView;

    private double ILa;
    private double ILo;
    private String IAddress;

    private int ITEM = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        initPoiView();
        initView(savedInstanceState);
    }

    private void initPoiView() {
        mLoadingView = new LoadingView(this);
        mLoadingView.setLoadingText("正在搜索...");

        mPoiView = DialogManager.getInstance().initView(this, R.layout.dialog_select_constellation, Gravity.BOTTOM);
        mPoiView.setCancelable(false);
        mConstellationnView = mPoiView.findViewById(R.id.mConstellationnView);
        tv_cancel = mPoiView.findViewById(R.id.tv_cancel);

        tv_cancel.setOnClickListener(v -> DialogManager.getInstance().hide(mPoiView));

        mConstellationnView.setLayoutManager(new LinearLayoutManager(this));
        mConstellationnView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mPoiListAdapter = new CommonAdapter<>(mList, new CommonAdapter.OnBindDataListener<PoiItem>() {
            @Override
            public void onBindViewHolder(PoiItem model, CommonViewHolder holder, int position, int type) {
                holder.setText(R.id.tv_age_text, model.toString());

                holder.itemView.setOnClickListener(v -> {
                    ITEM = position;

                    DialogManager.getInstance().hide(mPoiView);

                    MapManager.getInstance().address2poi(model.toString())
                            .setOnGeocodeListener(new MapManager.OnGeocodeListener() {
                                @Override
                                public void poi2address(String address) {

                                }

                                @Override
                                public void address2poi(double la, double lo, String address) {
                                    ILa = la;
                                    ILo = lo;
                                    IAddress = address;
                                    updatePoi(ILa, ILo, IAddress);
                                }
                            });
                });
            }

            @Override
            public int getLayoutId(int type) {
                return R.layout.layout_me_age_item;
            }
        });
        mConstellationnView.setAdapter(mPoiListAdapter);
    }

    private void initView(Bundle savedInstanceState) {
        mMapView = findViewById(R.id.mMapView);
        et_search = findViewById(R.id.et_search);
        iv_poi = findViewById(R.id.iv_poi);

        iv_poi.setOnClickListener(this);

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

            updatePoi(la, lo, address);
        }

        aMap.setOnMyLocationChangeListener(location -> {

        });
    }

    /**
     * 更新地点
     * @param la 经度
     * @param lo 纬度
     * @param address 地址
     */
    private void updatePoi(double la, double lo, String address) {
        aMap.setMyLocationEnabled(false);
        supportInvalidateOptionsMenu();
        // 显示位置
        LatLng latLng = new LatLng(la, lo);
        aMap.clear();
        aMap.addMarker(new MarkerOptions().position(latLng).title("位置").snippet(address));
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_poi) {
            String keyword = et_search.getText().toString().trim();
            if (TextUtils.isEmpty(keyword)) {
                return;
            }
            poiSearch(keyword);
        }
    }

    /**
     * 关键字POI搜索
     *
     * @param keyWord 关键字
     */
    private void poiSearch(String keyWord) {
        mLoadingView.show();
        query = new PoiSearch.Query(keyWord, "", "");
        query.setPageSize(6);
        query.setPageNum(1);
        try {
            poiSearch = new PoiSearch(this, query);
            poiSearch.setOnPoiSearchListener(this);
            poiSearch.searchPOIAsyn();
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onPoiSearched(PoiResult poiResult, int i) {
        // 得到搜索结果
        mLoadingView.hide();
        if (mList.size() > 0) {
            mList.clear();
        }
        mList.addAll(poiResult.getPois());
        mPoiListAdapter.notifyDataSetChanged();
        DialogManager.getInstance().show(mPoiView);
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

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
            if (ITEM > 0) {
                intent.putExtra("la", ILa);
                intent.putExtra("lo", ILo);
                intent.putExtra("address", IAddress);
            } else {
                intent.putExtra("la", aMap.getMyLocation().getLatitude());
                intent.putExtra("lo", aMap.getMyLocation().getLongitude());
                intent.putExtra("address", aMap.getMyLocation().getExtras().getString("desc"));
            }
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
