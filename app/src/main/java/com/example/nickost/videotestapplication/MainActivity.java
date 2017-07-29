package com.example.nickost.videotestapplication;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    MaximizeMediaController mediaController;
    VideoView vv;
    int videoProgress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vv =(VideoView)findViewById(R.id.windowVid);
        vv.setVideoURI(Uri.parse("https://images-na.ssl-images-amazon.com/images/I/D15ccfgtVfS.mp4"));
        mediaController = new MaximizeMediaController(this, false, (ViewGroup)findViewById(R.id.videoContainer));
        mediaController.setAnchorView(vv);
        //mediaController.setMediaPlayer(vv);
        vv.setMediaController(mediaController);

        vv.setOnPreparedListener(mp -> {

            if (MainActivity.this.videoProgress > 0) {
                int position = 50;//getSeekTimeFromProgressPercentage(MainActivity.this.videoProgress, binding.VideoView
                    //.getDuration());
                Log.d(TAG, "onResume: Seeking to: " + position);
                vv.seekTo(position);

            }

            vv.start();
            mediaController.show();
        });
    }



    public void launchVideo(View view) {
        Intent intent = new Intent(this, VideoActivity.class);
        intent.putExtra(VideoActivity.INTENT_EXTRA_SHOW_VIDEO_CONTROLS, true);
        intent.putExtra(VideoActivity.INTENT_EXTRA_FORCE_LANDSCAPE, true);
        String localVideoLink = "android.resource://" + getPackageName() + "/" +
            R.raw.samplevideo;
        String videolink = "https://images-na.ssl-images-amazon.com/images/I/D15ccfgtVfS.mp4";
        intent.putExtra(VideoActivity.INTENT_EXTRA_VIDEO_URL, videolink);
        startActivity(intent);
    }
}
