package com.example.nickost.videotestapplication;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.MediaController;

import com.example.nickost.videotestapplication.databinding.VideoBinding;

import static com.example.nickost.videotestapplication.Util.getSeekTimeFromProgressPercentage;

/**
 * Activity that plays native legacy_video, and communicates its playback state to the webview.
 */
public class VideoActivity extends Activity {

    private static final String TAG = "VideoActivity";
    /**
     * Intent parameter for legacy_video url.
     */
    public static final String INTENT_EXTRA_VIDEO_URL = "com.amazon.mobile.webapp.videoUrl";

    /**
     * Intent parameter for legacy_video progress.
     */
    public static final String INTENT_EXTRA_VIDEO_PROGRESS = "com.amazon.mobile.webapp.videoProgress";

    /**
     * Optional parameters to show play controls and disable click-to-close functionality
     */
    public static final String INTENT_EXTRA_SHOW_VIDEO_CONTROLS = "SHOW_VIDEO_CONTROLS";
    public static final String INTENT_EXTRA_FORCE_LANDSCAPE = "FORCE_LANDSCAPE";

    /**
     * Instance state parameter for legacy_video progress.
     */
    private static final String INSTANCE_STATE_VIDEO_PROGRESS = "VIDEO_PROGRESS";


    /**
     * Root binding view.
     */
    VideoBinding binding;

    /**
     * Media Controller.
     */
    MediaController mediaController;

    /**
     * Video progress. This static variable is required to save legacy_video progress state
     * in situations where AlexaWebappActivity.onActivityResult does not get called with the
     * returned intent from this activity. For example, when the home button is pressed while
     * playing legacy_video.
     */
    int videoProgress = 0;

    /**
     * Completion flag to get around order-of-events problem with resetting progress.
     */
    private boolean videoIsComplete = false;

    /**
     * The video activity can show controls, but this is disabled by default.
     */
    private boolean showVideoControls = false;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int progressFromInstanceState = 0;
        Intent intent = getIntent();

        binding = DataBindingUtil.setContentView(this, R.layout.video);


        if (savedInstanceState != null) {
            progressFromInstanceState = savedInstanceState.getInt(INSTANCE_STATE_VIDEO_PROGRESS);
        }

        if (intent != null) {
            Bundle extras = intent.getExtras();
            configureVideoFromExtras(extras, extras.getInt(INTENT_EXTRA_VIDEO_PROGRESS, 0),
                progressFromInstanceState
            );
            setUpVideoView(extras.getString(INTENT_EXTRA_VIDEO_URL));

            binding.VideoView.requestFocus();
        }
    }

    private void configureVideoFromExtras(Bundle bundle, int progressFromIntent, int progressFromInstanceState) {
        if (bundle.containsKey(INTENT_EXTRA_SHOW_VIDEO_CONTROLS)) {
            showVideoControls = bundle.getBoolean(INTENT_EXTRA_SHOW_VIDEO_CONTROLS);

            mediaController = new MediaController(this);
            binding.VideoView.setMediaController(mediaController);
        }
        videoProgress = Math.max(progressFromIntent, progressFromInstanceState);
        Log.d(TAG, "configureVideoFromExtras: videoprogress"+ videoProgress);
        if(videoProgress == 0) videoProgress = 60;
        if (bundle.containsKey(INTENT_EXTRA_FORCE_LANDSCAPE) &&
            bundle.getBoolean(INTENT_EXTRA_FORCE_LANDSCAPE)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    @Override
    public void onPause() {
        if (videoIsComplete) {
            videoProgress = 0;
            videoIsComplete = false;
        } else {
            if (!showVideoControls) {
                saveProgress();
            }
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        binding.VideoView.setOnPreparedListener(mp -> {
            if (VideoActivity.this.videoProgress > 0) {

                int position = getSeekTimeFromProgressPercentage(VideoActivity.this.videoProgress, binding.VideoView.getDuration());
                Log.d(TAG, "onResume: Seeking to: " + position);
                binding.VideoView.seekTo(position);

            }
            binding.VideoView.start();
            if (showVideoControls) {
                mediaController.show();
            }
        });

        super.onResume();
    }

    @Override
    public void onBackPressed() {
        saveProgress();
        super.onBackPressed();
    }

    @Override
    public void onSaveInstanceState(final Bundle state) {
        super.onSaveInstanceState(state);
        state.putInt(INSTANCE_STATE_VIDEO_PROGRESS, getVideoProgress());
    }

    /**
     * Queue up legacy_video progress to provide back to the activity that started this activity.
     */
    private void saveProgress() {
        videoProgress = getVideoProgress();

        Intent data = new Intent();
        data.putExtra(INTENT_EXTRA_VIDEO_PROGRESS, videoProgress);
        setResult(RESULT_CANCELED, data);
    }

    /**
     * Given a legacy_video url and (optionally) a progress percentage, set up the legacy_video view and play
     * the legacy_video when ready.
     * @param videoUrl - url of the legacy_video to load.
     */
    void setUpVideoView(final String videoUrl) {
        binding.VideoView.setVideoURI(Uri.parse(videoUrl));

        binding.VideoView.setOnCompletionListener(mp -> {
            VideoActivity.this.videoIsComplete = true;
            VideoActivity.this.setResult(RESULT_OK);
            VideoActivity.this.finish();
        });

        binding.VideoView.setOnTouchListener((v, m) -> {
            if (m.getAction() == MotionEvent.ACTION_UP) {
                if (!showVideoControls) {
                    saveProgress();
                    VideoActivity.this.finish();
                    Log.d(TAG, "setUpVideoView: finish activity");
                }
                return false;
            }
            return true;
        });
    }

    /**
     * Return the current position of playback as a percentage.
     * @return an int from 0-100
     */
    int getVideoProgress() {
        int duration = binding.VideoView.getDuration();

        if (duration != 0) {
            return (100 * binding.VideoView.getCurrentPosition()) / duration;
        } else {
            return 0;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


}
