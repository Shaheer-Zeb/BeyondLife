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
 * non hostile Non playable character.
 * Cycle through its dialogues one press at a time
 * 
 * @author EDEN COMPUTERS
 */
public class NPC extends Entity{

    //---------------- Dimensions ------------
    private static final int NPC_HEIGHT = 46;
    private static final int NPC_WIDTH = 30;
    
    private final String[] dialogueLines; 
    private int lineIndex = 0;
    private boolean talking = false;
    
    private static final float INTERACT_DISTANCE = 80f;
    
    private final InputHandler input;
    
    public NPC(float x, float y, InputHandler input, String... dialogueLines){
        super(x, y, NPC_WIDTH, NPC_HEIGHT, 1);
        this.dialogueLines = dialogueLines;
        this.input = input;
        
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
        float npcCenter = (getLeft() - getRight()) / 2f;
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
            drawSpeechState(g, dialogueLines[lineIndex]);
    }
    
    //------------------- Draw Helpers ---------------------
    private void drawNPC(Graphics2D g, Camera cam){
        
    }
    
    private void drawSpeechState(Graphics2D g, String dialogue){
        
    }
    
    @Override
    public void update(float deltaTime) {/*NPC wont move*/}

    
}
