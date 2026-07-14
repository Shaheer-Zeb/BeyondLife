/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import entity.Player;
import entity.Walker;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;

/**
 * The gauntlet - a stretch of floating platforms between the village and the
 * boss arena. The player must hop across 4 platforms over a kill zone.
 *
 * @author EDEN COMPUTERS
 */
public class GauntletRoom extends Room {

    //------------------- Room Layout ----------------------
    public static final float ROOM_W = 1600;
    public static final float ROOM_H = 1080;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    private static final String BOSS_ROOM_ID = "boss_room";

    //------------------- Platforms & Kill Zone ---------------
    private static final int PLATFORM_H = 40;
    private static final float LAND_TOLERANCE = 14f;

    private final Platform[] platforms = new Platform[] {
        new Platform(80, 800, 220, PLATFORM_H),
        new Platform(420, 650, 200, PLATFORM_H),
        new Platform(760, 750, 200, PLATFORM_H),
        new Platform(1100, 550, 240, PLATFORM_H)
    };
    
    private final int KILLZONE_HEIGHT = 40;
    private final KillZone killZone = new KillZone(ROOM_H - KILLZONE_HEIGHT, ROOM_W, ROOM_H);

    //------------------- Walkers -----------------------------
    private final Walker walkerOnPlatform2;
    private final Walker walkerOnPlatform4;

    //------------------- Door -------------
    private static final int DOOR_W = 128;
    private static final int DOOR_H = 128;
    private final int doorX;
    private final int doorY;
    private boolean doorTriggered = false;
    private Image doorPortalGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/doorPortal1.gif");

    //------------------- Checkpoint (respawn point) ------------
    private boolean checkpointInitialized = false;
    private float lastSafeX;
    private float lastSafeY;

    //----------------- Contact damage iframes ------------------
    private static final float CONTACT_IFRAME_DURATION = 1.0f;
    private float contactIframeTimer = 0f;
    
    // -------------------- Parallax Background --------------------
    private BufferedImage sky = AssetManager.getImage("/assets/rooms/gauntlet/sky.png");
    private BufferedImage cloudsOne = AssetManager.getImage("/assets/rooms/gauntlet/clouds_1.png");
    private BufferedImage cloudsTwo = AssetManager.getImage("/assets/rooms/gauntlet/clouds_2.png");
    private BufferedImage rocks = AssetManager.getImage("/assets/rooms/gauntlet/rocks.png");
    private BufferedImage ground = AssetManager.getImage("/assets/rooms/gauntlet/ground.png");

    /**
     * Creates the gauntlet room and places both Walkers on their platforms.
     *
     * @param input shared input handler, forwarded to the Walkers
     */
    public GauntletRoom(InputHandler input) {
        super("gauntlet_room", ROOM_W, ROOM_H);

        Platform p2 = platforms[1];
        Platform p4 = platforms[3];
        
        walkerOnPlatform2 = new Walker( p2.getX() + 10, p2.getY() - Walker.WALKER_H, p2.getY(), input, p2.getX(), p2.getRight(), Walker.WalkerType.SLIME);
        walkerOnPlatform4 = new Walker( p4.getX() + 10, p4.getY() - Walker.WALKER_H, p4.getY(), input,p4.getX(), p4.getRight(), Walker.WalkerType.DINO);
        
        doorX = p4.getRight() - DOOR_W - 20;
        doorY = p4.getY() - DOOR_H + 17;
    }

    //---------------------- Update --------------------------

    @Override
    public void update(float dt, Player player, Camera cam) {
        if (!checkpointInitialized) {
            lastSafeX = player.getLeft();
            lastSafeY = player.getTop();
            checkpointInitialized = true;
        }

        float playerCenterX = player.getLeft() + player.getWidth() / 2f;
        float playerFeetY = player.getTop() + player.getHeight();

        walkerOnPlatform2.setPlayerPosition(playerCenterX, playerFeetY);
        walkerOnPlatform4.setPlayerPosition(playerCenterX, playerFeetY);
        walkerOnPlatform2.update(dt);
        walkerOnPlatform4.update(dt);

        updatePlayerPhysics(player);
        checkKillZone(player);
        checkWalkerContact(player, walkerOnPlatform2, cam);
        checkWalkerContact(player, walkerOnPlatform4, cam);
        checkDoorTrigger(player);
        if (contactIframeTimer > 0f) {
            contactIframeTimer -= dt;
        }
        
    }
    //---------------- Player Physics -------------------------

    /**
     * Resolves platform landings, ceiling, and side walls..
     *
     * @param player the active player
     */
    private void updatePlayerPhysics(Player player) {
        player.leaveGround();
        for (Platform p : platforms) {
            if (p.tryLand(player, LAND_TOLERANCE)) {
                lastSafeX = player.getLeft();
                lastSafeY = player.getTop();
            }
        }

        
        if (player.getTop() <= CEILING_Y) {
            player.setY(CEILING_Y);
            player.setVelY(0);
        }
        
        if (player.getLeft() <= ROOM_LEFT) {
            player.setX(ROOM_LEFT);
            player.setVelX(0);
            player.clingToWall(false);
        } else if (player.getLeft() + player.getWidth() >= ROOM_RIGHT) {
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
            player.clingToWall(true);
        } else {
            player.leaveWall();
        }
    }

    //------------------- Kill Zone ---------------------------

    /**
     * If the player falls past the kill zone, they take a hit and
     * respawn at the last platform they safely stood on.
     *
     * @param player the active player
     */
    private void checkKillZone(Player player) {
        if (killZone.catches(player)) {
            player.takeDamage(1);
            player.setX(lastSafeX);
            player.setY(lastSafeY);
            player.setVelX(0);
            player.setVelY(0);
        }
    }

    //------------------- Walker Contact -----------------------

    /**
     * Basic contact damage if the player touches a Walker's body.
     * Mirrors the iframe pattern used against the Boss.
     *
     * @param player the active player
     * @param walker the Walker to check against
     * @param cam camera, used for knockback shake
     */
    private void checkWalkerContact(Player player, Walker walker, Camera cam) {
        if (player.isInvincible()) return;
        if (contactIframeTimer > 0) return;

        if (player.overlap(walker)) {
            player.takeDamage(1);
            contactIframeTimer = CONTACT_IFRAME_DURATION;

            float walkerCenter = walker.getLeft() + walker.getWidth() / 2f;
            float playerCenter = player.getLeft() + player.getWidth() / 2f;
            float knockDir = playerCenter < walkerCenter ? -1f : 1f;
            player.applyKnockback(knockDir * 350f, -200f, cam);
        }
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

    //------------------------ Draw --------------------------

    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawBackground(g, cam);

        for (Platform p : platforms) {
            p.draw(g, cam);
        }
        killZone.draw(g, cam);
        drawDoor(g, cam);

        walkerOnPlatform2.draw(g, cam);
        walkerOnPlatform4.draw(g, cam);
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
        
        drawX = (int)(-cam.offsetX * 0.5);
        drawY = (int)(-cam.offsetY * 0.5);
//        g.drawImage(ground, drawX, drawY, roomWidth, roomHeight, null);
        
    }

    private void drawDoor(Graphics2D g, Camera cam) {
        int drawX = (int)(doorX - cam.offsetX);
        int drawY = (int)(doorY - cam.offsetY);
        g.drawImage(doorPortalGif, drawX, drawY, DOOR_W, DOOR_H, null);
    }

    //------------------- Room Transition --------------------

    @Override
    public String getNextRoomId() {
        return doorTriggered ? BOSS_ROOM_ID : null;
    }
}