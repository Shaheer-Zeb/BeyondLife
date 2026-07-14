/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import core.InputHandler;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;

/**
 *
 * @author EDEN COMPUTERS
 */
public class Walker extends Entity {
    // ----------- Characteristics -----------------------
    public final static int WALKER_H = 210 / 3;
    public final static int WALKER_W = 140 / 3;
    public final static int WALKER_HP = 30;

    //--------------------- Physics -------------------------------
    private final float WALK_SPEED = 220f;
    private final int platformY;
    private final float landingTolerance = 6f; //Landing trigger range

    private final InputHandler input;

    // reference to player
    private float playerX;
    private float playerY;

    // Patrol values to enforce clamping
    private final int patrolLeftBoundary;
    private final int patrolRightBoundary;
    
    // ---------------- Fuckin GIF Handling ----------------------
    private Image walkerGif;
    
    public static enum WalkerType
    {
        SLIME, DINO
    }
    private WalkerType type;
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
    public void update(float deltaTime) {
        if (playerLandedOnPlatform()) {
            chasePlayer(deltaTime);
        } else {
            updatePatrol(deltaTime);
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
            case SLIME -> walkerGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/gauntlet/enemies/slimer.gif");
            case DINO -> walkerGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/gauntlet/enemies/dino.gif");
        }
    }
}