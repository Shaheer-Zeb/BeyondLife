/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import core.InputHandler;
import core.SaveData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
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
        
        float playerCenter = (player.getLeft() + player.getRight()) / 2f;
        float benchCenter = (getLeft() + getRight()) / 2f;
        
        float distance = Math.abs(playerCenter - benchCenter);
        
        showPrompt = (distance <= INTERACT_DISTANCE);
        
        if(showPrompt && input.isJustPressed(KeyEvent.VK_UP)){
            System.out.println("Player interacted with bench");
            sit(player,  roomID);
            return true;
        }
        
        return false;
    }
    
    private void sit(Player player, String roomID){
        
        player.setHealth(player.getMaxHealth());
        
        player.spawnX = this.getLeft() + (getWidth() - player.getWidth()) / 2f;
        player.spawnY = getTop() - player.getHeight();

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
    
    private void drawBench(Graphics2D g, Camera cam) {
        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop()  - cam.offsetY;

        // Legs
        g.setColor(new Color(90, 70, 50));
        g.fillRect((int) drawX + 4, (int) drawY + benchH - 6, 6, 6);
        g.fillRect((int) drawX + benchW - 10, (int) drawY + benchH - 6, 6, 6);

        // Seat
        g.setColor(new Color(120, 90, 60));
        g.fillRect((int) drawX, (int) drawY, benchW, benchH - 6);

        // Seat outline
        g.setColor(new Color(80, 60, 40));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect((int) drawX, (int) drawY, benchW, benchH - 6);
        g.setStroke(new BasicStroke(1f));
    }
    
    private void drawPrompt(Graphics2D g, Camera cam) {
        if (!showPrompt) return;

        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop()  - cam.offsetY;

        String prompt = "[UP Arrow Key] Rest";
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        int textW = g.getFontMetrics().stringWidth(prompt);

        int boxX = (int)(drawX + benchW / 2f - textW / 2f) - 6;
        int boxY = (int) drawY - 30;
        int boxW = textW + 12;
        int boxH = 18;

        // Box background
        g.setColor(new Color(20, 15, 30, 200));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Box outline
        g.setColor(new Color(140, 120, 180));
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 6, 6);

        // Text
        g.setColor(new Color(220, 210, 240));
        g.drawString(prompt, boxX + 6, boxY + 13);
    }
    
    @Override
    public void update(float deltaTime) {/* Konsa bench bhagtay hoai dekha hai? 😛*/}
    
}
