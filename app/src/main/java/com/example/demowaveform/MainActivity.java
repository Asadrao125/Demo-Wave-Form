package com.example.demowaveform;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.animation.Animator;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chibde.visualizer.LineBarVisualizer;
import com.masoudss.lib.SeekBarOnProgressChanged;
import com.masoudss.lib.WaveformSeekBar;

import org.firezenk.audiowaves.Visualizer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    public MediaPlayer mediaPlayer;
    public WaveformSeekBar waveformSeekBar;
    final Handler handler = new Handler();

    LinearLayout record_voice_container_linearLayout;
    ImageView cancel_imageview;

    String fileName = "";
    MediaRecorder recorder;
    MediaPlayer player;
    boolean isRecorded = false;
    CountDownTimer timer;
    TextView recordVoiceMessageTextView;
    int audioButtonCounter = -1;

    Visualizer visualizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        record_voice_container_linearLayout = findViewById(R.id.record_voice_container_linearlayout);
        cancel_imageview = findViewById(R.id.cancel_imageview);
        recordVoiceMessageTextView = findViewById(R.id.record_voice_msg_textview);
        visualizer = findViewById(R.id.visualizer);

        /*waveformSeekBar.setOnProgressChanged(new SeekBarOnProgressChanged() {
            @Override
            public void onProgressChanged(@NotNull WaveformSeekBar waveformSeekBar, float v, boolean b) {
                mediaPlayer.seekTo((int) v);
            }
        });*/

        cancel_imageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileName = "";
                audioButtonCounter = -1;
                isRecorded = false;
                stopRecording();
                stopPlaying();
                recordVoiceMessageTextView.setText("Start Recording");

                cancel_imageview.animate().translationX(220).alpha(1).setInterpolator(new DecelerateInterpolator(3f)).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        cancel_imageview.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).setDuration(1500).start();
            }
        });

        // Start Recording Audio, Stop Recording Audio, Start Playing, Stop Playing
        record_voice_container_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioButtonCounter++;
                switch (audioButtonCounter) {
                    case 0:
                        startRecording();
                        isRecorded = true;
                        recordVoiceMessageTextView.setText("Stop Recording");
                        break;

                    case 1:
                        stopRecording();
                        recordVoiceMessageTextView.setText("Play Voice");
                        isRecorded = false;
                        // Imageview visibility
                        cancel_imageview.setVisibility(View.VISIBLE);
                        cancel_imageview.animate().translationX(0).alpha(1).setInterpolator(new DecelerateInterpolator(3f)).setListener(null).setDuration(1500).start();
                        break;

                    case 2:
                        playRecording();
                        recordVoiceMessageTextView.setText("Stop Voice");
                        break;

                    case 3:
                        stopPlaying();
                        recordVoiceMessageTextView.setText("Play Voice");
                        // Setting audioCounter variable to 1
                        audioButtonCounter = 1;
                        // So that after this case, case 2 should be executed
                        // Setting audioCounter variable to -1 in cancel button click for cycle to recontinue
                        break;
                }
            }
        });
    }

    private void startRecording() {
        String uuid = UUID.randomUUID().toString();
        fileName = getFilesDir().getPath() + "/" + uuid + ".wav";
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(AudioFormat.ENCODING_PCM_16BIT);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioChannels(1);
        recorder.setAudioEncodingBitRate(128000);
        recorder.setAudioSamplingRate(48000);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":startRecording()", "prepare() failed");
        }

        recorder.start();
        visualizer.startListening();

        timer = new CountDownTimer(120000, 120000) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                record_voice_container_linearLayout.performClick();
            }
        }.start();
    }

    private void stopRecording() {
        if (timer != null) {
            timer.cancel();
        }

        if (recorder != null) {
            recorder.release();
            visualizer.stopListening();
            recorder = null;
        }
    }

    private void playRecording() {
        player = new MediaPlayer();
        try {
            player.setDataSource(fileName);
            waveformSeekBar = findViewById(R.id.waveformSeekBar);
            waveformSeekBar.setSampleFrom(new File(fileName));
            waveformSeekBar.setVisibility(View.VISIBLE);

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    stopPlaying();
                    recordVoiceMessageTextView.setText("Play Voice");
                    audioButtonCounter = 1;
                    waveformSeekBar.setVisibility(View.GONE);
                    visualizer.stopListening();
                }
            });

            /*handler.postDelayed(new Runnable() {
                public void run() {
                    waveformSeekBar.setProgress(player.getCurrentPosition());
                    handler.postDelayed(this, 1000);
                }
            }, 1000);*/

            player.prepare();
            player.start();
        } catch (IOException e) {
            Log.e(MainActivity.class.getSimpleName() + ":playRecording()", "prepare() failed");
        }
    }

    void setWavesWhilePlaying() {
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        waveformSeekBar.setSampleFrom(R.raw.song);

        mediaPlayer = MediaPlayer.create(this, R.raw.song);
        mediaPlayer.start();

        handler.postDelayed(new Runnable() {
            public void run() {
                waveformSeekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }, 1000);

        waveformSeekBar.setMaxProgress(mediaPlayer.getDuration());
        LineBarVisualizer lineBarVisualizer = findViewById(R.id.visualizerLineBar);
        lineBarVisualizer.setVisibility(View.VISIBLE);
        lineBarVisualizer.setColor(ContextCompat.getColor(MainActivity.this, R.color.black));
        lineBarVisualizer.setDensity(60);
        lineBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
    }

    private void stopPlaying() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private boolean checkWritePermission() {
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        return result1 == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWritePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS, WRITE_EXTERNAL_STORAGE}, 1);
    }
}