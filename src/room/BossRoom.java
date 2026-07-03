/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.Camera;
import core.InputHandler;
import entity.Boss;
import entity.Player;
import entity.attack.SoulProjectile;
import java.awt.Graphics2D;

/**
 * The boss arena room.
 *
 * Fight flow:
 *  - Boss starts {@code DORMANT} in the center of the arena.
 *  - Player walks close and presses UP to challenge it (Mantis-Lords-style).
 *  - Boss cycles: PATROL -> LEAP -> SLAM (beam) -> VULNERABLE -> repeat.
 *  - Room transitions to the next room once the boss is defeated.
 *
 * Collision handled here:
 *  - Player slashes   -> boss (soul rewarded on hit)
 *  - Player projectiles -> boss
 *  - Boss beam slabs  -> player
 *  - Boss body        -> player (contact damage)
 *  - Ground / ceiling -> player and boss (basic AABB)
 *
 * @author EDEN COMPUTERS
 */
public class BossRoom extends Room {

    //------------------- Room Layout ----------------------
    private static final float ROOM_W   = 1400f;
    private static final float ROOM_H   = 620f;
    private static final int GROUND_Y = 560;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT  = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    //-------------------- Boss ----------------------------
    private final Boss boss;

    //------------------- Win State ------------------------
    private boolean bossDefeated = false;

    //----------------- Contact damage iframes ------------
    /**
     * Frames the player is immune to boss-body contact after being hit.
     * Prevents instant repeated damage from standing inside the boss.
     */
    private static final int CONTACT_IFRAME_DURATION = 40;
    private int contactIframeTimer = 0;

    /**
     * Creates the boss room, spawning the boss centered on the arena floor.
     *
     * @param input the shared input handler — forwarded to the boss so it
     *              can read the UP key press for the fight trigger
     */
    public BossRoom(InputHandler input) {
        super("boss_room", ROOM_W, ROOM_H);

        float bossStartX = ROOM_W / 2f - Boss.BOSS_W / 2f;
        float bossStartY = GROUND_Y - Boss.BOSS_H;

        boss = new Boss(bossStartX, bossStartY, GROUND_Y, input, ROOM_LEFT, ROOM_RIGHT);
    }

    //---------------------- Update --------------------------

    /**
     * Advances the room one tick: wakes the boss if challenged, runs boss
     * and player physics, then resolves all collisions.
     *
     * @param dt delta time in seconds
     * @param player the active player
     */
    @Override
    public void update(float dt, Player player) {
        float playerCenterX = player.getLeft() + player.getWidth() / 2f;

        //Boss needs to be released from the dormant state checked devery frame
        boss.tryStart(playerCenterX);

        boss.setPlayerPosition(playerCenterX);
        boss.update(dt);

        updatePlayerPhysics(dt, player);

        if (contactIframeTimer > 0) contactIframeTimer--;

        if (!bossDefeated){
            checkSlashHits(player);
            checkProjectileHits(player);
            checkBeamHits(player);
            checkBodyContact(player);

            if (boss.isDead()) bossDefeated = true;
        }
    }

    //---------------- Player Physics -------------------------

    /**
     * Resolves ground and ceiling collisions for the player within this room.
     * Horizontal wall clamping is also applied here so the player cannot
     * leave the arena during the fight.
     *
     * @param dt     delta time in seconds
     * @param player the active player
     */
    private void updatePlayerPhysics(float dt, Player player) {

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

        // Horizontal arena walls
        if (player.getLeft() <= ROOM_LEFT) {
            player.setX(ROOM_LEFT);
            player.setVelX(0);
        }
        if (player.getLeft() + player.getWidth() >= ROOM_RIGHT) {
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
        }
    }

    //------------------- Collision Checks -------------------

    /**
     * Checks each active player slash against the boss hitbox.
     * Deactivates the slash on contact and awards soul to the player.
     * Boss.takeDamage() internally gates on the VULNERABLE state,
     * so slashes during other phases are ignored.
     *
     * @param player the active player
     */
    private void checkSlashHits(Player player) {
        for (var slash : player.getSlashes()) {
            if (!slash.isActive()) continue;

            if (slash.overlapsRect(boss.getLeft(), boss.getTop(), boss.getWidth(), boss.getHeight())){
                boss.takeDamage(1);
                slash.deactivate();
                player.gainSoul(Player.SOUL_PER_HIT);
            }
        }
    }

    /**
     * Checks each active soul projectile against the boss hitbox.
     * Deactivates the projectile on contact.
     * Damage is gated by VULNERABLE state inside Boss.takeDamage().
     *
     * @param player the player object
     */
    private void checkProjectileHits(Player player) {
        for (var proj : player.getProjectiles()){
            if (!proj.isActive()) continue;

            if (proj.overlapsRect(boss.getLeft(), boss.getTop(), boss.getWidth(), boss.getHeight())) {
                boss.takeDamage(SoulProjectile.DAMAGE);
                proj.deactivate();
            }
        }
    }

    /**
     * Checks whether the active beam slabs overlap the player.
     * Dashing grants invincibility so the player can dash through the beam.
     *
     * @param player the active player
     */
    private void checkBeamHits(Player player) {
        if (!boss.isBeamActive()) return;
        if (player.isInvincible()) return;

        if (boss.beamHits(player.getLeft(), player.getTop(), player.getWidth(), player.getHeight())){
            player.takeDamage(1);
        }
    }

    /**
     * Deals contact damage to the player when they overlap the boss body.
     * Uses a short per-contact iframe window so the player isn't hit
     * every frame while standing inside the boss.
     * Dashing (invincible) ignores this check entirely.
     *
     * @param player the active player
     */
    private void checkBodyContact(Player player) {
        if (player.isInvincible()) return;
        if (contactIframeTimer > 0) return;

        boolean overlapping =
            player.getLeft() < boss.getLeft() + boss.getWidth() &&
            player.getLeft() + player.getWidth() > boss.getLeft() &&
            player.getTop() < boss.getTop()  + boss.getHeight() &&
            player.getTop() + player.getHeight() > boss.getTop();

        if (overlapping){
            player.takeDamage(1);
            contactIframeTimer = CONTACT_IFRAME_DURATION;
        }
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the room background, floor, and all entities.
     * HUD is handled by the active game state, not here.
     *
     * @param g   drawing object
     * @param cam camera used to convert world coordinates to screen coordinates
     */
    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawBackground(g, cam);
        drawFloor(g, cam);
        boss.draw(g, cam);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the arena background.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawBackground(Graphics2D g, Camera cam) {
        //draw bg here
    }

    /**
     * Draws the arena floor ledge.
     *
     * @param g   drawing object
     * @param cam active camera
     */
    private void drawFloor(Graphics2D g, Camera cam) {
        //Draw Floor here
    }

    //------------------- Win State --------------------------

    /**
     * @return true once the boss has been defeated — poll this each tick
     *         from your game state to trigger the win screen
     */
    public boolean isWon() { return bossDefeated; }
}