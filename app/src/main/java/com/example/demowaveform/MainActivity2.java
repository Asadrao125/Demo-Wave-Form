package com.example.demowaveform;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.chibde.visualizer.LineBarVisualizer;
import com.example.demowaveform.waveform_pkg.AudioDataReceivedListener;
import com.example.demowaveform.waveform_pkg.PlaybackListener;
import com.example.demowaveform.waveform_pkg.PlaybackThread;
import com.example.demowaveform.waveform_pkg.RecordingThread;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.masoudss.lib.WaveformSeekBar;
import com.newventuresoftware.waveform.WaveformView;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class MainActivity2 extends AppCompatActivity {
    private WaveformView mRealtimeWaveformView;
    private RecordingThread mRecordingThread;
    private PlaybackThread mPlaybackThread;
    private static final int REQUEST_RECORD_AUDIO = 13;
    public WaveformSeekBar waveformSeekBar;
    MediaPlayer player;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mRealtimeWaveformView = (WaveformView) findViewById(R.id.waveformView);
        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
                mRealtimeWaveformView.setSamples(data);
            }
        });

        WaveformView mPlaybackView = (WaveformView) findViewById(R.id.playbackWaveformView);

        player = MediaPlayer.create(this, R.raw.song);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mRecordingThread.recording()) {
                    startAudioRecordingSafe();
                } else {
                    mRecordingThread.stopRecording();
                }
            }
        });

        short[] samples = null;
        try {
            samples = getAudioSample();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (samples != null) {
            final FloatingActionButton playFab = (FloatingActionButton) findViewById(R.id.playFab);

            mPlaybackThread = new PlaybackThread(samples, new PlaybackListener() {
                @Override
                public void onProgress(int progress) {
                    mPlaybackView.setMarkerPosition(progress);
                }

                @Override
                public void onCompletion() {
                    mPlaybackView.setMarkerPosition(mPlaybackView.getAudioLength());
                    playFab.setImageResource(android.R.drawable.ic_media_play);
                }
            });
            mPlaybackView.setChannels(1);
            mPlaybackView.setSampleRate(PlaybackThread.SAMPLE_RATE);
            mPlaybackView.setSamples(samples);

            playFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if (!mPlaybackThread.playing()) {
                        mPlaybackThread.startPlayback();
                        playFab.setImageResource(android.R.drawable.ic_media_pause);
                    } else {
                        mPlaybackThread.stopPlayback();
                        playFab.setImageResource(android.R.drawable.ic_media_play);
                    }*/

                    setWavesWhilePlaying();
                }
            });
        }
    }

    void setWavesWhilePlaying() {
        waveformSeekBar = findViewById(R.id.waveformSeekBar);
        waveformSeekBar.setSampleFrom(R.raw.song);

        player.start();

        handler.postDelayed(new Runnable() {
            public void run() {
                waveformSeekBar.setProgress(player.getCurrentPosition());
                handler.postDelayed(this, 1000);
            }
        }, 1000);
        waveformSeekBar.setMaxProgress(player.getDuration());
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecordingThread.stopRecording();
        mPlaybackThread.stopPlayback();
    }

    private short[] getAudioSample() throws IOException {
        InputStream is = getResources().openRawResource(R.raw.song);
        byte[] data;
        try {
            data = IOUtils.toByteArray(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }

        ShortBuffer sb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    private void startAudioRecordingSafe() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.startRecording();
        } else {
            requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            // Show dialog explaining why we need record audio
            Snackbar.make(mRealtimeWaveformView, "Microphone access is required in order to record audio",
                    Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity2.this, new String[]{
                            android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                }
            }).show();
        } else {
            ActivityCompat.requestPermissions(MainActivity2.this, new String[]{
                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mRecordingThread.stopRecording();
        }
    }
}