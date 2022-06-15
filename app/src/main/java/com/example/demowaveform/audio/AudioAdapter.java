package com.example.demowaveform.audio;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.demowaveform.R;
import com.example.demowaveform.waveform_pkg.AudioDataReceivedListener;
import com.example.demowaveform.waveform_pkg.RecordingThread;
import com.newventuresoftware.waveform.WaveformView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.MyViewHolder> {
    Context context;
    ArrayList<TrackModel> list;
    MediaPlayer mediaPlayer;
    RecordingThread mRecordingThread;
    public int selectedPosition = -1;

    public AudioAdapter(Context c, ArrayList<TrackModel> l) {
        context = c;
        list = l;
        mediaPlayer = new MediaPlayer();
    }

    @NotNull
    @Override
    public AudioAdapter.MyViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_audio, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull AudioAdapter.MyViewHolder holder, int position) {
        TrackModel trackModel = list.get(position);
        holder.tvAudioName.setText(trackModel.name);

        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
                holder.waveformView.setSamples(data);
            }
        });

        if (selectedPosition == position) {
            mRecordingThread.startRecording();
            holder.waveformView.setVisibility(View.VISIBLE);
            holder.imgPlay_Pause.setImageResource(R.drawable.ic_equliser);
        } else {
            mRecordingThread.stopRecording();
            holder.waveformView.setVisibility(View.GONE);
            holder.imgPlay_Pause.setImageResource(R.drawable.ic_audio);
        }
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    private void handleMediaPlayer(MyViewHolder holder, int position) {
        mRecordingThread = new RecordingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
                holder.waveformView.setSamples(data);
            }
        });
        if (!mediaPlayer.isPlaying()) {
            list.get(position).isPlaying = true;
            holder.imgPlay_Pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_voice));
            mediaPlayer = MediaPlayer.create(context, list.get(position).audio);
            mediaPlayer.start();
            mRecordingThread.startRecording();
            holder.waveformView.setVisibility(View.VISIBLE);
        } else {
            list.get(position).isPlaying = false;
            holder.imgPlay_Pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_voice));
            mediaPlayer.stop();
            mRecordingThread.stopRecording();
            holder.waveformView.setVisibility(View.GONE);
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                list.get(position).isPlaying = false;
                mRecordingThread.stopRecording();
                holder.imgPlay_Pause.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_voice));
                holder.waveformView.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvAudioName;
        ImageView imgPlay_Pause;
        WaveformView waveformView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAudioName = itemView.findViewById(R.id.tvAudioName);
            imgPlay_Pause = itemView.findViewById(R.id.imgPlay_Pause);
            waveformView = itemView.findViewById(R.id.waveformView);
        }
    }
}