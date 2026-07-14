/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import core.TileManager;
import entity.Bench;
import entity.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 *
 * @author EDEN COMPUTERS
 */
public class ShaftRoom extends Room {

    //------------------- Room Layout ----------------------
    public static final float ROOM_W = 2000;
    public static final float ROOM_H = 4000;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;
    public static final int GROUND_Y = 3900;
    
    private static final String BOSS_ROOM_ID = "boss_room";
    
    //------------------- Door ----------------------------
    private static final int DOOR_W = 128;
    private static final int DOOR_H = 128;
    private final int doorX = ROOM_RIGHT - DOOR_W;
    private final int doorY = GROUND_Y - DOOR_H + 20;
    private boolean doorTriggered = false;
    private Image doorPortalGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/doorPortal1.gif");

    // -------------------- Parallax Background --------------------
    private BufferedImage sky = AssetManager.getImage("/assets/rooms/gauntlet/sky.png");
    private BufferedImage cloudsOne = AssetManager.getImage("/assets/rooms/gauntlet/clouds_1.png");
    private BufferedImage cloudsTwo = AssetManager.getImage("/assets/rooms/gauntlet/clouds_2.png");
    private BufferedImage rocks = AssetManager.getImage("/assets/rooms/gauntlet/rocks.png");
    private BufferedImage ground = AssetManager.getImage("/assets/rooms/gauntlet/ground.png");

    Bench bench;
    
    public ShaftRoom(InputHandler input) {
        super("shaft_room", ROOM_W, ROOM_H);
        
        bench = new Bench(doorX - Bench.benchH - 500, GROUND_Y - 28, input);
    }
    
    /**
     * Resolves ground, ceiling, and arena-wall collisions for the player.
     *
     * @param player the player obj
     */
    private void updatePlayerPhysics(Player player) {

        // Ground
        if (player.getTop() + player.getHeight() - 1 >= GROUND_Y) {
            player.setY(GROUND_Y - player.getHeight());
            player.setVelY(0);
            player.landOnGround();
        }

        // Ceiling
        if (player.getTop() <= CEILING_Y) {
            player.setY(CEILING_Y);
            player.setVelY(0);
        }

        // Left wall
        if (player.getLeft() <= ROOM_LEFT) {
            if (!player.isOnGround())
                player.clingToWall(false);
            player.setX(ROOM_LEFT);
            player.setVelX(0);
        }

        // Right wall — blocked by the door frame, not a free exit
        else if (player.getLeft() + player.getWidth() >= ROOM_RIGHT ){
            if (!player.isOnGround())
                player.clingToWall(true);
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
            
        }
        else
            player.leaveWall();
    }
    
    //------------------- Door Trigger -------------------------

    private void checkDoorTrigger(Player player) {
        if (doorTriggered) return;

        boolean overlapping =
            player.getLeft() < doorX + DOOR_W &&
            player.getLeft() + player.getWidth() > doorX &&
            player.getTop() < doorY + DOOR_H &&
            player.getTop() + player.getHeight() > doorY;

        if (overlapping) doorTriggered = true;
    }
    
    @Override
    public void update(float dt, Player player, Camera cam) {
    
        updatePlayerPhysics(player);
        checkDoorTrigger(player);
        
        bench.update(dt);
        bench.updateInteraction(player, id);
     
        
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
         drawBackground(g, cam);
         drawDoor(g, cam);
         
         bench.draw(g, cam);
         
         g.setColor(Color.MAGENTA);
         drawFloor(g, cam);
    }
    
    private void drawBackground(Graphics2D g, Camera cam) {
        int roomWidth = (int)ROOM_W, roomHeight = (int)ROOM_H;
        
        int drawX = (int)(-cam.offsetX), drawY = (int)(-cam.offsetY);
        g.drawImage(sky, drawX, drawY, roomWidth, roomHeight, null);
        
        drawX = (int)(-cam.offsetX * 0.2);
        drawY = (int)(-cam.offsetY * 0.3);
        g.drawImage(cloudsOne, drawX, drawY, roomWidth, roomHeight, null);
        
        drawX = (int)(-cam.offsetX * 0.3);
        drawY = (int)(-cam.offsetY * 0.3);
        g.drawImage(cloudsTwo, drawX, drawY, roomWidth, roomHeight, null);
        
        drawX = (int)(-cam.offsetX * 0.5);
        drawY = (int)(-cam.offsetY * 0.5);
        g.drawImage(rocks, drawX, drawY, roomWidth, roomHeight, null);
        

//        g.drawImage(ground, drawX, drawY, roomWidth, roomHeight, null);
        
    }

    private void drawDoor(Graphics2D g, Camera cam) {
        int drawX = (int)(doorX - cam.offsetX);
        int drawY = (int)(doorY - cam.offsetY);
        g.drawImage(doorPortalGif, drawX, drawY, DOOR_W, DOOR_H, null);
    }

    private void drawFloor(Graphics2D g, Camera cam) {
        int drawX = (int)(-cam.offsetX);
        int drawY = (int)(GROUND_Y - cam.offsetY);

        g.setColor(Color.RED);
        g.fillRect(drawX, drawY, (int)ROOM_W, 100);
    }
    
    //------------------- Room Transition --------------------

    @Override
    public String getNextRoomId() {
        return doorTriggered ? BOSS_ROOM_ID : null;
    }
    
}
