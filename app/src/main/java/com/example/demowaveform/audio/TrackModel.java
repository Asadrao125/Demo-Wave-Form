package com.example.demowaveform.audio;

public class TrackModel {
    public int audio;
    public String name;
    public boolean isPlaying;

    public TrackModel(int audio, String name, boolean isPlaying) {
        this.audio = audio;
        this.name = name;
        this.isPlaying = isPlaying;
    }
}
