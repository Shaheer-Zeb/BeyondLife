/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import core.InputHandler;
import core.SaveData;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Bench to save the game state when interacted with
 *  - Restores the player's health full
 *  - Sets the re spawn point
 *  - Writes SaveData to disk
 * 
 * @author EDEN COMPUTERS
 */
public class Bench extends Entity{

    private static final int benchW = 50;
    private static final int benchH = 20;
    
    private static final float INTERACT_DISTANCE = 60f;
    
    private final InputHandler input;
    private boolean showPrompt = false;
    
    public Bench(float x, float y, InputHandler input){
        super(x, y, benchW, benchH, 1);
        
        this.input = input;
    }

    public boolean updateInteraction(Player player, String roomID){
        
        System.out.println("Player interacted with bench");
        
        float playerCenter = (player.getLeft() + player.getRight()) / 2f;
        float benchCenter = (getLeft() + getRight()) / 2f;
        
        float distance = Math.abs(playerCenter - benchCenter);
        
        showPrompt = (distance <= INTERACT_DISTANCE);
        
        if(showPrompt && input.isJustPressed(KeyEvent.VK_UP)){
            sit(player,  roomID);
            return true;
        }
        
        return false;
    }
    
    private void sit(Player player, String roomID){
        
        player.setHealth(player.getMaxHealth());
        
        player.spawnX = this.getLeft() + (getWidth() - player.getWidth()) / 2f;
        player.spawnY = getTop() - player.getHealth();

        // Write to disk
        SaveData data = new SaveData(player.spawnX, player.spawnY, roomID);
        data.writeToDisk();

        System.out.println("[Bench] Game saved.");
        
    }
    
    
    @Override
    public void draw(Graphics2D g, Camera cam){
        drawBench(g, cam);
        drawPrompt(g, cam);
    }
    
    //------------------- Draw Helper -----------------
    
    private void drawBench(Graphics2D g, Camera cam){
        
    }
    
    private void drawPrompt(Graphics2D g, Camera cam){
        if(showPrompt){
            //draw prompt
        }
    }
    
    @Override
    public void update(float deltaTime) {/* Konsa bench bhagtay hoai dekha hai? 😛*/}
    
}
