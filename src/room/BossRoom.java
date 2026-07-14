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
import entity.Boss;
import entity.Player;
import entity.attack.Slash;
import entity.attack.SoulProjectile;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * The boss arena room.
 *
 * Fight flow:
 *  - Boss starts Dormant in the center of the arena.
 *  - Player walks close and presses UP to challenge it - Hollow Knight Style.
 *  - Boss cycles: PATROL -> LEAP -> SLAM (beam) -> VULNERABLE -> repeat.
 *  - Room transitions to the next room once the boss is defeated.
 *
 * Collision handled here:
 *  - Player slashes   -> boss (soul rewarded on hit)
 *  - Player projectiles -> boss
 *  - Boss beam slabs  -> player
 *  - Boss body  -> player (contact damage)
 *  - Ground / ceiling -> player and boss (basic AABB)
 *
 * @author EDEN COMPUTERS
 */
public class BossRoom extends Room {

    //------------------- Room Layout ----------------------
    public static final int ROOM_W = 1400;
    public static final int ROOM_H = 1080;
    public static final int GROUND_Y = 970;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    //-------------------- Boss ----------------------------
    private final Boss boss;
    private boolean challenged = false;

    //------------------- Win State ------------------------
    private boolean bossDefeated = false;

    //----------------- Contact damage iframes ------------
    /**
     * Frames the player is immune to boss-body contact after being hit.
     * Prevents instant repeated damage from standing inside the boss.
     */
    private static final float CONTACT_IFRAME_DURATION = 1.5f;
    private float contactIframeTimer = 0;

    //------------------ KnockBack Force ----------------------
    private static final float KNOCKBCK_X = -450f;
    private static final float KNOCKBCK_Y = -250f;
    
    //------------------ Parallax Background Stuff -------------------------
    private BufferedImage sky = AssetManager.getImage("/assets/rooms/boss/background/sky.png");
    private BufferedImage cloudsOne = AssetManager.getImage("/assets/rooms/boss/background/clouds_1.png");
    private BufferedImage cloudsTwo = AssetManager.getImage("/assets/rooms/boss/background/clouds_2.png");
    private BufferedImage cloudsThree = AssetManager.getImage("/assets/rooms/boss/background/clouds_3.png");
    private BufferedImage rocksOne = AssetManager.getImage("/assets/rooms/boss/background/rocks1.png");
    private BufferedImage rocksTwo = AssetManager.getImage("/assets/rooms/boss/background/rocks2.png");
    private BufferedImage rocksThree = AssetManager.getImage("/assets/rooms/boss/background/rocks3.png");
    private BufferedImage pines = AssetManager.getImage("/assets/rooms/boss/background/pines.png");
    private BufferedImage birds = AssetManager.getImage("/assets/rooms/boss/background/birds.png");
    
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
        
        SoundManager.stopAllSfx();
        SoundManager.playMusic("boss");
        SoundManager.setMusicVolume(-10);
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
    public void update(float dt, Player player, Camera cam) {
        float playerCenterX = player.getLeft() + player.getWidth() / 2f;

        //Boss needs to be released from the dormant state checked devery frame
        if(boss.tryStart(playerCenterX) && !challenged){
            challenged = true;
            cam.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
        }

        boss.setPlayerPosition(playerCenterX);
        boss.update(dt);

        updatePlayerPhysics(dt, player);

        if(contactIframeTimer > 0f){
            contactIframeTimer -= dt;
        }

        if(!bossDefeated){
            checkSlashHits(player);
            checkProjectileHits(player);
            checkBeamHits(player);
            checkBodyContact(player, cam);

            if (boss.isDead()){
                cam.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
                bossDefeated = true;
            }
        }
    }

    //---------------- Player Physics -------------------------

    /**
     * Resolves ground and ceiling collisions for the player within this room.
     * Horizontal wall clamping is also applied here so the player cannot
     * leave the arena during the fight.
     *
     * @param dt delta time in seconds
     * @param player the active player
     */
    private void updatePlayerPhysics(float dt, Player player) {

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

        // Horizontal arena walls
        if (player.getLeft() <= ROOM_LEFT) {
            player.setX(ROOM_LEFT);
            player.setVelX(0);
            player.clingToWall(false);
        }
        else if (player.getLeft() + player.getWidth() >= ROOM_RIGHT) {
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
            player.clingToWall(true);
        }
        else
            player.leaveWall();
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
                if(boss.takeDamage(Slash.DAMAGE))
                    player.gainSoul(Player.SOUL_PER_HIT);
                
                slash.deactivate();
                
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
            boss.deactivateBeam();
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
    private void checkBodyContact(Player player, Camera cam) {
        if (player.isInvincible()) return;
        if (contactIframeTimer > 0) return;

        boolean overlapping =
            player.getLeft() < boss.getLeft() + boss.getWidth() &&
            player.getLeft() + player.getWidth() > boss.getLeft() &&
            player.getTop() < boss.getTop() + boss.getHeight() &&
            player.getTop() + player.getHeight() > boss.getTop() &&
            boss.isFightStarted();

        if (overlapping){
            player.takeDamage(1);
            contactIframeTimer = CONTACT_IFRAME_DURATION;
            
            float bossCenter = boss.getLeft() + boss.getWidth() / 2f;
            float playerCenter = player.getLeft() + player.getWidth() / 2f;

            if (playerCenter < bossCenter) {
                player.applyKnockback(KNOCKBCK_X, KNOCKBCK_Y, cam);
            } else {
                player.applyKnockback(-KNOCKBCK_X, KNOCKBCK_Y, cam);
            }
        }
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the room background, floor, and all entities.
     * HUD is handled by the active game state, not here.
     *
     * @param g drawing object
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
     * @param g drawing object
     * @param cam active camera
     */
    private void drawBackground(Graphics2D g, Camera cam) {
        int startingDrawY = -180;
        int drawX = (int)(-cam.offsetX), drawY = (int)(-cam.offsetY);
        g.drawImage(sky, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.10);
        drawY = (int)(startingDrawY - cam.offsetY * 0.10);
        g.drawImage(cloudsOne, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.15);
        drawY = (int)(startingDrawY - cam.offsetY * 0.15);
        g.drawImage(cloudsTwo, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.25);
        drawY = (int)(startingDrawY - cam.offsetY * 0.25);
        g.drawImage(cloudsThree, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.25);
        drawY = (int)(startingDrawY - cam.offsetY * 0.25);
        g.drawImage(rocksOne, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.30);
        drawY = (int)(startingDrawY - cam.offsetY * 0.30);
        g.drawImage(rocksTwo, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.40);
        drawY = (int)(startingDrawY - cam.offsetY * 0.40);
        g.drawImage(rocksThree, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.50);
        drawY = (int)(startingDrawY - cam.offsetY * 0.50);
        g.drawImage(pines, drawX, drawY, ROOM_W, ROOM_H, null);
        
        drawX = (int)(-cam.offsetX * 0.80);
        drawY = (int)(startingDrawY - cam.offsetY * 0.80);
        g.drawImage(birds, drawX, drawY, ROOM_W, ROOM_H, null);
        
    }
    /**
     * Draws the arena floor ledge.
     *
     * @param g drawing object
     * @param cam active camera
     */
    private void drawFloor(Graphics2D g, Camera cam) {
        TileManager.drawVillageTiles(g, cam);
    }

    //------------------- Win State --------------------------

    /**
     * @return true once the boss has been defeated — poll this each tick
     *         from your game state to trigger the win screen
     */
    public boolean isWon() { return bossDefeated; }
}