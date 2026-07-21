/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.*;

/**
 *
 * @author ShaheerZK
 */
public class SoundManager {
    private static Map<String, Clip> music = new HashMap<>();
    private static Map<String, Clip> sfx = new HashMap<>();
    private static Clip currentMusic;
    
    /**
     * Loads the music clip in the music HashMap.
     * @param id
     * @param path 
     */
    private static void loadMusic(String id, String path){
        music.put(id, loadClip(path));
    }
    /**
     * Loads the SFX to the sfx HashMap.
     * @param id
     * @param path 
     */
    private static void loadSfx(String id, String path){
        sfx.put(id, loadClip(path));
    }
    /**
     * A helper for loading the clip.
     * @param path
     * @return 
     */
    private static Clip loadClip(String path){
        try
        {
            InputStream is = SoundManager.class.getResourceAsStream(path);
            if (is == null)
                throw new Exception("Music sound not found mate. " + path);
            
            BufferedInputStream bis = new BufferedInputStream(is);
            AudioInputStream audio = AudioSystem.getAudioInputStream(bis);
            
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            
            return clip;
        } 
        catch (Exception ex) 
        {
            System.getLogger(SoundManager.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return null;
    }
    /**
     * Starts playing the music just by taking the ID (name) of the music if it's loaded in the HashMap. Let's say SoundManager.playMusic("village");
     * I was intending to organize it more by having public static Strings in this class, so that the user can just SoundManager.playMusic(SoundManager.village);
     * But let it stay as it is for now.
     * Called by the constructors of the respective sound track players. For example, the StartScreen construcror plays the startScreen music.
     * @param id 
     */
    public static void playMusic(String id){
        Clip clip = music.get(id);
        if (clip == null)
            return;
        stopMusic();
        
        currentMusic = clip;
        currentMusic.setFramePosition(0);
        currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
        currentMusic.start();
    }
    /**
     * Stops the music and resets the time played.
     */
    public static void stopMusic(){
        if (currentMusic == null)
            return;
        currentMusic.stop();
        currentMusic.setFramePosition(0);
    }
    /**
     * Merely stops the current music.
     */
    public static void pauseMusic(){
        if (currentMusic == null)
            return;
        currentMusic.stop();
    }
    /**
     * Resumes the music. I don't think we'd ever stop a music in our game, but still, let it stay.
     */
    public static void resumeMusic(){
        if (currentMusic == null)
            return;
        currentMusic.start();
    }
    /**
     * Simply loads the SFX from the HashMap if it exists. And play it. We don't have a stop method for SFX since it wouldn't last long.
     * @param id 
     */
    public static void playSfx(String id){
        Clip clip = sfx.get(id);
        if (clip == null)
            return;
        
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
    /**
     * Changes the volume of the currently playing music. The argument takes the decibels in float. For example, if I pass -10, it'd reduce the volume by 10 dB.
     * @param volume 
     */
    public static void setMusicVolume(float volume){
        if (currentMusic == null)
            return;
        
        FloatControl control = (FloatControl)currentMusic.getControl(FloatControl.Type.MASTER_GAIN);
        control.setValue(volume);
    }
    /**
     * Changes the SFX volume for all the loaded SFXs.
     * @param volume 
     */
    public static void setSfxVolume(float volume){
        for (Clip clip : sfx.values()){
            FloatControl control = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
            control.setValue(volume);
        }
    }
    public static void stopAllSfx(){
        for (Clip clip : sfx.values()){
            clip.stop();
            clip.setFramePosition(0);
        }
    }
    /**
     * Loads all the music and SFX files. Called from the GamePanel. 
     */
    public static void fireSoundManager(){
        loadAllMusic();
        loadAllSfx();
    }
    private static void loadAllMusic(){
        loadMusic("startScreen", "/assets/audio/music/startScreenCompressed.wav");
        loadMusic("village", "/assets/audio/music/villageBackground.wav");
        loadMusic("boss", "/assets/audio/music/bossBackgroundCompressed.wav");
        loadMusic("winScreen", "/assets/audio/music/winScreenCompressed.wav");
        loadMusic("gauntlet", "/assets/audio/music/gauntletBackground.wav");
        loadMusic("gauntlet2", "/assets/audio/music/gauntlet2Background.wav");
        loadMusic("shaft", "/assets/audio/music/shaftBackground.wav");
    }
    private static void loadAllSfx(){
        loadSfx("villageLife", "/assets/audio/sfx/environment/villageLife.wav");
        loadSfx("benchRest", "/assets/audio/sfx/environment/benchRest.wav");
        loadSfx("doorOpen", "/assets/audio/sfx/environment/doorOpen.wav");
        
        loadPlayerSfx();
        loadBossSfx();
    }
    private static void loadPlayerSfx(){
        loadSfx("damagePlayer", "/assets/audio/sfx/player/hero_damage.wav");
        loadSfx("dash", "/assets/audio/sfx/player/hero_dash.wav");
        loadSfx("playerDeath", "/assets/audio/sfx/player/hero_death_extra_details.wav");
        loadSfx("playerFalling", "/assets/audio/sfx/player/hero_falling.wav");
        loadSfx("playerJump", "/assets/audio/sfx/player/hero_jump.wav");
        loadSfx("playerHitGround", "/assets/audio/sfx/player/hero_land_hard.wav");
        loadSfx("playerRunFootsteps", "/assets/audio/sfx/player/hero_run_footsteps_grass.wav");
        loadSfx("wallSlide", "/assets/audio/sfx/player/hero_wall_slide.wav");
        loadSfx("slash", "/assets/audio/sfx/player/hero_slash.wav");
    }
    private static void loadBossSfx(){
        loadSfx("slam", "/assets/audio/sfx/boss/strikeGround.wav");
        loadSfx("leap", "/assets/audio/sfx/boss/jump.wav");
        loadSfx("damageBoss", "/assets/audio/sfx/boss/getBodyDamage.wav");
        loadSfx("fireBolt", "/assets/audio/sfx/boss/bossBolt.wav");
        loadSfx("bossDash", "/assets/audio/sfx/boss/dash.wav");
    }
}
