/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import core.InputHandler;
import entity.attack.Slash;
import entity.attack.SoulProjectile;
import java.awt.Graphics2D;
import java.util.List;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

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
    
    //--------------- Attack Params --------------
    public static final int MAX_SOUL = 99;
    public static final int SOUL_PER_CAST = 33;
    public static final int SOUL_PER_HIT = 11;
    
    private static final float SOUL_PROJECTILE_COOLDOWN = 0.35f;
    private float soulProjectileCooldownTimer = 0f;
    
    private int soul = 0;
    
    private static final float SLASH_COOLDOWN = 0.35f;
    private float slashCooldownTimer = 0f;
    
    private final List<Slash> slashes = new ArrayList<>();
    private final List<SoulProjectile> projectiles = new ArrayList<>();
    
    // dash state
    private boolean dashing = false;
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    
    // ------------------ Respawn Points ------------------
    public float spawnX, spawnY;
    
    private final InputHandler input;
    
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
            handleMovement();
            handleJump();
            applyGravity(deltaTime);
        }
        
        handleAttacks(deltaTime);
        
        // Movement
        setX(getLeft() + getVelX() * deltaTime);
        setY(getTop() + getVelY() * deltaTime);
        
    }
    
    //---------------- Timers --------------------
    private void handleTimers(float dt){
        if(dashCooldownTimer > 0f) dashCooldownTimer -= dt;
        if(slashCooldownTimer > 0f) slashCooldownTimer -= dt;
        if(soulProjectileCooldownTimer > 0f) soulProjectileCooldownTimer -= dt; 
        if (invincibleTimer > 0f) invincibleTimer -= dt;

        isInvincible = invincibleTimer > 0f;
        
    }
    
    // ---------------------- Dash -------------------
    private void handleDash(float dt){
        
        if(dashing){
            dashTimer -= dt;
            if(dashTimer <= 0f){
                dashing = false;
                setVelX(0);
            }
            return;
        }
        
        if(input.isJustPressed(KeyEvent.VK_SHIFT) && dashCooldownTimer <= 0f){
            System.out.println("Player is Dashing");
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

        if (input.isJustPressed(KeyEvent.VK_Z)) {

            if (onWall) {
                jumps = 0;
                setVelX(wallOnRight ? -WALL_JUMP_VX : WALL_JUMP_VX);
                setVelY(WALL_JUMP_VY);
                onWall = false;
            }
            else if (jumps < 2) {
                setVelY(JUMP_FORCE);
                jumps++;

                if (onGround)
                    onGround = false;
            }
        }
        
        // When player releases jump key early:
        //upward momentum is reduced
        //jump becomes shorter
        if (!input.isHeld(KeyEvent.VK_Z) && getVelY() < 0)
            setVelY(getVelY() + 1800f * 0.016f);
        
            
    }
    
    /**
     * Called by the room when the player lands on the ground tile
    */
    public void landOnGround() {
        onGround = true;
        jumps = 0;
        setVelY(0);
    }
    
    //--------------- Horizontal Movement ----------------
    private void handleMovement(){
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
    
    //------------- Attacks ---------------
    public int getSoul(){
        return soul;
    }
    
    private void handleAttacks(float dt){
        slashes.removeIf(s -> {
            s.update(dt);
            return !s.isActive();
        });

        projectiles.removeIf(p -> {
            p.update(dt);
            return !p.isActive();
        });
        
        if(input.isJustPressed(KeyEvent.VK_X) && slashCooldownTimer <= 0f)
            slash();
        
        if(input.isJustPressed(KeyEvent.VK_C) && soulProjectileCooldownTimer <= 0f && soul >= SOUL_PER_CAST )
            castProjectile();
    }
    
    private void slash(){
        slashCooldownTimer = SLASH_COOLDOWN;
        slashes.add(new Slash(getLeft(), getTop(), getWidth(), getHeight(), getDir()));
    }
    
    private void castProjectile(){
        if((getDir() == Direction.LEFT || getDir() == Direction.RIGHT)){
            soul -= SOUL_PER_CAST;
            soulProjectileCooldownTimer = SOUL_PROJECTILE_COOLDOWN;
            projectiles.add(new SoulProjectile(getLeft() + getWidth()/2f, getTop() + getHeight() / 2f, getDir()));
        }
    }
    
    public void gainSoul(int amount) {
        soul = Math.min(MAX_SOUL, soul + amount);
    }
    
    //------------ Getters ------------
    public boolean isDashing() {
        return dashing;
    }

    public boolean isInvincible() {
        return isInvincible;
    }

    public boolean isOnGround() {
        return onGround;
    }
    public List<Slash> getSlashes() {
        return slashes;
    }

    public List<SoulProjectile> getProjectiles() {
        return projectiles;
    }
    
    @Override
    public void draw(Graphics2D g, Camera cam) {
    
        drawPlayer(g, cam);
   
        for (Slash s : slashes)
            s.draw(g, cam);

        for (SoulProjectile p : projectiles)
            p.draw(g, cam);
        
    }
   
    //----------- Draw Helper -----------------
    private void drawPlayer(Graphics2D g, Camera cam){
        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop() - cam.offsetY;
        //draw player here
    }
    
}
