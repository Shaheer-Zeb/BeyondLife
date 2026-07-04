/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.Camera;
import core.InputHandler;
import entity.Bench;
import entity.NPC;
import entity.Player;
import java.awt.Graphics2D;

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
    private static final float ROOM_W = 2000f;
    private static final float ROOM_H = 620f;
    private static final int GROUND_Y = 560;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    //------------------- NPC Dimensions ------------------
    private static final int NPC_W = 32;
    private static final int NPC_H = 48;

    //------------------- Door ----------------------------
    private static final int DOOR_X = ROOM_RIGHT - 70;
    private static final int DOOR_Y = GROUND_Y - 110;
    private static final int DOOR_W = 45;
    private static final int DOOR_H = 110;
    private static final String BOSS_ROOM_ID  = "boss_room";

    //------------------- Entities ------------------------
    private final Bench bench;
    private final NPC faseeh;
    private final NPC shaheer;
    private final NPC pigeonDoctor;

    //------------------- Transition ----------------------
    private boolean doorTriggered = false;

    /**
     * Creates the village room and places all entities.
     *
     * @param input the shared input handler passed to NPCs and the bench
     *              so they can read the UP key for interactions
     */
    public VillageRoom(InputHandler input) {
        super("village", ROOM_W, ROOM_H);

        // Bench - left side of the room, sitting on the floor
        bench = new Bench(220f, GROUND_Y - 20, input);

        // NPCs spread across the room, all standing on the ground
        faseeh = new NPC( "Faseeh" ,480f, GROUND_Y - NPC_H, NPC_H, NPC_W, input,
            "Hi My Nigga. Welcome to our Game",
            "This is a project for OOP",
            "Please Sir G Marks achay de dein 🤨"
        );

        shaheer = new NPC( "Shaheer" ,820f, GROUND_Y - NPC_H, NPC_H, NPC_W, input,
            "Placeholder line",
            "Placeholder line 2",
            "Good luck, I suppose."
        );

        pigeonDoctor = new NPC("Tuff Pigeon", 1200f, GROUND_Y - NPC_H, NPC_H, NPC_W, input,
            "I have One Advice",
            "Be Tuff",
            "Like Me"
        );
    }

    //---------------------- Update --------------------------

    /**
     * Updates all entity interactions, player physics, and the door trigger.
     *
     * @param dt delta time in seconds
     * @param player the active player
     */
    @Override
    public void update(float dt, Player player) {
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
        if (player.getTop() + player.getHeight() >= GROUND_Y) {
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
            player.setX(ROOM_LEFT);
            player.setVelX(0);
        }

        // Right wall - blocked by the door frame, not a free exit
        if (player.getLeft() + player.getWidth() >= ROOM_RIGHT) {
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
        }
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

        bench.draw(g, cam);
        faseeh.draw(g, cam);
        shaheer.draw(g, cam);
        pigeonDoctor.draw(g, cam);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the village background fill.
     *
     * @param g drawing object
     * @param cam active camera
     */
    private void drawBackground(Graphics2D g, Camera cam) {
        //draw bg here
    }

    /**
     * Draws the flat stone floor with a lit top edge.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawFloor(Graphics2D g, Camera cam) {
        // draw floor here
    }

    /**
     * Draws the boss-room door on the right wall.
     * A red ambient glow hints at what lies beyond.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawDoor(Graphics2D g, Camera cam) {
        //draw door here
    }

    //------------------- Room Transition --------------------

    /**
     * Returns the boss room ID once the player walks through the door,
     * or {@code null} while they are still in the village.
     *
     * @return next room ID or null
     */
    @Override
    public String getNextRoomId() {
        return doorTriggered ? BOSS_ROOM_ID : null;
    }
}