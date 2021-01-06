package com.roncho.game;

import com.roncho.engine.Engine;
import com.roncho.engine.World;
import com.roncho.engine.audio.AudioClip;
import com.roncho.engine.audio.AudioEngine;
import com.roncho.engine.audio.AudioStreamPlayer;
import com.roncho.engine.gl.objects.WorldObject;

public class MainActivity extends Engine {

    @Override
    public void onLoad(World world) {

        WorldObject object = new WorldObject();
        world.register(object);

        AudioClip clip = AudioClip.load("sound.wav");
        AudioStreamPlayer streamPlayer = clip.play();
        streamPlayer.stop();
        streamPlayer.resume();
    }
}