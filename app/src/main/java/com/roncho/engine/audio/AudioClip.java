package com.roncho.engine.audio;

import java.util.HashMap;

public class AudioClip {
    private static final HashMap<String, AudioClip> clips = new HashMap<>();

    private final int audioId;

    private AudioClip(String file){
        audioId = AudioEngine.loadFile("audio/" + file);
        clips.put(file, this);
    }

    public boolean valid(){
       return this.audioId >= 0;
   }

    /**
     * Plays the clips, return null if the clip failed to play, or the AudioStreamPlayer that the clip is played on
     * @return
     */
    public AudioStreamPlayer play() {
       return play(1);
    }
    public AudioStreamPlayer play(float volume) {
        if(valid()) return new AudioStreamPlayer(AudioEngine.play(audioId, volume));
        return null;
    }

    public static AudioClip load(String path){
        if(clips.containsKey(path)) return clips.get(path);
        return new AudioClip(path);
    }
}
