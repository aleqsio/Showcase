package com.aleqsio.testscanning;


import android.app.Application;
import android.hardware.Camera;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aleqsio.testscanning.formats.Test_layout;

import java.io.IOException;
import java.util.List;

public class ScanningSetupClass implements SurfaceHolder.Callback, Camera.PreviewCallback {
    Camera backcamera;
    boolean surfaceexists;
    Thread scanningthread;
    public static  Camera.Size imageframesize;
    public static byte[] receiveddata;
    ScanningprocessRunnable scanningprocessrunnable;
    public ScanningSetupClass() {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        starttracking();
        surfaceexists=true;
    }




    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceexists=false;
    }


    public void starttracking() {
        startupbackcamera();
        startupthreadedscanning();
    }

    private int findFrontFacingCamera() {
        int cameraId=0;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
    public void startupbackcamera(){

        if(backcamera!=null)
        {
            backcamera.release();
            backcamera=null;
        }
        backcamera = Camera.open(findFrontFacingCamera());


        Camera.Parameters params = backcamera.getParameters();
        List<Camera.Size> sizes = params.getSupportedPreviewSizes();
        int pos=sizes.size()-1;
        while(sizes.get(pos).width<400 || sizes.get(pos).height<300)
        {

            pos--;

            if(pos==0)
            {
                break;
            }
        }
        imageframesize =sizes.get(pos);
        params.setPreviewSize(imageframesize.width, imageframesize.height);
        backcamera.setParameters(params);
        backcamera.setPreviewCallback(this);
        backcamera.setDisplayOrientation(90);
        int height=TestScanningActivity.camerapreviewlayout.getHeight();
      int width=  TestScanningActivity.camerapreviewlayout.getHeight()*imageframesize.height/imageframesize.width;
        TestScanningActivity.camerapreviewsurfaceviewholder.setFixedSize(width,height);
        try {
            backcamera.setPreviewDisplay(TestScanningActivity.camerapreviewsurfaceviewholder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        backcamera.startPreview();
    }
    public void terminatebackcamera()
    {
        if(backcamera!=null)
        {
            backcamera.stopPreview();
            backcamera.setPreviewCallback(null);
            backcamera.release();
            backcamera = null;
        }
    }

    public void stoptracking() {
        terminatebackcamera();
        endthreadedscanning();
    }

    public void startupthreadedscanning()
    {
        if(scanningprocessrunnable!=null) {
            scanningprocessrunnable.terminatethread();
        }
        scanningprocessrunnable=new ScanningprocessRunnable();
        scanningthread =new Thread(scanningprocessrunnable);
        scanningthread.start();

    }


    public void endthreadedscanning()
    {
        try {
            scanningprocessrunnable.terminatethread();
        }catch (NullPointerException e)
        {

        }

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        receiveddata=data;
    }
}
