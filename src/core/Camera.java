/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

/**
 * Follow the player with linear interpolation(LERP movement) and supports a shake effect
 * 
 * 
 * @author Faseeh Ur Rehman
 */
public class Camera {
    
    // how far the camera's left top corner has moved from origin
    public float offsetX, offsetY;
    
    private final int screenW, screenH;

    //Shake
    private float shakeDuration = 0f;
    private float shakeMagnitude = 0f;
    
    //-------------------- Shake --------------------
    public static final float SHAKE_DURATION = 1f;
    public static final float SHAKE_MAGNITUDE = 2f;
    
    private static final float LERP_SPEED = 8f;
    
    public Camera(int screenW, int screenH){
        this.screenW = screenW;
        this.screenH = screenH;
    }
    
    /**
     * Smoothly move the camera to keep targetX/targetY centered.
     * Linear interpolation -> LERP for player following
     * Passing -1 would skip the clamping (in case there is no world boundary)
     * 
     * @param targetX world x coordinate of the object the camera should follow
     * @param targetY world y coordinate of the object the camera should follow
     * @param dt delta time -> amount of time passed since last frame
     * @param worldW total Width of the Game World
     * @param wordlH total Height of the Game World
    */
    public void follow(float targetX, float targetY, float dt, float worldW, float worldH){
        
//        System.out.println("Camera following to: x = " + targetX + " y = " + targetY);
        
        float desiredX = targetX - screenW / 2f;
        float desiredY = targetY - screenH / 2f;
        
        //Lerp of the camera towards the target
        offsetX += (desiredX - offsetX) * LERP_SPEED * dt;
        offsetY += (desiredY - offsetY) * LERP_SPEED * dt;
        
        //Clamp to world boundary
        if(worldW > 0)
            offsetX = Math.max(0 , (int)Math.min(offsetX, worldW - screenW));  
        if(worldH > 0)
            offsetY = Math.max(0, (int)Math.min(offsetY, worldH - screenH));
        
        //shake
        if(shakeDuration > 0){
            shakeDuration -= dt;
            float dx = (float)(Math.random() * 2 - 1) * shakeMagnitude;
            float dy = (float)(Math.random() * 2 - 1) * shakeMagnitude;
            offsetX += dx;
            offsetY += dy;
        }
        
    }
    
    
    /**
     * Trigger a Camera Shake
     * 
     * @param duration seconds to shake
     * @param magnitude max displacement to shake
     */
    public void shake(float duration, float magnitude){
        this.shakeDuration = duration;
        this.shakeMagnitude = magnitude;
    }
    
}