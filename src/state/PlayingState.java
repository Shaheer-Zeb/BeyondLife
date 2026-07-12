/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import core.Camera;
import core.InputHandler;
import core.SoundManager;
import entity.Player;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import room.BossRoom;
import room.Room;
import room.VillageRoom;

/**
 * The active game play state.
 * Owns the current Room, Player, and Camera.
 * Detects room transitions and player death, and signals the manager.
 *
 * @author EDEN COMPUTERS
 */
public class PlayingState implements GameState {

    //------------------- Core ----------------------------
    private final InputHandler input;
    private final int screenW, screenH;

    private final Camera camera;
    private final Player player;

    //------------------- Room ----------------------------
    private Room currentRoom;
    private boolean pendingTransition = false;
    private String nextRoomId = null;

    //------------------- Death ---------------------------

    private boolean playerDead = false;
    private float deathTimer = 0f;
    private static final float DEATH_RESPAWN_DELAY = 2.0f;

    //------------------- Win -----------------------------
    private boolean bossDefeated = false;
    /**
     * Creates the playing state, initializes the player, camera,
     * and drops into the village as the starting room.
     *
     * @param input input handler
     * @param screenW screen width
     * @param screenH screen height
     */
    public PlayingState(InputHandler input, int screenW, int screenH){
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;

        camera = new Camera(screenW, screenH);

        currentRoom = new VillageRoom(input);
        player = new Player(120f, VillageRoom.GROUND_Y, input);
    }

    //---------------------- Update --------------------------

    /**
     * Advances the game one tick.
     * Flushes input at the end regardless of state so no key press bleeds into the next frame.
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(float dt) {

        // Input is always flushed — even when paused/transitioning/won
        if (pendingTransition || bossDefeated) {
            input.flushJustPressed();
            return;
        }

        currentRoom.update(dt, player, camera);
        player.update(dt);

        camera.follow( player.getLeft() + player.getWidth()  / 2f, player.getTop() + player.getHeight() / 2f, dt, currentRoom.roomW, currentRoom.roomH);

        if (player.isDead()){

            camera.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
            playerDead = true;
            handleDeath(dt);
            input.flushJustPressed();
            return;
        }

        // Room transition
        String next = currentRoom.getNextRoomId();
        if (next != null) {
            nextRoomId = next;
            pendingTransition = true;
        }

        // Win condition
        if (currentRoom instanceof BossRoom br && br.isWon()) {
            camera.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
            bossDefeated = true;
        }

        input.flushJustPressed();
    }

    /**
     * Counts down the death delay then re spawns the player
     * at their last bench position in the village.
     *
     * @param dt delta time in seconds
     */
    private void handleDeath(float dt) {
        deathTimer += dt;
        if (deathTimer >= DEATH_RESPAWN_DELAY) {
            player.respawn();
            currentRoom = new VillageRoom(input);
            playerDead  = false;
            deathTimer  = 0f;
        }
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the current room, player, HUD, and any overlays.
     *
     * @param g drawing object
     */
    @Override
    public void draw(Graphics2D g) {
        currentRoom.draw(g, camera);
        player.draw(g, camera);
        drawHUD(g);

        if (player.isDead()) drawDeathOverlay(g);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the in-game HUD: health masks, soul meter, and room label.
     *
     * @param g drawing object
     */
    private void drawHUD(Graphics2D g) {

        // Health masks (Hollow Knight style)
        int maskSize = 18, gap = 4, startX = 16, startY = 16;
        for (int i = 0; i < player.getMaxHealth(); i++) {
            int mx = startX + i * (maskSize + gap);
            boolean filled = i < player.getHealth();
            g.setColor(filled ? new Color(220, 200, 255) : new Color(50, 45, 65));
            g.fillOval(mx, startY, maskSize, maskSize);
            g.setColor(new Color(180, 160, 220));
            g.drawOval(mx, startY, maskSize, maskSize);
        }

        // Soul meter
        int soulBarW = 80;
        int soulBarH = 8;
        int sbx = 16;
        int sby = startY + maskSize + 8;
        
        g.setColor(new Color(30, 25, 45));
        g.fillRoundRect(sbx, sby, soulBarW, soulBarH, soulBarH, soulBarH);
        
        g.setColor(new Color(100, 160, 255));
        g.fillRoundRect(sbx, sby, (int)(soulBarW * ((float) player.getSoul() / Player.MAX_SOUL)), soulBarH, soulBarH, soulBarH);
        
        g.setColor(new Color(70, 60, 100));
        g.drawRoundRect(sbx, sby, soulBarW, soulBarH, soulBarH, soulBarH);
        
        g.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g.setColor(new Color(100, 90, 130));
        g.drawString("SOUL", sbx + soulBarW + 6, sby + soulBarH);

        // Room label (top right)
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        g.setColor(new Color(80, 70, 100));
        g.drawString(currentRoom.id.toUpperCase(), screenW - 80, 24);
    }

    /**
     * Draws a black fade and "You Died" text that grows in over the
     * first half of the re spawn delay.
     *
     * @param g drawing object
     */
    private void drawDeathOverlay(Graphics2D g) {
        float alpha = Math.min(deathTimer / DEATH_RESPAWN_DELAY, 1f);
        g.setColor(new Color(0f, 0f, 0f, alpha * 0.8f));
        g.fillRect(0, 0, screenW, screenH);

        if (deathTimer > 0.6f) {
            float textAlpha = Math.min((deathTimer - 0.6f) / 0.4f, 1f);
            g.setFont(new Font("Serif", Font.PLAIN, 28));
            g.setColor(new Color(180, 60, 60, (int)(180 * textAlpha)));
            String msg = "You Died";
            int mw = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, screenW / 2 - mw / 2, screenH / 2);
        }
    }

    //-------------- Accessors for State Manager -------------

    /**
     * @return true if a room transition is waiting to be executed
     */
    public boolean isPendingTransition() { return pendingTransition; }

    /**
     * @return the ID of the room to transition into, or null
     */
    public String getNextRoomId() { return nextRoomId; }

    /**
     * @return true once the boss has been defeated — signal to switch to WIN state
     */
    public boolean isBossDefeated() { return bossDefeated; }

    /**
     * Called by the state manager after any loading screen completes.
     * Loads the target room and repositions the player at the entry point.
     *
     * @param roomId the ID of the room to load
     */
    public void transitionToRoom(String roomId) {
        camera.shake(Camera.SHAKE_DURATION, Camera.SHAKE_MAGNITUDE);
        pendingTransition = false;
        nextRoomId = null;

        switch (roomId) {
            case "boss_room" -> {
                currentRoom = new BossRoom(input);
                player.setX(80f);
                player.setY(BossRoom.GROUND_Y - player.getHeight());
            }
            case "village" -> {
                currentRoom = new VillageRoom(input);
                player.setX(player.spawnX);
                player.setY(player.spawnY);
                SoundManager.stopAllSfx();
                SoundManager.playMusic("village");
            }
        }
    }

    @Override
    public GameState nextState() { return null; }
}