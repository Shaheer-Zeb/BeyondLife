/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import core.SoundManager;
import entity.attack.Slash;
import entity.attack.SoulProjectile;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.Timer;
import java.util.Random;

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
public class Player extends Entity implements ActionListener{
    
    //------------ Characteristics --------------------
    private static final int HITBOX_WIDTH  = 42;
    private static final int HITBOX_HEIGHT = 78;
    
    //------------- Physics ------------------
    private static final float MOVE_SPEED = 220f;
    private static final float JUMP_FORCE = -790f;
    private static final float WALL_JUMP_VX = 280f;
    private static final float WALL_JUMP_VY = -480f;
    private static final float GRAVITY = 1550f;
    private static final float MAX_FALL_SPEED = 700f;
    private int jumps = 0;
    private boolean wallOnRight = true;
    
    //-------------- Dash ---------------------
    private static final float DASH_SPEED = 600;
    private static final float DASH_DURATION = 0.3f;
    private static final float DASH_COOLDOWN = 0.6f;
    private static final float INVINCIBLE_DURATION = 0.34f;
    
    //----------------- Knockback ---------------------
    private boolean knockedBack = false;
    private float knockbackTimer = 0f;
    private static final float KNOCKBACK_DURATION = 0.25f;
    
    //--------------- Attack Params --------------
    public static final int MAX_SOUL = 99;
    public static final int SOUL_PER_CAST = 33;
    public static final int SOUL_PER_HIT = 11;
    
    private static final float SOUL_PROJECTILE_COOLDOWN = 0.35f;
    private float soulProjectileCooldownTimer = 0f;
    
    private int soul = 99;
    
    private static final float SLASH_COOLDOWN = 0.35f;
    private float slashCooldownTimer = 0f;
    private Image upSlashGif = Toolkit.getDefaultToolkit().getImage("src/assets/sprites/player/upSlash.gif");
    
    private final List<Slash> slashes = new ArrayList<>();
    private final List<SoulProjectile> projectiles = new ArrayList<>();
    
    // dash state
    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;
    private Image dashEffectGif = Toolkit.getDefaultToolkit().getImage("src/assets/sprites/player/dashEffect.gif");
    
    // ------------------ Respawn Points ------------------
    public float spawnX, spawnY;
    
    //-------------- States ---------------------
    private boolean onGround = true;
    private boolean onWall = false;
    private boolean isInvincible = true;
    private float invincibleTimer = 0f;
    private boolean dashing = false;
    private boolean isSlashing = false;
    private boolean isCastingProjectile;
    private boolean isRunning = false;
    private boolean isFalling;
    private boolean isTurning = false;
    
    // ------------------ Spritesheet Handling ------------ // switches the rows in the spritesheet based on the ordinal value of the SPRITEACTION enum (basically the state of the player)
    private BufferedImage spriteSheet = AssetManager.getImage("/assets/sprites/player/playerSheet.png");
    private int sheetX, sheetY;
    private int spriteWidth = 120, spriteHeight = 82;
    
    private enum SPRITEACTION{
        ATTACK, DEATH, FALL, IDLE, JUMP, FALLINBW, ROLL, RUN, SLIDE, TURNAROUND, WALLHANG, WALLSLIDE;
    }
    private int spriteRowNumber = SPRITEACTION.IDLE.ordinal();
    private int lastSpriteRow = -1;
    private final int spriteChangeDelay = 125;
    private final Timer spriteTimer;
    private static final int DRAW_OFFSET_X = -75; // I've got to this bullshit because the spritesheet has extra padding between each sprite, so we've to separate the player collision (WIDTH and HEIGHT) and the sprite drawing width and height
    private static final int DRAW_OFFSET_Y = -174;
    private static final int DRAW_WIDTH = 192;
    private static final int DRAW_HEIGHT = 254;
    
    private final InputHandler input;
    private final Random random = new Random();
    
    public Player(float x, float y, InputHandler input){
        super(x, y, HITBOX_WIDTH, HITBOX_HEIGHT, 6);
        this.spawnX = x;
        this.spawnY = y;
        this.input = input;
        
        spriteTimer = new Timer(spriteChangeDelay, this);
        spriteTimer.start();
    }
    @Override
    public void update(float deltaTime) {
        if(isDead()) return;
        
        handleTimers(deltaTime);
        handleDash(deltaTime);
        
        if(!dashing){
            if(!knockedBack){
                handleMovement();
                handleJump(deltaTime);               
            }
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
        if(knockbackTimer > 0f) knockbackTimer -= dt;
        if(knockbackTimer <= 0f) knockedBack = false;
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
            setVelY(0);
            dash();
        }
    }
    
    private void dash(){
        if(!(getDir() == Direction.LEFT || getDir() == Direction.RIGHT)) return;
        
        dashing = true;
        dashTimer = DASH_DURATION;
        dashCooldownTimer = DASH_COOLDOWN;
        invincibleTimer = INVINCIBLE_DURATION;
        setVelX(getDir().getValue() * DASH_SPEED);
        
        String randomDashSound = (random.nextInt(2) == 1) ? "dash" : "superDash";
        SoundManager.playSfx(randomDashSound);
    }
    
    //--------------- Geavity -----------------------
    private void applyGravity(float dt){
        if(!onGround && !onWall){
            setVelY(getVelY() + GRAVITY * dt);
            if(getVelY() > MAX_FALL_SPEED) setVelY(MAX_FALL_SPEED);
        }
    }
    
    //----------------- Jump -----------------------
    
    private void handleJump(float dt){

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
                SoundManager.playSfx("playerJump");
                if (onGround)
                {
                    onGround = false;
                    isFalling = false;
                }
            }
        }
        
        // When player releases jump key early:
        //upward momentum is reduced
        //jump becomes shorter
        if (!input.isHeld(KeyEvent.VK_Z) && getVelY() < 0)
            setVelY(getVelY() + 1800f * dt);
        
        if (getVelY() >= 0 && !onGround && !onWall){
            isFalling = true;
            spriteRowNumber = SPRITEACTION.FALL.ordinal();
        }
    }
    /**
     * Called by the room when the player lands on the ground tile
    */
    public void landOnGround() {
        onGround = true;
        jumps = 0;
        setVelY(0);
        SoundManager.playSfx("playerHitGround");
    }
    //--------------- Wall Cling and Leaving -------------
    /**
     * Called by the room (when player is jumping and touches the left or right wall) to make the player cling to the wall.
     * @param wallOnRight 
     */
    public void clingToWall(boolean wallOnRight){
        this.onWall = true;
        this.wallOnRight = wallOnRight;
        this.onGround = false;
    }
    /**
     * Called by the room once player stops touching the left or right wall.
     */
    public void leaveWall(){
        this.onWall = false;
    }
    //--------------- Horizontal Movement ----------------
    private void handleMovement(){
        setVelX(0);
        
        if(input.isHeld(KeyEvent.VK_LEFT)){
            if (getDir() == Direction.RIGHT)
                isTurning = true;
            setVelX(-MOVE_SPEED);
            setDir(Direction.LEFT);
            isRunning = true;
        }
        else if(input.isHeld(KeyEvent.VK_RIGHT)){
            if (getDir() == Direction.LEFT)
                isTurning = true;
            setVelX(MOVE_SPEED);
            setDir(Direction.RIGHT);
            isRunning = true;
        }
        else if(input.isHeld(KeyEvent.VK_UP)){
            setDir(Direction.UP);
        }
        else if(input.isHeld(KeyEvent.VK_DOWN)){
            setDir(Direction.DOWN);
        }
        else
            isRunning = false;
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
        
        // Makes the player jump a little and casts the projectile
        if(input.isJustPressed(KeyEvent.VK_C) && soulProjectileCooldownTimer <= 0f && soul >= SOUL_PER_CAST ){
            setVelY(JUMP_FORCE - 100);
            onGround = false;
            castProjectile();
        }
    }
    
    private void slash(){
        slashCooldownTimer = SLASH_COOLDOWN;
        isSlashing = true;
        sheetX = 0; // it'll basically make the slash animation play from the very benninging ;)
        slashes.add(new Slash(getLeft(), getTop(), getWidth(), getHeight(), getDir()));
        SoundManager.playSfx("slash");
    }
    
    private void castProjectile(){
        if((getDir() == Direction.LEFT || getDir() == Direction.RIGHT)){
            soul -= SOUL_PER_CAST;
            soulProjectileCooldownTimer = SOUL_PROJECTILE_COOLDOWN;
            
            float cx = getLeft() + getWidth() / 2f;
            
            double dist = Math.pow(JUMP_FORCE / 2, 2) / (2 * GRAVITY); // calculates the max distance at the end of the short jump
            float cy = (float)((getTop() + getHeight() / 2f - dist));
            
            projectiles.add(new SoulProjectile(cx, cy, getDir()));
            slash();
        }
    }
    
    public void gainSoul(int amount) {
        soul = Math.min(MAX_SOUL, soul + amount);
    }
    
    public void applyKnockback(float forceX, float forceY, Camera cam) {
        cam.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
        setVelX(forceX);
        setVelY(forceY);
        onGround = false;
        knockedBack = true;
        knockbackTimer = KNOCKBACK_DURATION;
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
    /**
     * Changes the sprite after spriteChangeDelay and handles the sprites according to the player's state.
     * @param ae 
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        handleSpriteRow();
        if (spriteRowNumber != lastSpriteRow){
            sheetX = 0;
            lastSpriteRow = spriteRowNumber;
        }
        else{
            sheetX += spriteWidth;
        }
        handleSpriteRepetition();
        sheetY = spriteRowNumber * spriteHeight;
    }
    /**
     * Changes the spriteRowNumber based on the state of the player. The assets/sprites/PlayerSpriteInfo.txt file
     * mentions the row number and the number of frames for each player state sprite.
     */
    private void handleSpriteRow(){
//        if (isTurning)
//            spriteRowNumber = SPRITEACTION.TURNAROUND.ordinal();
        if (onGround)
            spriteRowNumber = SPRITEACTION.IDLE.ordinal();
        if (dashing)
            spriteRowNumber = SPRITEACTION.SLIDE.ordinal();
        if (isRunning && !dashing && onGround)
            spriteRowNumber = SPRITEACTION.RUN.ordinal();
        if (isDead())
            spriteRowNumber = SPRITEACTION.DEATH.ordinal();
        if (onWall && !onGround && getVelY() > 0)
            spriteRowNumber = SPRITEACTION.WALLSLIDE.ordinal();
        else if (!onGround && !onWall && !isFalling)
            spriteRowNumber = SPRITEACTION.JUMP.ordinal();
        if (isSlashing)
            spriteRowNumber = SPRITEACTION.ATTACK.ordinal();
    }
    /**
     * Handles the sprite repetition of each spritesheet's row. For example, the first (attack) row has 6 frames,
     * so this method will ensure that once it rendered 6 frames, it gets back to frame 1.
     */
    private void handleSpriteRepetition(){
        switch(spriteRowNumber){
            case 0 -> {
                if (sheetX > 4 * spriteWidth)
                    isSlashing = false;
            }
            case 1 -> sheetX = (sheetX > 10 * spriteWidth) ? 9 * spriteWidth : sheetX; // I didn't reset it to the 0th (1st) frame, because I don't want the death animation to repeat. You don't die twice, do you?
            case 2 -> sheetX = (sheetX > 2 * spriteWidth) ? 0 : sheetX;
            case 3 -> sheetX = (sheetX > 9 * spriteWidth) ? 0 : sheetX;
            case 4 -> sheetX = (sheetX > 2 * spriteWidth) ? 0 : sheetX;
            case 7 -> sheetX = (sheetX > 9 * spriteWidth) ? 0 : sheetX;
            case 8 -> sheetX = (sheetX > 3 * spriteWidth) ? 0 : sheetX;
            case 9 ->{
                if (sheetX > 2* spriteWidth){
                    isTurning = false;
                }
            }
            case 11 -> sheetX = (sheetX > 2 * spriteWidth) ? 0 : sheetX;
        }
    }
    /**
     * Faseeh's idea to crop the bottom half of the player to give a feeling of sitting. Called by the bench when the UP arrow is pressed.
     */
    public void cropSpriteToSit(){
        if (spriteRowNumber == SPRITEACTION.IDLE.ordinal()){
            
        }
    }
    //----------- Draw Helper -----------------
    private void drawPlayer(Graphics2D g, Camera cam) {
        int drawX = (int)(getLeft() - cam.offsetX + DRAW_OFFSET_X);
        int drawY = (int)(getTop() - cam.offsetY + DRAW_OFFSET_Y);
        
        // Body color — white when invincible/dashing, normal otherwise
        float alpha = 0.7f;
        Composite originalComposite = g.getComposite();
        if (dashing || isInvincible){
            handleDashingEffect(g, cam);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(Color.WHITE);
        }
       // if (isSlashing && (getDir() != Direction.UP && getDir() != Direction.DOWN))
       //     handleUpDownSlashGif(g, cam);
        BufferedImage spriteFrame = spriteSheet.getSubimage(sheetX, sheetY, spriteWidth, spriteHeight);
        if (onWall){
            if (wallOnRight)
                g.drawImage(spriteFrame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT, null);
            else
                g.drawImage(spriteFrame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT, null);
        }
        else if (getDir() == Direction.RIGHT)
            g.drawImage(spriteFrame, drawX, drawY, DRAW_WIDTH, DRAW_HEIGHT, null);
        else
            g.drawImage(spriteFrame, drawX + DRAW_WIDTH, drawY, -DRAW_WIDTH, DRAW_HEIGHT, null);
        g.setComposite(originalComposite);
    }
    private void handleUpDownSlashGif(Graphics2D g, Camera cam){
        int upSlashWidth = 100, upSlashHeight = 60;
        int distanceBetweenPlayerAndSlash = 7;
        int drawX = (int)(getLeft() - cam.offsetX);
        int drawY = (int)(getTop() - distanceBetweenPlayerAndSlash - cam.offsetY + DRAW_OFFSET_Y);
        g.drawImage(upSlashGif, drawX, drawY, upSlashWidth, upSlashHeight, null);
    }
    /**
     * Draws the dash effect gif either to the left or to the right of the palyer sprite.
     * @param g
     * @param cam 
     */
    private void handleDashingEffect(Graphics2D g, Camera cam){
        int dashGifSize = 100;
        int distanceBetweenPlayerAndEffect = 40;
        int drawX = (getDir() == Direction.RIGHT) ? (int)(getLeft() - cam.offsetX + DRAW_OFFSET_X - distanceBetweenPlayerAndEffect) : (int)(getRight() - cam.offsetX + DRAW_OFFSET_X) + distanceBetweenPlayerAndEffect;
        int drawY = (int)((getBottom() + getTop()) / 2 - cam.offsetY - distanceBetweenPlayerAndEffect);
        if (dashing && getDir() == Direction.LEFT)
            g.drawImage(dashEffectGif, drawX + dashGifSize / 2, drawY, dashGifSize, dashGifSize, null);
        else if (dashing && getDir() == Direction.RIGHT)
            g.drawImage(dashEffectGif, drawX + dashGifSize, drawY, -dashGifSize, dashGifSize, null);
    }
    
    //------------ Respawn ----------------
    public void respawn() {
        setX(spawnX);
        setY(spawnY);
        setVelX(0);
        setVelY(0);
        setHealth(getMaxHealth());
        soul = 0;
        setIsAlive(true);
        dashing = false;
        isInvincible = false;
        dashCooldownTimer = 0;
    }
    
}
