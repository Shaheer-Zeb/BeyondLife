/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import java.awt.Graphics2D;
import java.awt.Image;

/**
 *
 * @author EDEN COMPUTERS
 */
public class Walker extends Entity {
    // ----------- Characteristics -----------------------
    public final static int WALKER_H = 210 / 3;
    public final static int WALKER_W = 140 / 3;
    public final static int WALKER_HP = 20;

    //--------------------- Physics -------------------------------
    private final float WALK_SPEED = 160f;
    private final int platformY;
    private final float landingTolerance = 6f; //Landing trigger range

    private final InputHandler input;

    // reference to player
    private float playerX;
    private float playerY;

    // Patrol values to enforce clamping
    private final int patrolLeftBoundary;
    private final int patrolRightBoundary;
    
    private static final float DAMAGE_MAX_TIME = 0.8f;
    private float dmgTimer = 0;
    
    // ---------------- GIF Handling ----------------------
    private Image walkerGif, walkerWalkGif, walkerHitGif, walkerDeathGif;
    
    public static enum WalkerType
    {
        SLIME, DINO
    }
    private enum State
    {
        Walking, Hit, Death
    }
    private WalkerType type;
    private State state = State.Walking;
    
    // ------------ Death Stuff -----------
    private static final float DEATH_DURATION = 0.5f;
    private float deathTimer = 0f;
    private boolean isRemovable = false; // basicaly, it'd become true once the deathTimer is up, and now the GauntletRoom and the GauntletRoom2 update and draw the walkers based on if they are removable, and they don't update or draw them if they are marked removable 
    
    public Walker(float x, float y, int platformY, InputHandler input, int patrolLeftBoundary, int patrolRightBoundary, WalkerType type) {
        super(x, y, WALKER_W, WALKER_H, WALKER_HP);
        this.platformY = platformY;
        this.input = input;
        this.patrolLeftBoundary = patrolLeftBoundary;
        this.patrolRightBoundary = patrolRightBoundary;
        this.type = type;
       
        loadGif();
    }

    public void setPlayerPosition(float playerX, float playerY) {
        this.playerX = playerX;
        this.playerY = playerY;
    }

    /** True once the player is horizontally within patrol range AND standing on this platform. */
    private boolean playerLandedOnPlatform() {
        boolean withinX = playerX > patrolLeftBoundary && playerX < patrolRightBoundary;
        boolean onSameLevel = Math.abs(playerY - platformY) <= landingTolerance;
        return withinX && onSameLevel;
    }

    /** Clamps the Walker to its platform and flips patrol direction at the edges. */
    private void enforceScreenBounds() {
        if (getLeft() < patrolLeftBoundary) {
            setX(patrolLeftBoundary);
            setVelX(0);
            setDir(Direction.RIGHT);
        }
        if (getLeft() > patrolRightBoundary - WALKER_W) {
            setX(patrolRightBoundary - WALKER_W);
            setVelX(0);
            setDir(Direction.LEFT);
        }
    }

    /** Patrols back and forth on the platform when the player hasn't landed on it. */
    private void updatePatrol(float dt) {
        enforceScreenBounds();
        setX(getLeft() + WALK_SPEED * getDir().getValue() * dt);
    }

    /** Moves toward the player's X position once they've landed on the platform. */
    private void chasePlayer(float dt) {
        setDir(playerX > getLeft() ? Direction.RIGHT : Direction.LEFT);
        setX(getLeft() + WALK_SPEED * getDir().getValue() * dt);
        enforceScreenBounds(); 
    }

    @Override
    public boolean takeDamage(int amount) {
        dmgTimer = DAMAGE_MAX_TIME;
        return super.takeDamage(amount);
    }
    

    @Override
    public void update(float deltaTime) {
        updateState();
        updateGif();
        
        if (state == State.Death){
            deathTimer -= deltaTime;

            if (deathTimer <= 0f){
                 isRemovable = true;
            }

        return;
        }
        if(dmgTimer > 0){ 
            dmgTimer -= deltaTime;
            setX(playerX < getLeft() ? getLeft() + 10 : getLeft() - 10);
        }
        
        if (playerLandedOnPlatform()) {
            chasePlayer(deltaTime);
        } else {
            updatePatrol(deltaTime);
        }
    }
    private void updateState() {
        if (state == State.Death)
            return;
        if (isDead()){
            state = State.Death;
            deathTimer = DEATH_DURATION;
            return;
        }
        if (dmgTimer > 0f) {
            state = State.Hit;
            return;
        }
        state = State.Walking;
    }
    private void updateGif(){
        switch (state)
        {
            case Walking -> walkerGif = walkerWalkGif;
            case Hit -> walkerGif = walkerHitGif;
            case Death -> walkerGif = walkerDeathGif;
        }
    }
    @Override
    public void draw(Graphics2D g, Camera cam) {
        int drawX = (int)(getLeft() - cam.offsetX);
        int drawY = (int)(getTop() - cam.offsetY);
        if (getDir() == Direction.LEFT)
            g.drawImage(walkerGif, drawX, drawY, getWidth(), getHeight(), null);
        else if (getDir() == Direction.RIGHT)
            g.drawImage(walkerGif, drawX + getWidth(), drawY, -getWidth(), getHeight(), null);
    }
    private void loadGif(){
        switch (type)
        {
            case SLIME -> 
            {
                walkerWalkGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/slimer/slime_walk_anim.gif");
                walkerHitGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/slimer/slime_hit_anim.gif");
                walkerDeathGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/slimer/slime_death_anim.gif");
            }
            case DINO -> 
            {
                walkerWalkGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/dino/dino_walk_anim.gif");
                walkerHitGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/dino/dino_hit_anim.gif");
                walkerDeathGif = AssetManager.getGif("/assets/rooms/gauntlet/enemies/dino/dino_death_anim.gif");
            }
        }
    }
    public boolean isRemovable(){ return isRemovable; }
}