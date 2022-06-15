package com.example.demowaveform.audio;

public class AudioModel {
    public String audioName;
    public int audioId;
    public int isPlaying;

    public AudioModel(String audioName, int audioId, int isPlaying) {
        this.audioName = audioName;
        this.audioId = audioId;
        this.isPlaying = isPlaying;
    }
}