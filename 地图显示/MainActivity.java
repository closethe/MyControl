package com.example.yuansen.baiduimaptest;


import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.example.yuansen.baiduimaptest.model.MarkObject;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TouchImageView touchImageView;

    public static Bitmap bmp;
    private Button btn_surface;

    private Button btn_startlocation;
    private LocationClientOption.LocationMode tempMode = LocationClientOption.LocationMode.Hight_Accuracy;
    private String tempcoor="gcj02";
    public TextView mLocationResult,logMsg;
    public TextView trigger,exit;

    public LocationClient mLocationClient;
    public MyLocationListener mMyLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    ////////////////////////////////////////////////////////////////////////////////////////
        touchImageView = (TouchImageView) findViewById(R.id.mv_map);

        bmp= BitmapFactory.decodeResource(getResources(), R.drawable.railway_cn).copy(Bitmap.Config.ARGB_8888, true);
        touchImageView.setImageBitmap(bmp);

        final MarkObject markObject = new MarkObject();
        markObject.setMapX(520f);
        markObject.setMapY(510f);
        markObject.setmBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.icon_marka));
        markObject.setMarkListener(new MarkObject.MarkClickListener() {

            @Override
            public void onMarkClick(int x, int y) {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "点击覆盖物", Toast.LENGTH_SHORT)
                        .show();
            }
        });
        touchImageView.addMark(markObject);

    //////////////////////////////////////////////////////////////////////////////////////////

        mLocationClient = new LocationClient(this.getApplicationContext());
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);

        mLocationResult = (TextView) findViewById(R.id.textView1);

        btn_startlocation = (Button) findViewById(R.id.btn_location);
        btn_startlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initLocation();
                int x = (int) (Math.random() * 1000);
                int y = (int) (Math.random() * 1000);
                touchImageView.removeMark(markObject);
                touchImageView.invalidate();
                MarkObject markObject1 = new MarkObject();
                markObject1.setMapX(x);
                markObject1.setMapY(y);
                markObject1.setmBitmap(BitmapFactory.decodeResource(getResources(),
                        R.drawable.icon_marka));
                markObject1.setMarkListener(new MarkObject.MarkClickListener() {
                    @Override
                    public void onMarkClick(int x, int y) {
                        Toast.makeText(MainActivity.this, "点击new覆盖物", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                touchImageView.addMark(markObject1);
                touchImageView.invalidate();
                if(btn_startlocation.getText().equals("开启定位")){
                    //mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
                    btn_startlocation.setText("停止定位");
                }else{
                    //mLocationClient.stop();
                    btn_startlocation.setText("开启定位");
                }
            }
        });

        btn_surface = (Button) findViewById(R.id.btn_surface);
        btn_surface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(getApplicationContext(),TestSurFaceView.class);
                startActivity(intent);
            }
        });
    }
    private Bitmap setMarker(Bitmap bmp,float x,float y){
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        canvas.drawBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon_marka).copy(Bitmap.Config.ARGB_8888, true),x,y,paint);
        return bmp;
    }
    private void resetImageView(ImageView imageView,Integer index){
        Bitmap bmp;
        bmp = BitmapFactory.decodeResource(getResources(), index).copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bmp);
    }
    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(tempMode);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType(tempcoor);//可选，默认gcj02，设置返回的定位结果坐标系，
        int span=1000;
        try {
            span = Integer.valueOf(""+1000);
        } catch (Exception e) {
            // TODO: handle exception
        }
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        mLocationClient.setLocOption(option);
    }
    /**
     * 实现实时位置回调监听
     */
    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
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
            sb.append("\nlocationdescribe : ");// 位置语义化信息
            sb.append(location.getLocationDescribe());
            List<Poi> list = location.getPoiList();// POI信息
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            sb.append("\ncity= : ");
            sb.append("" +location.getCity());
            sb.append("\ncity code= : ");
            sb.append("" +location.getCityCode());
            sb.append("\n区县= :");
            sb.append(""+location.getDistrict());
            logMsg(sb.toString());
            Log.i("BaiduLocationApiDem", sb.toString());
        }

    }


    /**
     * 显示请求字符串
     * @param str
     */
    public void logMsg(String str) {
        try {
            if (mLocationResult != null)
                mLocationResult.setText(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        mLocationClient.stop();
        super.onStop();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
