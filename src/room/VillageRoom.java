/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.Camera;
import entity.Player;
import java.awt.Graphics2D;

/**
 * Village Room - id village 
 * 
 * contains NPC to tell about the boss
 * a bench where the player can save
 * a door to pass to the next room i.e the boss room
 * 
 * @author EDEN COMPUTERS
 */
public class VillageRoom extends Room{

    //-------------- Layout Constanst ---------------
    private static final float GROUND_Y = 460f;
    private static final float DOOR_X = 1170f;
    private static final float roomW = 1200f;
    private static final float roomH = 600f;
    
    private String nextRoom = null;
    
    public VillageRoom() {
        super("village", roomW, roomH);
    }
    
    
    @Override
    public void update(float dt, Player player) {
        nextRoom = null;
        
        if(player.getRight() >= DOOR_X)
            nextRoom = "boss";
    
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawBackground(g, cam);
        drawTiles(g, cam);
        drawDoor(g, cam);
    }
    
    //------------------------ Draw Helpers ---------------------
    private void drawBackground(Graphics2D g, Camera cam){
        
    }
    
    private void drawTiles(Graphics2D g, Camera cam){
        
    }
    
    private void drawDoor(Graphics2D g, Camera cam){
        
    }
    
}
