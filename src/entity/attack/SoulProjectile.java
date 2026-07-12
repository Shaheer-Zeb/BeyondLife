/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity.attack;

import core.AssetManager;
import core.Camera;
import entity.Direction;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 * Ranged Attack fired by the player. Costs Soul
 * Travels horizontally until it hits a wall or expires
 * 
 * @author EDEN COMPUTERS
 */
public class SoulProjectile {
    
    public static final int DAMAGE = 8;
    private static final float SPEED = 400f;
    private static final float LIFETIME = 1.8f;
    private static final int RADIUS = 8 * 4;
    
    public float x, y;
    private final float velX;
    
    private boolean active = true;
    private float timer = LIFETIME;
    
    private Image image = AssetManager.getImage("/assets/sprites/player/projectile/projectile.png");
    
    
    /**
    * Creates a new soul projectile fired from the player's position.
    *
    * @param cx the projectile's starting center x-coordinate
    * @param cy the projectile's starting center y-coordinate
    * @param dir the horizontal direction in which the projectile travels
    */
    public SoulProjectile(float cx, float cy, Direction dir){
        x = cx - RADIUS;
        y = cy - RADIUS;
        velX = SPEED * dir.getValue();
    }
    
    public boolean isActive() { return active; }
    public void deactivate(){
        active = false;
    }
    
    public void update(float dt){
        timer -= dt;
        if(timer <= 0){
            active = false;
            return;
        }
        
        x+= velX * dt;
    }
    
     /**
      * Checks whether this projectile's hit box overlaps the given rectangle using AABB collision detection
      * 
      * @param rx x - coordinate of the other rectangle 
      * @param ry y - coordinate of the other rectangle
      * @param rw width of the of the other rectangle
      * @param rh height of the other rectangle
      * 
      * @return whether the two rectangles overlap
     */
    public boolean overlapsRect(float rx, float ry, float rw, float rh) {
        return x < rx + rw &&
               x + RADIUS * 2 > rx &&
               y < ry + rh &&
               y + RADIUS * 2 > ry;
    }
    
    public void draw(Graphics2D g, Camera cam){
        if(!active)
            return;
        
        drawSoulProjectile(g, cam);
    }
    
    //------------ Draw Helpers ------------
    private void drawSoulProjectile(Graphics2D g, Camera cam){
        int drawX = (int)(x - cam.offsetX);
        int drawY = (int)(y - cam.offsetY);
        g.drawImage(image, drawX, drawY, RADIUS * 2, RADIUS * 2, null);
    }
}
