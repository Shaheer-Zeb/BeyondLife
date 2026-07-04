/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.Camera;
import core.InputHandler;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * non hostile Non playable character.
 * Cycle through its dialogues one press at a time
 * 
 * @author EDEN COMPUTERS
 */
public class NPC extends Entity{

    private final String name;
    private final String[] dialogueLines; 
    private int lineIndex = 0;
    private boolean talking = false;
    
    private static final float INTERACT_DISTANCE = 80f;
    
    private final InputHandler input;
    
    public NPC(String name, float x, float y, int npc_height, int npc_width, InputHandler input, String... dialogueLines){
        super(x, y, npc_width, npc_height, 1);
        this.dialogueLines = dialogueLines;
        this.input = input;
        this.name = name;
        
    }
    
    public boolean isTalking(){
        return talking;
    }
    
    /**
     * moves to next dialogue when up arrow key is pressed
     * 
     * @param playerCenterX players objects center X coordinate
     * @param playerCenterY player objects center Y coordinate
     */
    public void updateInteraction(float playerCenterX, float playerCenterY){
        
        System.out.println("Player interacted with NPC");
        
        float npcCenter = (getLeft() + getRight()) / 2f;
        float distance = Math.abs(playerCenterX - npcCenter);
        
        if(distance <= INTERACT_DISTANCE && input.isJustPressed(KeyEvent.VK_UP)){
            
            if(!talking ){
                talking = true;
                lineIndex = 0;
            }
            else{
                lineIndex++;
                if(lineIndex >= dialogueLines.length){
                    lineIndex = 0;
                    talking = false;
                }
            }
        }
        
        //proximity check
        
        if(distance > INTERACT_DISTANCE * 2){
            talking = false;
            lineIndex = 0;
        }
    }
    
    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawNPC(g, cam);
        
        if(talking && lineIndex < dialogueLines.length)
            drawSpeechState(g, cam ,dialogueLines[lineIndex]);
    }
    
    //------------------- Draw Helpers ---------------------
    private void drawNPC(Graphics2D g, Camera cam) {
        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop()  - cam.offsetY;

        // Body
        g.setColor(new Color(140, 110, 170));
        g.fillRect((int) drawX, (int) drawY, getWidth(), getHeight());
        
        g.setFont(new Font("Monospaced", Font.BOLD, 12));
        g.setColor(Color.BLACK);
        int lineW = g.getFontMetrics().stringWidth(name) ;
        g.drawString(name, (int)(drawX + getWidth()/2f - lineW/2f), (int)(drawY + getHeight() + 18));
        
        
        // Interact prompt when not already talking
        if (!talking) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 11));
            g.setColor(new Color(200, 185, 220));
            String hint = "[Up Arrow Key]";
            int hintW = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, (int)(drawX + getWidth() / 2f - hintW / 2f), (int) drawY - 8);
        }
    }
    
    private void drawSpeechState(Graphics2D g, Camera cam, String dialogue) {
        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop()  - cam.offsetY;

        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(dialogue);
        int textH = fm.getHeight();

        int padding = 8;
        int boxW = textW + padding * 2;
        int boxH = textH + padding;
        int boxX = (int)(drawX + getWidth() / 2f - boxW / 2f);
        int boxY = (int) drawY - boxH - 20;

        // Box background
        g.setColor(new Color(20, 15, 30, 220));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 8, 8);

        // Box outline
        g.setColor(new Color(160, 140, 200));
        g.setStroke(new BasicStroke(1.2f));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 8, 8);
        g.setStroke(new BasicStroke(1f));

        // Small triangle tail pointing down toward NPC
        int tailX = (int)(drawX + getWidth() / 2f);
        int tailY  = boxY + boxH;
        g.setColor(new Color(20, 15, 30, 220));
        g.fillPolygon(
            new int[]{ tailX - 5, tailX + 5, tailX },
            new int[]{ tailY,     tailY,     tailY + 8 },
            3
        );

        // Dialogue text
        g.setColor(new Color(230, 220, 245));
        g.drawString(dialogue, boxX + padding, boxY + textH - 2);

        // Page indicator if more lines remain
        if (lineIndex < dialogueLines.length - 1) {
            g.setFont(new Font("SansSerif", Font.PLAIN, 10));
            g.setColor(new Color(150, 130, 180));
            g.drawString("▼", boxX + boxW - 14, boxY + boxH - 4);
        }
    }
    
    @Override
    public void update(float deltaTime) {/*NPC wont move*/}

    
}
