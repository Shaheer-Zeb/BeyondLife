/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beyondlife.state;

import beyondlife.core.Camera;
import beyondlife.core.InputHandler;
import beyondlife.entity.Player;
import java.awt.Graphics2D;

/**
 * The active game play state
 * Owns the current Room, Player and Camera
 * Detects room transitions and player death, and signals the manager
 * 
 * @author EDEN COMPUTERS
 */
public class PlayingState implements GameState{

    private final InputHandler input;
    private final int screenW, screenH;
    
    private final Camera camera;
    private final Player player;
    
    private boolean playerDead = false;
    private boolean bossDefeated = false;
    private float deathTimer = 0f;
    private static final float DEATH__RESPAWN_DELAY = 2.0f;
    
    public PlayingState(InputHandler input , int screenW, int screenH){
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;
        
        camera = new Camera(screenW, screenH);
        player = new Player(120f, 380f, input);
    }
    
    
    @Override
    public void update(float deltaTime) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void draw(Graphics2D g) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public GameState nextState() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
