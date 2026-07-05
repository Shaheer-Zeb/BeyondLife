/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity.attack;

import core.Camera;
import entity.Direction;
import java.awt.Graphics2D;

/**
 *
 * @author EDEN COMPUTERS
 */
public class Slash {

    private float x, y;
    private final int playerW;
    private final int playerH;
    private final Direction dir;
    
    private static final float LIFETIME = 0.15f;
    private boolean active = true;
    
    private float timer = LIFETIME;
    
    private static final int REACH = 50;
    private int width, height;

    
    /**
     * Creates a new melee slash originating from the player's current position
     * The slash's attributes are calculated based on player's.
     * 
     * 
     * @param playerX player's x - coordinate in the world's space
     * @param playerY player's y - coordinate in the world's space
     * @param playerW player's width
     * @param playerH player's height
     * @param dir player's the direction of attack
     */
    public Slash(float playerX, float playerY, int playerW, int playerH, Direction dir) {
       this.playerW = playerW;
       this.playerH = playerH;
       this.dir = dir;

       switch (dir) {
           case RIGHT, LEFT -> {
               width  = (int)(1.7f * playerW);
               height = (int)(0.6f * playerH);
           }

           case UP, DOWN -> {
               width  = (int)(0.6f * playerW);
               height = (int)(1.7f * playerH);
           }
       }
       
        calculateXY(playerX, playerY);

   }

    public void updatePosition(float newplayerX, float newplayerY){
        calculateXY(newplayerX, newplayerY);
    }
    
    /**
     * Calculates and updates the slash's world position based on the player's
     * current position and direction of the attack
     * 
     * @param playerX player's x coordinate in world space
     * @param playerY player's y coordinate in world space
     */
    private void calculateXY(float playerX, float playerY){
       float centerX = playerX + playerW / 2f;
       float centerY = playerY + playerH / 2f;

       switch (dir) {
           case RIGHT -> {
               x = centerX + REACH - width / 2f;
               y = centerY - height / 2f;
           }
           case LEFT -> {
               x = centerX - REACH - width / 2f;
               y = centerY - height / 2f;
           }
           case UP -> {
               x = centerX - width / 2f;
               y = centerY - REACH - height / 2f;
           }
           case DOWN -> {
               x = centerX - width / 2f;
               y = centerY + REACH - height / 2f;
           }
       }
    }

    public void update(float dt){
        timer -= dt;
        if(timer <= 0) active = false;
    }

    public boolean isActive() { return active; }
    public void deactivate(){
        active = false;
    }

    /**
     * Checks whether this slash's hit box overlaps the given rectangle using AABB collision detection
     * 
     * @param rx x - coordinate of the other rectangle 
     * @param ry y - coordinate of the other rectangle
     * @param rw width of the of the other rectangle
     * @param rh height of the other rectangle
     * 
     * @return whether the two rectangles overlap
     */
    public boolean overlapsRect(float rx, float ry, float rw, float rh) {
      return  x < rx + rw &&
              x + width > rx && 
              y < ry + rh &&
              y + height > ry;
    }

    public void draw(Graphics2D g, Camera cam){
        
        if(!active) return;
        drawSlash(g, cam);

    }
    
    //---------- Draw Helper ---------------
    private void drawSlash(Graphics2D g, Camera cam){
        float drawX = x - cam.offsetX;
        float drawY = y - cam.offsetY;
        
//        g.drawRect((int)drawX, (int)drawY, width, height);
    }
    
}
