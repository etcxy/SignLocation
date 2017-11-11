package com.etcxy.signlocation;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.Poi;
import com.baidu.location.service.LocationService;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.etcxy.Utils.UIUtils;

import org.json.JSONObject;

import baidumapsdk.demo.indoorview.BaseStripAdapter;
import baidumapsdk.demo.indoorview.StripListView;

import static com.etcxy.Utils.JsonUtils.JsonPost;


public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private LocationService locationService;
    private Vibrator mVibrator;

    boolean isFirstLoc = true; // 是否首次定位
    MapView mMapView;
    BaiduMap mBaiduMap;
    StripListView stripListView;
    BaseStripAdapter mFloorListAdapter;

    private BDLocation mLocation;

    private String mName;
    private String mNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mName = intent.getStringExtra("name");
        mNumber = intent.getStringExtra("number");


        RelativeLayout layout = new RelativeLayout(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mainview = inflater.inflate(R.layout.activity_halfmap, null);
        layout.addView(mainview);

        mTextMessage = (TextView) mainview.findViewById(R.id.message);

        locationService = ((LocationApplication) getApplication()).locationService;


        // 地图初始化
        mMapView = (MapView) mainview.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        stripListView = new StripListView(this);
        layout.addView(stripListView);
        setContentView(layout);
        mFloorListAdapter = new BaseStripAdapter(MainActivity.this);

    }


    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(String str) {
        final String s = str;
        try {
            if (mTextMessage != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mTextMessage.post(new Runnable() {
                            @Override
                            public void run() {
                                mTextMessage.setText(s);
                            }
                        });

                    }
                }).start();
            }
            //LocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 停止定位服务
     */
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);
        //注册监听
        int type = getIntent().getIntExtra("from", 0);
        if (type == 0) {
            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        } else if (type == 1) {
            locationService.setLocationOption(locationService.getOption());
        }
        locationService.start();// 定位SDK

//        startLocation.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (startLocation.getText().toString().equals(getString(R.string.startlocation))) {
//                    locationService.start();// 定位SDK
//                     start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
//                    startLocation.setText(getString(R.string.stoplocation));
//                } else {
//                    locationService.stop();
//                    startLocation.setText(getString(R.string.startlocation));
//                }
//            }
//        });
    }

    public void submit(View view) {

        String sbumit_msg = "";

        if (view.getId() == R.id.sign) {
            sbumit_msg = "签到";
        } else if (view.getId() == R.id.unsign) {
            sbumit_msg = "签离";
        } else if (view.getId() == R.id.leave) {
            sbumit_msg = "请假";
        }

        final String finalSbumit_msg = sbumit_msg;
        new AsyncTask<String, String,String>() {

            @Override
            protected String doInBackground(String... strings) {

                try {

                    String path = "http://192.168.199.100:8080/serviceHu/Ask4Leave";
                    String result = null;

                    JSONObject json = new JSONObject();
                    json.put("name", mName);
                    json.put("number", mNumber);
                    json.put("sbumit_msg", finalSbumit_msg);
                    json.put("time", mLocation.getTime());
                    json.put("latitude", mLocation.getLatitude());
                    json.put("lontitude", mLocation.getLongitude());
                    json.put("addr", mLocation.getAddrStr());


                    result = JsonPost(path, json);
                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                UIUtils.showToast(MainActivity.this,s);
            }
        }.execute("");



        Toast.makeText(MainActivity.this, sbumit_msg, Toast.LENGTH_SHORT).show();

    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {

                mLocation = location;

                StringBuffer sb = new StringBuffer(256);
//                sb.append("time : ");
//                /**
//                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
//                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
//                 */
//                sb.append(location.getTime());
//
//                sb.append("\nlocType : ");// 定位类型
//                sb.append(location.getLocType());

//                sb.append("\nlocType description : ");// *****对应的定位类型说明*****
//                sb.append(location.getLocTypeDescription());

                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());

                sb.append("\nlontitude : ");// 经度
                sb.append(location.getLongitude());

//                sb.append("\nradius : ");// 半径
//                sb.append(location.getRadius());

//                sb.append("\nCountryCode : ");// 国家码
//                sb.append(location.getCountryCode());

//                sb.append("\nCountry : ");// 国家名称
//                sb.append(location.getCountry());

//                sb.append("\ncitycode : ");// 城市编码
//                sb.append(location.getCityCode());

//                sb.append("\ncity : ");// 城市
//                sb.append(location.getCity());

                sb.append("\nDistrict : ");// 区
                sb.append(location.getDistrict());

                sb.append("\nStreet : ");// 街道
                sb.append(location.getStreet());

                sb.append("\naddr : ");// 地址信息
                sb.append(location.getAddrStr());

//                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
//                sb.append(location.getUserIndoorState());
//                sb.append("\nDirection(not all devices have value): ");

//                sb.append(location.getDirection());// 方向
//                sb.append("\nlocationdescribe: ");

                sb.append(location.getLocationDescribe());// 位置语义化信息
                sb.append("\nPoi: ");// POI信息
                if (location.getPoiList() != null && !location.getPoiList().isEmpty()) {
                    for (int i = 0; i < location.getPoiList().size(); i++) {
                        Poi poi = (Poi) location.getPoiList().get(i);
                        sb.append(poi.getName() + ";");
                    }
                }
                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
//                    sb.append("\noperationers : ");// 运营商信息
//                    sb.append(location.getOperators());
//
//                    sb.append("\ndescribe : ");
//                    sb.append("网络定位成功");

                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                logMsg(sb.toString());


                MyLocationData locData = new MyLocationData.Builder().accuracy(location.getRadius())
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(100).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
                mBaiduMap.setMyLocationData(locData);
                if (isFirstLoc) {
                    isFirstLoc = false;
                    LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(ll).zoom(18.0f);
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                }
            }
        }

    };
}
