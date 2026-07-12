/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import java.awt.Graphics2D;

/**
 *
 * @author EDEN COMPUTERS
 */
public abstract class Entity {
    
    private float x, y;
    private float velX, velY;
    private int width, height;
    
    private int maxHealth;
    private int health;
    private boolean isAlive;
    
    private Direction dir = Direction.RIGHT;
    
    /**
     * Constructor to initialize the entity
     * 
     * @param x world x coordinate of the entity
     * @param y world y coordinate of the entity
     * @param width body width of the entity
     * @param height body height of the entity
     * @param maxHealth maximum health as well as initial health
     */
    public Entity(float x, float y, int width, int height, int maxHealth){
        
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.isAlive = true;
        
    }
    
    /**
     * update entity's state once per game tick i.e move, apply gravity etc
     * 
     * @param deltaTime the time passed since last frame - to make the game fps independent
     */
    public abstract void update(float deltaTime);
    
    /**
     * render the entity
     * 
     * @param g drawing object
     * @param cam the active camera used to convert the entity's
     *            world coordinates into screen coordinates by applying the camera offsets
     */
    public abstract void draw(Graphics2D g, Camera cam);

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVelX() {
        return velX;
    }

    public void setVelX(float velX) {
        this.velX = velX;
    }

    public float getVelY() {
        return velY;
    }

    public void setVelY(float velY) {
        this.velY = velY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isDead() {
        return !isAlive;
    }

    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public Direction getDir() {
        return dir;
    }

    public void setDir(Direction dir) {
        this.dir = dir;
    }
    
    
    public float getLeft(){
        return x;
    }
    
    public float getRight(){
        return x + width;
    }
    
    public float getTop(){
        return y;
    }
    
    public float getBottom(){
        return y + height;
    }
    
    /**
     * Apply damage and return true if this entity just died.
     * 
     * @param amount the damage taken
     * @return entity is dead
    */
    public boolean takeDamage(int amount) {
        health -= amount;
        
        if(health <= 0){
            health = 0;
            isAlive = false;
        }
        
        return isDead();
    }
    
    /**
     * simple AABB overlap check against other entity
     * 
     * @param other
     * @return if the entity's overlap
     */
    public boolean overlap(Entity other){
        return getRight() > other.getLeft() &&
                getLeft() < other.getRight() &&
                getBottom() > other.getTop() &&
                getTop() < other.getBottom();
    }
    
}
