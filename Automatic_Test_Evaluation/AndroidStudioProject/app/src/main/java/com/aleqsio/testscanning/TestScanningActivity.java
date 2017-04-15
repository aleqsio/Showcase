package com.aleqsio.testscanning;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.Manifest;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aleqsio.testscanning.formats.Test_layout;

public class TestScanningActivity extends AppCompatActivity{
    public static SurfaceView camerapreviewsurfaceview;
    public static SurfaceHolder camerapreviewsurfaceviewholder;
    public static ScanningSetupClass scanningclassinstance;
    public static FrameLayout camerapreviewlayout;
   public static Handler mainthreadhandler;
    public static Test_layout test_layout;
    public static int sliderbarvalue=0;
public boolean spinnervisible=false;


    private int constantanswerscount=0;

private String oldanswers="";
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
                    Intent i = getBaseContext().getPackageManager()
                            .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_scanning);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
        camerapreviewlayout = (FrameLayout) findViewById(R.id.camerapreviewlayout);
        camerapreviewsurfaceview = (SurfaceView) findViewById(R.id.previewsurfaceview);
findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View view) {
begin_scanning_of_test();
    }
});
        mainthreadhandler = new Handler(getMainLooper()) {
            @Override
            public void handleMessage(Message inputMessage) {
                receive_results_from_tracked_frame(inputMessage.getData());
            }
        };
    }


    @Override
    protected void onResume() {
        super.onResume();
        begin_scanning_of_test();

        camerapreviewsurfaceviewholder=camerapreviewsurfaceview.getHolder();
      if(scanningclassinstance==null) {
          scanningclassinstance = new ScanningSetupClass();
      }
        camerapreviewsurfaceviewholder.addCallback(scanningclassinstance);
        if(scanningclassinstance.surfaceexists)
        {
            scanningclassinstance.starttracking();
        }
    }


    private void begin_scanning_of_test()
    {
        test_layout=new Test_layout();
        test_layout.count_of_questions=11;
        test_layout.max_count_of_choices =4;
        test_layout.integritygrid="1_1_1_1_1_0_0_0_0_0_0";
        test_layout.creditgrid="1_1_1_1_1_1_1_1_1_1_1";
      //  test_layout.answersgrid="13_1_1_1_1_1_1_1_1_1_1";
        test_layout.answersgrid=((EditText)findViewById(R.id.editText)).getText().toString();

    }
    private void receive_results_from_tracked_frame(Bundle data)
    {

        if (!data.getBoolean("success")==true) {
            if (data.getChar("angle") == 'L') {
                setTooltip(getString(R.string.left));

            }
            if (data.getChar("angle") == 'R') {
                setTooltip(getString(R.string.right));

            }
            if (data.getChar("angle") == 'C') {
                setTooltip(getString(R.string.center));
            }
            constantanswerscount = 0;
        }else
        {

            if (data.getString("answers").equals(oldanswers) && test_layout.verifyintegritygrid(data.getString("integrity"))) {
                constantanswerscount++;
            } else if(test_layout.verifyintegritygrid(data.getString("integrity"))){

                constantanswerscount = 1;
            }else
            {
                constantanswerscount = 0;
            }

            oldanswers = data.getString("answers");
            if(constantanswerscount<=2 &&constantanswerscount>0 ) {
                vibrate();
                setTooltip(getString(R.string.wait));
            }
            if(constantanswerscount>=2)
            {
                setTooltip("Success");
                int score=test_layout.generatescore(data.getString("answers"));
                int max=  test_layout.get_max_score();
                setResult(String.valueOf(score)+"/"+String.valueOf(max)+"  "+String.valueOf((int)(((float)(score))/max*100))+"%");

            }
        }

    }

private void setSpinner(boolean isVisible)
{
  final  ProgressBar detectionprogressbar = (ProgressBar) findViewById(R.id.progressBar);
    final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
    final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

    animationFadeOut.setAnimationListener(new Animation.AnimationListener()
    {
        public void onAnimationStart(Animation arg0)
        {
        }
        public void onAnimationRepeat(Animation arg0)
        {
        }

        public void onAnimationEnd(Animation arg0)
        {
            detectionprogressbar.setVisibility(View.INVISIBLE);
        }
    });
    animationFadeIn.setAnimationListener(new Animation.AnimationListener()
    {
        public void onAnimationStart(Animation arg0)
        {
            detectionprogressbar.setVisibility(View.VISIBLE);
        }
        public void onAnimationRepeat(Animation arg0)
        {
        }

        public void onAnimationEnd(Animation arg0)
        {

        }
    });
if(isVisible && !spinnervisible) {
    detectionprogressbar.startAnimation(animationFadeIn);

    spinnervisible=true;
}    if(!isVisible && spinnervisible)
{
    detectionprogressbar.startAnimation(animationFadeOut);
    spinnervisible=false;
}
}

    private void setTooltip(final String text)
    {
        final  TextView tooltip = (TextView) findViewById(R.id.textView4);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        animationFadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationStart(Animation arg0)
            {
            }
            public void onAnimationRepeat(Animation arg0)
            {
            }

            public void onAnimationEnd(Animation arg0)
            {
                tooltip.setText(text);
                tooltip.startAnimation(animationFadeIn);
            }
        });
        if(!text.equals(tooltip.getText())) {
            tooltip.startAnimation(animationFadeOut);
        }
    }
    private void setResult(final String text)
    {
        final  TextView resulttextview = (TextView) findViewById(R.id.resulttextview);
        final Animation animationFadeIn = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation animationFadeOut = AnimationUtils.loadAnimation(this, R.anim.fadeout);

        animationFadeOut.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationStart(Animation arg0)
            {
            }
            public void onAnimationRepeat(Animation arg0)
            {
            }

            public void onAnimationEnd(Animation arg0)
            {
                resulttextview.setText(text);
                resulttextview.startAnimation(animationFadeIn);
            }
        });
        if(!text.equals(resulttextview.getText())) {
            resulttextview.startAnimation(animationFadeOut);
        }
    }
    private void vibrate()
    {
        Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if(v.hasVibrator()) {
            v.vibrate(50);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        scanningclassinstance.stoptracking();
        camerapreviewsurfaceviewholder.removeCallback(scanningclassinstance);
    }

}
