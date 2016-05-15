
package com.wendy.work.camera1.activity;

import com.wendy.work.camera1.camera.CameraInterface;
import com.wendy.work.camera1.camera.CameraInterface.CamOpenOverCallback;
import com.wendy.work.camera1.camera.preview.CameraSurfaceView;
import com.wendy.work.camera1.R;
import com.wendy.work.camera1.ui.MaskView;
import com.wendy.work.camera1.util.CamParaUtil;
import com.wendy.work.camera1.util.DisplayUtil;
import com.wendy.work.camera1.util.FileUtil;
import com.wendy.work.camera1.util.ResultShowActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;


public class CameraActivity extends Activity implements CamOpenOverCallback {
    private static final String TAG = "Wendy";
    MaskView maskView = null;
    private int zoomValue=0;
    CameraSurfaceView surfaceView = null;
    ImageButton shutterBtn;
    private RelativeLayout linearLayout;
    private int distance;
    public  Handler handler;
    float previewRate = -1f;
    int DST_CENTER_RECT_WIDTH = 40; //
    int DST_CENTER_RECT_HEIGHT = 40;//
    public static boolean orientation=true;//true表示竖屏，false表示横屏
    private float gravityX;
    private float gravityY;
    private long lastUpdateTime;
    Point rectPictureSize = null;
    private  SensorManager mSensorManager;
    private  Sensor mAccelerometer;
    private int surfaceViewWidth;
    private int surfaceViewHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CameraSurfaceView.handler=handler;
        setContentView(R.layout.activity_camera);
        initUI();
        initViewParams();
        mSensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);;
        mAccelerometer=mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                long currentUpdateTime = System.currentTimeMillis();
                if (currentUpdateTime - lastUpdateTime > 200) {
                    lastUpdateTime = currentUpdateTime;
                    gravityX = sensorEvent.values[0];
                    gravityY = sensorEvent.values[1];
                    orientation = DisplayUtil.judgeOrientation(gravityX, gravityY);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        }, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        shutterBtn.setOnClickListener(new BtnListeners());
        //RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)shutterBtn.getLayoutParams();
        RelativeLayout.LayoutParams params=(RelativeLayout.LayoutParams)linearLayout.getLayoutParams();
        distance=distance-params.height;
        params.bottomMargin=(distance-DisplayUtil.dip2px(this, 80))/2;
        //params.bottomMargin=20;
        //shutterBtn.setLayoutParams(params);
        linearLayout.setLayoutParams(params);
        View saveView=getLayoutInflater().inflate(R.layout.layout,null);
        final EditText editText=(EditText)saveView.findViewById(R.id.ipconfig);
        FileUtil.initPath();
        final File serverInfo=new File(Environment.getExternalStorageDirectory() + "/Camera1/serverInfo.txt");
        if(!serverInfo.exists()) {
            new AlertDialog.Builder(this).setView(saveView).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    BufferedWriter bufferedWriter= null;
                    try {
                        bufferedWriter = new BufferedWriter(new FileWriter(serverInfo));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    String ip=editText.getText().toString();
                    int port=8090;
                    try {
                        bufferedWriter.write(ip + "\r\n");
                        bufferedWriter.write(String.valueOf(port));
                        bufferedWriter.close();
                    }
                    catch(IOException e)
                    {

                    }
                }
            }).setNegativeButton("Cancel", null).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }
    public int getZoomValue(){
        return zoomValue;
    }
    private void initUI(){

        surfaceView = (CameraSurfaceView)findViewById(R.id.camera_surfaceview);
        shutterBtn = (ImageButton)findViewById(R.id.btn_shutter);
        linearLayout = (RelativeLayout)findViewById(R.id.linearLayout);
        maskView = (MaskView)findViewById(R.id.view_mask);
        surfaceView.getActivity(CameraActivity.this);
    }
    private void initViewParams(){
        LayoutParams params = surfaceView.getLayoutParams();
        //Point p = DisplayUtil.getScreenMetrics(this);
       // params.width = p.x;
        //params.height = p.y;
       // Log.i(TAG, "screen: w = " + p.x + " y = " + p.y);
       // previewRate = DisplayUtil.getScreenRate(this); //??????????????
        //surfaceView.setLayoutParams(params);
        Camera camera=Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        Camera.Parameters parameters=camera.getParameters();
        List<Camera.Size> sizes =parameters.getSupportedPictureSizes();
        Camera.Size size=CamParaUtil.getInstance().getPropPictureSize(sizes, 1.33f, 800);
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height=size.width*p.x/size.height;
        surfaceViewWidth=params.width;
        surfaceViewHeight=params.height;
        surfaceView.setLayoutParams(params);
        camera.release();
        LayoutParams p2 = shutterBtn.getLayoutParams();
        p2.width = DisplayUtil.dip2px(this, 80);
        p2.height = DisplayUtil.dip2px(this, 80);
        shutterBtn.setLayoutParams(p2);
        distance=p.y-surfaceViewHeight;

    }
    public int getSurfaceViewWidth(){
        return surfaceViewWidth;
    }
    public int getSurfaceViewHeight(){
        return surfaceViewHeight;
    }
    @Override
    public void cameraHasOpened() {
        // TODO Auto-generated method stub
        SurfaceHolder holder = surfaceView.getSurfaceHolder();
        CameraInterface.getInstance().doStartPreview(holder, previewRate,zoomValue);
        if(maskView != null){
            Rect screenCenterRect = createCenterScreenRect(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH)
                    ,DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
            maskView.setCenterRect(screenCenterRect);
        }
    }
    private class BtnListeners implements OnClickListener{

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch(v.getId()){
                case R.id.btn_shutter:
                    if(rectPictureSize == null){
                        rectPictureSize = createCenterPictureRect(DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_WIDTH)
                                ,DisplayUtil.dip2px(CameraActivity.this, DST_CENTER_RECT_HEIGHT));
                    }
                    CameraInterface.getInstance().doTakePicture(rectPictureSize.x, rectPictureSize.y);
                    break;
                default:break;
            }
        }

    }

    /**?????????????м???ε??????
     * @param w ????????ο?????λpx
     * @param h ????????θ?????λpx
     * @return
     */
    private Point createCenterPictureRect(int w, int h){

        int wScreen = surfaceView.getLayoutParams().width;
        int hScreen = surfaceView.getLayoutParams().height;
        int wSavePicture = CameraInterface.getInstance().doGetPrictureSize().y; //?????????????????????λ
        int hSavePicture = CameraInterface.getInstance().doGetPrictureSize().x; //?????????????????????λ
        float wRate = (float)(wSavePicture) / (float)(wScreen);
        float hRate = (float)(hSavePicture) / (float)(hScreen);
        float rate = (wRate <= hRate) ? wRate : hRate;//??????????С???????

        int wRectPicture = (int)( w * wRate);
        int hRectPicture = (int)( h * hRate);
        return new Point(wRectPicture, hRectPicture);

    }
    /**
     * ???????м?????
     * @param w ?????ε???,??λpx
     * @param h	?????ε???,??λpx
     * @return
     */
    private Rect createCenterScreenRect(int w, int h){
        int x1 = surfaceView.getLayoutParams().width / 2 - w / 2;
        int y1 = surfaceView.getLayoutParams().height / 2 - h / 2;
        int x2 = x1 + w;
        int y2 = y1 + h;
        return new Rect(x1, y1, x2, y2);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
               zoomValue= CameraInterface.getInstance().ZoomIn();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
               zoomValue= CameraInterface.getInstance().ZoomOn();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
