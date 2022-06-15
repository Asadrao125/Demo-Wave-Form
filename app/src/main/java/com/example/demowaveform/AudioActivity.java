package com.example.demowaveform;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;

import com.example.demowaveform.audio.AudioAdapter;
import com.example.demowaveform.audio.AudioModel;
import com.example.demowaveform.audio.RecyclerItemClickListener;
import com.example.demowaveform.audio.TrackModel;

import java.util.ArrayList;

public class AudioActivity extends AppCompatActivity {
    MediaPlayer mediaPlayer;
    AudioAdapter audioAdapter;
    RecyclerView recyclerViewAudio;
    ArrayList<TrackModel> audioList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);

        recyclerViewAudio = findViewById(R.id.recyclerViewAudio);
        recyclerViewAudio.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAudio.setHasFixedSize(true);

        setAdapter();

        recyclerViewAudio.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(),
                recyclerViewAudio, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                audioAdapter.selectedPosition = position;
                audioAdapter.notifyDataSetChanged();

                TrackModel trackModel = audioList.get(position);
                if (mediaPlayer != null) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        trackModel.isPlaying = false;
                        audioAdapter.notifyDataSetChanged();
                    }
                }

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.reset();
                    trackModel.isPlaying = false;
                } else {
                    mediaPlayer = MediaPlayer.create(AudioActivity.this, trackModel.audio);
                    mediaPlayer.start();
                    trackModel.isPlaying = true;
                }

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        trackModel.isPlaying = false;
                        audioAdapter.selectedPosition = -1;
                        audioAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onItemLongClick(View view, int position) {
                System.out.println("-- item long press pos: " + position);
            }
        }));
    }

    private void setAdapter() {
        mediaPlayer = new MediaPlayer();
        audioList.clear();
        audioList.add(new TrackModel(R.raw.song, "Audio 1", false));
        audioList.add(new TrackModel(R.raw.sample1, "Audio 2", false));
        audioList.add(new TrackModel(R.raw.sample2, "Audio 3", false));
        audioList.add(new TrackModel(R.raw.song, "Audio 1", false));
        audioList.add(new TrackModel(R.raw.sample1, "Audio 2", false));
        audioList.add(new TrackModel(R.raw.sample2, "Audio 3", false));

        audioList.add(new TrackModel(R.raw.song, "Audio 1", false));
        audioList.add(new TrackModel(R.raw.sample1, "Audio 2", false));
        audioList.add(new TrackModel(R.raw.sample2, "Audio 3", false));
        audioList.add(new TrackModel(R.raw.song, "Audio 1", false));
        audioList.add(new TrackModel(R.raw.sample1, "Audio 2", false));
        audioList.add(new TrackModel(R.raw.sample2, "Audio 3", false));

        audioAdapter = new AudioAdapter(this, audioList);
        recyclerViewAudio.setAdapter(audioAdapter);
    }
}