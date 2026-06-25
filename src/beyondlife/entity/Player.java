/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beyondlife.entity;

import beyondlife.core.Camera;
import beyondlife.core.InputHandler;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 *The Player Character 
 * 
 * Abilities
 * Movement
 * Jump (Double Jump as well)
 * Wall cling + wall jump
 * Dash grants invincibility
 * Slash Attack
 * Soul Projectile(cost soul)
 * 
 * @author EDEN COMPUTERS
 */
public class Player extends Entity{
    
    //------------ Characteristics --------------------
    private static final int PLAYER_HEIGHT = 32;
    private static final int PLAYER_WIDTH = 48;
    
    //------------- Physics ------------------
    private static final float MOVE_SPEED = 220f;
    private static final float JUMP_FORCE = -520f;
    private static final float WALL_JUMP_VX = 280f;
    private static final float WALL_JUMP_VY = -480f;
    private static final float GRAVITY = 1100f;
    private static final float MAX_FALL_SPEED = 700f;
    private int jumps = 0;
    private boolean wallOnRight = true;
    
    //-------------- Dash ---------------------
    private static final float DASH_SPEED = 480f;
    private static final float DASH_DURATION = 0.18f;
    private static final float DASH_COOLDOWN = 0.6f;
    private static final float INVINCIBLE_DURATION = 0.22f;
    
    //-------------- State ---------------------
    private boolean onGround = true;
    private boolean onWall = false;
    private boolean isInvincible = true;
    private float invincibleTimer = 0f;
    
    // dash state
    private boolean dashing = false;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    
    // ------------------ Respawn Points ------------------
    public float spawnX, spawnY;
    
    private InputHandler input;
    
    public Player(float x, float y, InputHandler input){
        super(x, y, PLAYER_WIDTH ,PLAYER_HEIGHT, 6);
        this.spawnX = x;
        this.spawnY = y;
        this.input = input;
    }

    @Override
    public void update(float deltaTime) {
        if(isDead()) return;
        
        handleTimers(deltaTime);
        handleDash(deltaTime);
        
        if(!dashing){
            handleMovement(deltaTime);
            handleJump();
            applyGravity(deltaTime);
        }
        
        // Movement
        setX(getLeft() + getVelX() * deltaTime);
        setY(getTop() + getVelY() * deltaTime);
        
    }
    
    //---------------- Timers --------------------
    private void handleTimers(float dt){
        if(dashCooldownTimer > 0f) dashCooldownTimer -= dt;
        if(invincibleTimer > 0f){
            invincibleTimer -= dt;
            isInvincible = true;
        } 
        else{
            isInvincible = false;
        }
        
    }
    
    // ---------------------- Dash -------------------
    private void handleDash(float dt){
        System.out.println("Player is DAshing");
        
        if(dashing){
            dashTimer -= dt;
            if(dashTimer <= 0f){
                dashing = false;
                setVelX(0);
            }
            return;
        }
        
        if(input.isJustPressed(KeyEvent.VK_SHIFT) && dashCooldownTimer <= 0f){
            dash();
        }
    }
    
    private void dash(){
        dashing = true;
        dashTimer = DASH_DURATION;
        dashCooldownTimer =  DASH_COOLDOWN;
        invincibleTimer = INVINCIBLE_DURATION;
        setVelX(getDir().getValue() * DASH_SPEED);
    }
    
    //--------------- Geavity -----------------------
    private void applyGravity(float dt){
        if(!onGround && !onWall){
            setVelY(getVelY() + GRAVITY * dt);
            if(getVelY() > MAX_FALL_SPEED) setVelY(MAX_FALL_SPEED);
        }
    }
    
    //----------------- Jump -----------------------
    
    private void handleJump(){

        if(input.isJustPressed(KeyEvent.VK_Z) && jumps < 2){
            setVelX(JUMP_FORCE);
            jumps++;
            if(onGround) onGround = false;
        }
        else if(onWall){
            jumps = 0;
            setVelX(wallOnRight ? -WALL_JUMP_VX : WALL_JUMP_VX);
            setVelY(WALL_JUMP_VY);
            setVelX(WALL_JUMP_VX);
            onWall = false;
        }
        
        // When player releases jump key early:
        //upward momentum is reduced
        //jump becomes shorter
        if (!input.isHeld(KeyEvent.VK_Z) && getVelY() < 0)
            setVelY(getVelY() + 1800f * 0.016f);
        
            
    }
    
    //--------------- Horizontal Movement ----------------
    private void handleMovement(float dt){
        setVelX(0);
        
        if(input.isHeld(KeyEvent.VK_LEFT)){
            setVelX(-MOVE_SPEED);
            setDir(Direction.LEFT);
        }
        else if(input.isHeld(KeyEvent.VK_RIGHT)){
            setVelX(MOVE_SPEED);
            setDir(Direction.RIGHT);
        }
        else if(input.isHeld(KeyEvent.VK_UP)){
            setDir(Direction.UP);
        }
        else if(input.isHeld(KeyEvent.VK_DOWN)){
            setDir(Direction.DOWN);
        }
        
        if(onWall && !onGround){
            if(getVelY() > 100f){
                setVelY(100f);
            }
        }
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
    
    }
    
    
}
