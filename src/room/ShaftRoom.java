/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import core.SoundManager;
import core.TileManager;
import entity.Bench;
import entity.NPC;
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
    public static final float ROOM_H = 2000;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;
    public static final int GROUND_Y = 1900;
    
    private static final String BOSS_ROOM_ID = "boss_room";
    
    //------------------- Door ----------------------------
    private static final int DOOR_W = 128;
    private static final int DOOR_H = 128;
    private final int doorX = ROOM_RIGHT - DOOR_W;
    private final int doorY = GROUND_Y - DOOR_H + 17;
    private boolean doorTriggered = false;
    private Image doorPortalGif = AssetManager.getGif("/assets/rooms/village/doorPortal1.gif");

    // -------------------- Parallax Background --------------------
    private BufferedImage sky = AssetManager.getImage("/assets/rooms/shaft/background/sky.png");
    private BufferedImage cloudsOne = AssetManager.getImage("/assets/rooms/shaft/background/clouds_1.png");
    private BufferedImage cloudsTwo = AssetManager.getImage("/assets/rooms/shaft/background/clouds_2.png");
    private BufferedImage rocks = AssetManager.getImage("/assets/rooms/shaft/background/rocks.png");
    private BufferedImage groundOne = AssetManager.getImage("/assets/rooms/shaft/background/ground_1.png");
    private BufferedImage groundTwo = AssetManager.getImage("/assets/rooms/shaft/background/ground_2.png");
    private BufferedImage groundThree = AssetManager.getImage("/assets/rooms/shaft/background/ground_2.png");
    private BufferedImage plant = AssetManager.getImage("/assets/rooms/shaft/background/plant.png");

    private Bench bench;
    
    // ------------- NPC Stuff ----------------
    private static final int NPC_W = 128;
    private static final int NPC_H = 128;
    private NPC eleonora;
    
    // -------------- Environment Stuff ----------------
    private Image birdIdleGif = AssetManager.getGif("/assets/rooms/shaft/environment/bird.gif");
    private Image rabbitIdleGif = AssetManager.getGif("/assets/rooms/shaft/environment/rabbit.gif");
    private Image campfireGif = AssetManager.getGif("/assets/rooms/shaft/environment/campfire.gif");
    private Image campfireWithFoodGif = AssetManager.getGif("/assets/rooms/shaft/environment/campfireWithFood.gif");
    
    private Image coffin = AssetManager.getImage("/assets/rooms/shaft/environment/coffin.png");
    private Image graveOne = AssetManager.getImage("/assets/rooms/shaft/environment/grave.png");
    private Image graveTwo = AssetManager.getImage("/assets/rooms/shaft/environment/graveTwo.png");
    private Image house = AssetManager.getImage("/assets/rooms/shaft/environment/house.png");
    private Image lamp = AssetManager.getImage("/assets/rooms/shaft/environment/lamp.png");
    private Image lantern = AssetManager.getImage("/assets/rooms/shaft/environment/lantern.png");
    private Image sign = AssetManager.getImage("/assets/rooms/shaft/environment/sign.png");
    private Image trunkOne = AssetManager.getImage("/assets/rooms/shaft/environment/trunkOne.png");
    private Image trunkTwo = AssetManager.getImage("/assets/rooms/shaft/environment/trunkTwo.png");
    
    public ShaftRoom(InputHandler input) {
        super("shaft_room", ROOM_W, ROOM_H);
        
        bench = new Bench(doorX - Bench.benchH - 500, GROUND_Y - 28, input);
        eleonora = new NPC("Eleonora", roomW / 2, GROUND_Y - NPC_H + 20, NPC_H, NPC_W, input, NPC.NPCTYPE.ELEONORA,
        "You've come farther than most ever do.",
        "I've watched countless warriors walk through that door.",
        "Some returned victorious.",
        "Most didn't even return at all.",
        "Take one last sit.",
        "I'll be waiting here when you return.");
        
        SoundManager.stopAllSfx();
        SoundManager.playMusic("shaft");
        SoundManager.setMusicVolume(-10);
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

        if (overlapping)
        {
            doorTriggered = true;
            SoundManager.playSfx("doorOpen");
        }
    }
    
    @Override
    public void update(float dt, Player player, Camera cam) {
    
        updatePlayerPhysics(player);
        checkDoorTrigger(player);
        
        bench.update(dt);
        bench.updateInteraction(player, id);
        
        eleonora.updateInteraction(player.getLeft() + player.getWidth() / 2, player.getTop() + player.getHeight() / 2);
        
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
         drawBackground(g, cam);
         drawDoor(g, cam);
         
         bench.draw(g, cam);
         
         g.setColor(Color.MAGENTA);
         drawFloor(g, cam);
         drawEnvironment(g, cam);
         drawNpcs(g, cam);
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
       
        drawX = (int)(-cam.offsetX * 0.6);
        drawY = (int)(-cam.offsetY * 0.6);
        g.drawImage(groundOne, drawX, drawY, roomWidth, roomHeight, null);
        
        
        drawX = (int)(-cam.offsetX * 0.7);
        drawY = (int)(-cam.offsetY * 0.7);
        g.drawImage(groundTwo, drawX, drawY, roomWidth, roomHeight, null);
        
        drawX = (int)(-cam.offsetX * 0.8);
        drawY = (int)(-cam.offsetY * 0.8);
        g.drawImage(groundThree, drawX, drawY, roomWidth, roomHeight, null);
        
        drawX = (int)(-cam.offsetX * 0.9);
        drawY = (int)(-cam.offsetY * 0.9);
        g.drawImage(plant, drawX, drawY, roomWidth, roomHeight, null);

//        g.drawImage(ground, drawX, drawY, roomWidth, roomHeight, null);
        
    }
    private void drawNpcs(Graphics2D g, Camera cam){
        eleonora.draw(g, cam);
    }
    private void drawEnvironment(Graphics2D g, Camera cam){
        drawRabbit(g, cam);
        drawCampfires(g, cam);
        drawHouse(g, cam);
        drawBird(g, cam);
        drawGraveyard(g, cam);
        drawTrunks(g, cam);
        drawLights(g, cam);
        drawSign(g, cam);
    }
    private void drawLights(Graphics2D g, Camera cam){
        int drawX = (int)(bench.getLeft() - 70 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 64 - cam.offsetY);
        g.drawImage(lantern, drawX, drawY, 64, 64, null);
        
        drawX = (int)(bench.getRight() + 10 - cam.offsetX);
        drawY = (int)(GROUND_Y - 128 - cam.offsetY);
        g.drawImage(lamp, drawX, drawY, 64, 128, null);
        
    }
    private void drawSign(Graphics2D g, Camera cam){
        int drawX = (int)(doorX - 80 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 64 - cam.offsetY);
        g.drawImage(sign, drawX, drawY, 64, 64, null);
    }
    private void drawGraveyard(Graphics2D g, Camera cam){
        int drawX = (int)(430 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 69 - cam.offsetY);
        g.drawImage(coffin, drawX, drawY, 69, 66, null);
        
        drawX += 70;
        drawY = (int)(GROUND_Y - 64 - cam.offsetY);
        g.drawImage(graveOne, drawX, drawY, 64, 64, null);
        
        drawX += 70;
        g.drawImage(graveTwo, drawX, drawY, 64, 64, null);
    }
    private void drawTrunks(Graphics2D g, Camera cam){
        
    }
    private void drawHouse(Graphics2D g, Camera cam){
        int drawX = (int)(200 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 200 - cam.offsetY);
        g.drawImage(house, drawX, drawY, 210, 200, null);
    }
    private void drawBird(Graphics2D g, Camera cam){
        int drawX = (int)(200 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 16 - cam.offsetY);
        g.drawImage(birdIdleGif, drawX, drawY, 16, 16, null);
    }
    private void drawRabbit(Graphics2D g, Camera cam){
        int drawX = (int)(eleonora.getLeft() - 50 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 16 - cam.offsetY);
        g.drawImage(rabbitIdleGif, drawX, drawY, 32, 16, null);
    }
    private void drawCampfires(Graphics2D g, Camera cam){
        int drawX = (int)(1600 - cam.offsetX);
        int drawY = (int)(GROUND_Y - 96 - cam.offsetY);
        g.drawImage(campfireGif, drawX, drawY, 96, 96, null);
        
//        drawX = (int)(800 - cam.offsetX);
//        g.drawImage(campfireWithFoodGif, drawX, drawY, 96, 96, null);
    }
    private void drawDoor(Graphics2D g, Camera cam) {
        int drawX = (int)(doorX - cam.offsetX);
        int drawY = (int)(doorY - cam.offsetY);
        g.drawImage(doorPortalGif, drawX, drawY, DOOR_W, DOOR_H, null);
    }

    private void drawFloor(Graphics2D g, Camera cam) {
        TileManager.drawShaftTiles(g, cam);
    }
    
    //------------------- Room Transition --------------------

    @Override
    public String getNextRoomId() {
        return doorTriggered ? BOSS_ROOM_ID : null;
    }
    
}
