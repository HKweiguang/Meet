package com.imooc.framework.manager;

import android.content.Context;

import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeAddress;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

public class MapManager {

    private static volatile MapManager mInstance = null;

    private GeocodeSearch geocodeSearch;

    private OnGeocodeListener onGeocodeListener;

    public void setOnGeocodeListener(OnGeocodeListener onGeocodeListener) {
        this.onGeocodeListener = onGeocodeListener;
    }

    private MapManager() {

    }

    public static MapManager getInstance() {
        if (mInstance == null) {
            synchronized (MapManager.class) {
                if (mInstance == null) {
                    mInstance = new MapManager();
                }
            }
        }

        return mInstance;
    }

    public void initMap(Context mContext) {
        try {
            geocodeSearch = new GeocodeSearch(mContext);
            geocodeSearch.setOnGeocodeSearchListener(searchListener);
        } catch (AMapException e) {
            e.printStackTrace();
        }
    }

    private GeocodeSearch.OnGeocodeSearchListener searchListener = new GeocodeSearch.OnGeocodeSearchListener() {
        @Override
        public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (regeocodeResult != null) {
                    if (onGeocodeListener != null) {
                        onGeocodeListener.poi2address(regeocodeResult.getRegeocodeAddress().getFormatAddress());
                    }
                }
            }
        }

        @Override
        public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
            if (i == AMapException.CODE_AMAP_SUCCESS) {
                if (geocodeResult != null) {
                    if (onGeocodeListener != null) {
                        if (geocodeResult.getGeocodeAddressList() != null && geocodeResult.getGeocodeAddressList().size() > 0) {
                            GeocodeAddress address = geocodeResult.getGeocodeAddressList().get(0);
                            onGeocodeListener.address2poi(address.getLatLonPoint().getLatitude(), address.getLatLonPoint().getLongitude(), address.getFormatAddress());
                        }
                    }
                }
            }
        }
    };

    /**
     * 地址转经纬度
     *
     * @param address 地址
     */
    public MapManager address2poi(String address) {
        GeocodeQuery query = new GeocodeQuery(address, "");
        geocodeSearch.getFromLocationNameAsyn(query);
        return mInstance;
    }

    /**
     * 经纬度转地址
     *
     * @param la 经度
     * @param lo 纬度
     */
    public MapManager poi2address(double la, double lo) {
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(la, lo), 3000, GeocodeSearch.AMAP);
        geocodeSearch.getFromLocationAsyn(query);
        return mInstance;
    }

    public interface OnGeocodeListener {

        void poi2address(String address);

        void address2poi(double la, double lo, String address);
    }
}