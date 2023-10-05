package com.thestbar.ludumdare54.managers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class SoundManager {
    private final AssetManager assetManager;
    private boolean isBackgroundMusicOn = false;

    public SoundManager(AssetManager assetManager) {
        this.assetManager = assetManager;
        loadAssetsToAssetManager();
    }

    public void playBackgroundMusic() {
        if (isBackgroundMusicOn) {
            return;
        }
        isBackgroundMusicOn = true;
        Music backgroundMusic = assetManager.get("music/ld54-song.wav", Music.class);
        backgroundMusic.play();
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.1f);
    }

    public void playSound(String soundName) {
        String fileName = "sfx/" + soundName + ".wav";
        Sound sound = assetManager.get(fileName, Sound.class);
        sound.play();
    }

    public boolean isBackgroundMusicOn() {
        return isBackgroundMusicOn;
    }

    private void loadAssetsToAssetManager() {
        assetManager.load("music/ld54-song.wav", Music.class);
        assetManager.load("sfx/button.wav", Sound.class);
        assetManager.load("sfx/hit.wav", Sound.class);
        assetManager.load("sfx/hurt.wav", Sound.class);
        assetManager.load("sfx/jump.wav", Sound.class);
        assetManager.load("sfx/lose.wav", Sound.class);
        assetManager.load("sfx/powerup.wav", Sound.class);
        assetManager.load("sfx/walk.wav", Sound.class);
        assetManager.load("sfx/win.wav", Sound.class);
    }
}
