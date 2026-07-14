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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * The village - the calm area before the boss fight.
 *
 * Contains:
 *  - A bench
 *  - Three NPCs: {Faseeh}, {Shaheer}, and the {Pigeon Doctor}
 *  - A visible door on the right wall leading to the boss room
 *
 * The player exits by walking into the door on the right side.
 *
 * @author EDEN COMPUTERS
 */
public class VillageRoom extends Room {

    //------------------- Room Layout ----------------------
    public static final float ROOM_W = 1634;
    public static final float ROOM_H = 1080;
    public static final int GROUND_Y = 970;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    //------------------- NPC Dimensions ------------------
    private static final int NPC_W = 128;
    private static final int NPC_H = 128;

    //------------------- Door ----------------------------
    private static final int DOOR_W = 128;
    private static final int DOOR_H = 128;
//    private static final int DOOR_X = ROOM_RIGHT - DOOR_W;
    private static final int DOOR_X = ROOM_LEFT;
    private static final int DOOR_Y = GROUND_Y - DOOR_H + 17;
    private static final String NEXT_ROOM_ID  = "gauntlet_room";
    private Image doorPortalGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/doorPortal1.gif");

    //------------------- Entities ------------------------
    private final Bench bench;
    private final NPC faseeh;
    private final NPC shaheer;
    private final NPC pigeonDoctor;

    //------------------- Transition ----------------------
    private boolean doorTriggered = false;
    
    //------------------- Environment Stuff ---------------
    private BufferedImage sunflowers = AssetManager.getImage("/assets/rooms/village/environment/sunflowers.png");
    private BufferedImage scarecrowImage = AssetManager.getImage("/assets/rooms/village/environment/scarecrow.png");
    private BufferedImage bigTee = AssetManager.getImage("/assets/rooms/village/environment/bigTree.png");
    private BufferedImage smallTree = AssetManager.getImage("/assets/rooms/village/environment/smallTree.png");
    private BufferedImage rightArrowSign = AssetManager.getImage("/assets/rooms/village/environment/rightArrowSign.png");
    
    private Image frogGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/environment/frogIdle.gif");
    
    private static BufferedImage bigBush = AssetManager.getImage("/assets/rooms/village/environment/bigBush.png");
    private BufferedImage anotherBigBush = AssetManager.getImage("/assets/rooms/village/environment/anotherBigBush.png");
    
    private BufferedImage firstGrave = AssetManager.getImage("/assets/rooms/village/environment/firstGrave.png");
    private BufferedImage secondGrave = AssetManager.getImage("/assets/rooms/village/environment/secondGrave.png");
    private BufferedImage thirdGrave = AssetManager.getImage("/assets/rooms/village/environment/thirdGrave.png");
    private BufferedImage prayingMary = AssetManager.getImage("/assets/rooms/village/environment/prayingMary.png");
    
    private Image butterflyOne = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/environment/butterfly.gif");
    
    //-------------------- Parllax Background Stuff -----------------------    
    private BufferedImage sky2Image = AssetManager.getImage("/assets/rooms/village/background/sky.png");
    private BufferedImage clouds1 = AssetManager.getImage("/assets/rooms/village/background/clouds_1.png");
    private BufferedImage clouds2 = AssetManager.getImage("/assets/rooms/village/background/clouds_2.png");
    private BufferedImage clouds3 = AssetManager.getImage("/assets/rooms/village/background/clouds_3.png");
    private BufferedImage clouds4 = AssetManager.getImage("/assets/rooms/village/background/clouds_4.png");
    private BufferedImage rocks1 = AssetManager.getImage("/assets/rooms/village/background/rocks_1.png");
    private BufferedImage rocks2 = AssetManager.getImage("/assets/rooms/village/background/rocks_2.png");
    
    // ------------------ Door -----------------------------
    
    /**
     * Creates the village room and places all entities.
     *
     * @param input the shared input handler passed to NPCs and the bench
     *              so they can read the UP key for interactions
     */
    public VillageRoom(InputHandler input) {
        super("village", ROOM_W, ROOM_H);

        // Bench - left side of the room, sitting on the floor
        bench = new Bench(650, GROUND_Y - 30, input);

        // NPCs spread across the room, all standing on the ground
        faseeh = new NPC("Faseeh", 480f, GROUND_Y - NPC_H, NPC_H, NPC_W, input, NPC.NPCTYPE.FASEEH,
            "Welcome! We don't get many visitors around here.",
            "Take a breath. Beyond that door, things stop being friendly.",
            "The bench is your best friend. Use it while you still can.",
            "Shaheer insists he built this village. Don't encourage him.",
            "Who's Shaheer, you ask?",
            "He's my girlfriend.",
            "And we built this place together... though he'll tell you he did most of the work.",
            "Anyways, whatever happens next... don't say I didn't warn you."
        );

        shaheer = new NPC("Shaheer", 820f, GROUND_Y - NPC_H, NPC_H, NPC_W, input, NPC.NPCTYPE.SHAHEER,
            "Nice, you actually made it this far.",
            "This village was supposed to be peaceful...",
            "...until a certain someone over there signed us up for this competition.",
            "Now we suddenly needed a final boss battle.",
            "If you lose... just pretend you were testing controls.",
            "The boss isn't impossible. We tested it... eventually."
        );

        pigeonDoctor = new NPC("Tuff Pigeon", 1200f, GROUND_Y - NPC_H, NPC_H, NPC_W, input, NPC.NPCTYPE.PIGEONDOCTOR,
            "You met them two, right?",
            "Both of them are idiots.",
            "One spends all day talking.",
            "The other spends all day solving everyone else's problems.",
            "I'm the only professional around here.",
            "Now listen carefully.",
            "Beyond that door is your final examination.",
            "Go make your teacher proud."
        );
        SoundManager.stopAllSfx();
        SoundManager.playMusic("village");
        SoundManager.setMusicVolume(-10);
        SoundManager.playSfx("villageLife");
    }

    //---------------------- Update --------------------------

    /**
     * Updates all entity interactions, player physics, and the door trigger.
     *
     * @param dt delta time in seconds
     * @param player the active player
     * @param cam the camera object
     */
    @Override
    public void update(float dt, Player player, Camera cam) {
        float playerCenterX = player.getLeft() + player.getWidth() / 2f;
        float playerCenterY = player.getTop() + player.getHeight() / 2f;

        bench.updateInteraction(player, id);

        faseeh.updateInteraction(playerCenterX, playerCenterY);
        shaheer.updateInteraction(playerCenterX, playerCenterY);
        pigeonDoctor.updateInteraction(playerCenterX, playerCenterY);

        updatePlayerPhysics(player);
        checkDoorTrigger(player);
    }

    //---------------- Player Physics -------------------------

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

    //------------------- Door Trigger -----------------------

    /**
     * Fires once when the player's hitbox overlaps the door rectangle.
     * After that {@link #getNextRoomId()} returns the boss room ID.
     *
     * @param player the player obj
     */
    private void checkDoorTrigger(Player player) {
        if (doorTriggered) return;

        boolean overlapping =
            player.getLeft() < DOOR_X + DOOR_W &&
            player.getLeft() + player.getWidth() > DOOR_X &&
            player.getTop() < DOOR_Y + DOOR_H &&
            player.getTop() + player.getHeight() > DOOR_Y;

        if (overlapping) doorTriggered = true;
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the room background, floor, door, and all entities.
     * HUD is handled by the active game state, not here.
     *
     * @param g drawing object
     * @param cam camera used to convert world coordinates to screen coordinates
     */
    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawBackground(g, cam);
        drawFloor(g, cam);
        drawDoor(g, cam);
        
        drawEnvironmentStuff(g, cam);
        drawEffects(g, cam);
        
        bench.draw(g, cam);
        faseeh.draw(g, cam);
        shaheer.draw(g, cam);
        pigeonDoctor.draw(g, cam);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the village background fill. Multiplying the camera offset by any value to control the Parallax backgrounds.
     *
     * @param g drawing object
     * @param cam active camera
     */
    private void drawBackground(Graphics2D g, Camera cam) {
        int startingDrawY = -200;
        g.drawImage(sky2Image, 0, 0, (int)ROOM_W, (int)ROOM_H, null);
        
        int drawX = (int)(-cam.offsetX * 0.1), drawY = (int)(startingDrawY-cam.offsetY * 0.1);
        g.drawImage(clouds1, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.15);
        drawY = (int)(startingDrawY-cam.offsetY * 0.15);
        g.drawImage(clouds2, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.20);
        drawY = (int)(startingDrawY-cam.offsetY * 0.20);
        g.drawImage(clouds3, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.25);
        drawY = (int)(startingDrawY-cam.offsetY * 0.25);
        g.drawImage(clouds4, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.35);
        drawY = (int)(startingDrawY-cam.offsetY * 0.4);
        g.drawImage(rocks1, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.5);
        drawY = (int)(startingDrawY-cam.offsetY * 0.6);
        g.drawImage(rocks2, drawX, drawY, (int)ROOM_W, (int)ROOM_H, null);
    }
    /**
     * Draws the flat stone floor with a lit top edge.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawFloor(Graphics2D g, Camera cam) {
        TileManager.drawVillageTiles(g, cam);
    }

    /**
     * Draws the boss-room door on the right wall.
     * A red ambient glow hints at what lies beyond.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawDoor(Graphics2D g, Camera cam) {
        int x = (int)(DOOR_X - cam.offsetX);
        int y = (int)(DOOR_Y - cam.offsetY);
        g.drawImage(doorPortalGif, x, y, DOOR_W, DOOR_H, null);
    }
    /**
     * Draws the environmental objects like the Soul Cache. We can add more things if we want to make the environment more lively.
     * All the helper draw envrionmental stuff methods like the drawSunflowers() will draw it on the ground and the width and height values will be hardcoded.
     * @param g
     * @param cam 
     */
    private void drawEnvironmentStuff(Graphics2D g, Camera cam){
        drawSunflowers(g, cam);
        drawScareCrow(g, cam);
        drawTrees(g, cam);
        drawFrogs(g, cam);
        drawArrowSymbols(g, cam);
        drawGraveyard(g, cam);
        drawButterflies(g, cam);
    }
    private void drawSunflowers(Graphics2D g, Camera cam){
        int drawX = (int)(900 - cam.offsetX), drawY = (int)(GROUND_Y - 70 - cam.offsetY);
        g.drawImage(sunflowers, drawX, drawY, 114, 70, null);
        drawX += 100;
        g.drawImage(sunflowers, drawX + 114, drawY, -114, 70, null);
    }
    private void drawScareCrow(Graphics2D g, Camera cam){
        int drawX = (int)(1100 - cam.offsetX), drawY = (int)(GROUND_Y - 99 - cam.offsetY);
        g.drawImage(scarecrowImage, drawX, drawY, 68, 99, null);
    }
    private void drawTrees(Graphics2D g, Camera cam){
        int drawX = (int)(577 - cam.offsetX), drawY = (int)(GROUND_Y - 147 - cam.offsetY);
        g.drawImage(smallTree, drawX, drawY, 126, 147, null);
        
        drawX += 700;
        drawY = (int)(GROUND_Y - 170 - cam.offsetY);
        g.drawImage(bigTee, drawX, drawY, 160, 170, null);
    }
    private void drawFrogs(Graphics2D g, Camera cam){
        int drawX = (int)(440 - cam.offsetX), drawY = (int)(GROUND_Y - 64 - cam.offsetY);
        g.drawImage(frogGif, drawX, drawY, 64, 64, null);
    }
    private void drawArrowSymbols(Graphics2D g, Camera cam){
        int drawX = (int)(ROOM_W - 200 - cam.offsetX), drawY = (int)(GROUND_Y - 44 - cam.offsetY);
        g.drawImage(rightArrowSign, drawX, drawY, 32, 44, null);
    }
    private void drawGraveyard(Graphics2D g, Camera cam){
        int drawX = (int)(100 - cam.offsetX), drawY = (int)(GROUND_Y - 82 - cam.offsetY);
        g.drawImage(prayingMary, drawX, drawY, 38, 82, null);
        
        drawX += 38 + 20;
        drawY = (int)(GROUND_Y - 54 - cam.offsetY);
        g.drawImage(firstGrave, drawX, drawY, 38, 54, null);
        
        drawX += 38;
        drawY = ((int)(GROUND_Y - 59 - cam.offsetY));
        g.drawImage(secondGrave, drawX, drawY, 34, 59, null);
        
        drawX += 34;
        drawY = ((int)(GROUND_Y - 46 - cam.offsetY));
        g.drawImage(thirdGrave, drawX, drawY, 35, 46, null);
        
        drawX += 35;
        drawY = ((int)(GROUND_Y - 59 - cam.offsetY));
        g.drawImage(secondGrave, drawX, drawY, 34, 59, null);
    }
    private void drawButterflies(Graphics2D g, Camera cam){
        int drawX = (int)(120 - cam.offsetX), drawY = (int)(GROUND_Y - 100 - cam.offsetY);
        g.drawImage(butterflyOne, drawX, drawY, 109 / 5, 86 / 5, null);
        
        drawX += 100;
        drawY += 30;
        g.drawImage(butterflyOne, drawX, drawY, 109 / 5, 86 / 5, null);
        
        drawX += 5;
        drawY += 20;
        g.drawImage(butterflyOne, drawX, drawY, 109 / 5, 86 / 5, null);
        
        drawX -= 70;
        drawY -= 17;
        g.drawImage(butterflyOne, drawX, drawY, 109 / 5, 86 / 5, null);
        
    }
    /**
     * Draw the effects like the butterfly and the bugs.
     * @param g
     * @param cam 
     */
    private void drawEffects(Graphics2D g, Camera cam){
        
    }
    //------------------- Room Transition --------------------

    /**
     * Returns the boss room ID once the player walks through the door,
     * or null while they are still in the village.
     *
     * @return next room ID or null
     */
    @Override
    public String getNextRoomId() {
        return doorTriggered ? NEXT_ROOM_ID : null;
    }
}