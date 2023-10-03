package com.thestbar.ludumdare54.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public final class SoundManager {
    private static Music backgroundMusic;
    static {
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("music/ld54-song.wav"));
    }

    public static Sound hitSound = Gdx.audio.newSound(Gdx.files.internal("sfx/hit.wav"));
    public static Sound hurtSound = Gdx.audio.newSound(Gdx.files.internal("sfx/hurt.wav"));
    public static Sound jumpSound = Gdx.audio.newSound(Gdx.files.internal("sfx/jump.wav"));
    public static Sound loseSound = Gdx.audio.newSound(Gdx.files.internal("sfx/lose.wav"));
    public static Sound walkSound = Gdx.audio.newSound(Gdx.files.internal("sfx/walk.wav"));

    private static long activeWalkSoundId = -1;

    public static void startMusic() {
        if (backgroundMusic.isPlaying()) {
            return;
        }
        backgroundMusic.setVolume(0.1f);
        backgroundMusic.setLooping(true);
        backgroundMusic.play();
    }

    public static void restartMusic() {
        if (!backgroundMusic.isPlaying()) {
            return;
        }
        backgroundMusic.stop();
        SoundManager.startMusic();
    }

    public static void stopMusic() {
        if (backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    public static void dispose() {
        activeWalkSoundId = -1;
        backgroundMusic.dispose();
        hitSound.dispose();
        hurtSound.dispose();
        jumpSound.dispose();
        loseSound.dispose();
        walkSound.dispose();
    }


}
