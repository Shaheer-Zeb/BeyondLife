/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Brief loading screen shown between room transitions.
 *
 * Counts down a fixed duration while animating a progress bar,
 * then signals the state manager via to load the target room.
 * JUST FOR DRAMATIC PURPOSES XD :)
 *
 * @author EDEN COMPUTERS
 */
public class LoadingScreen implements GameState {

    //------------------- Timing --------------------------
    private static final float DURATION = 1.2f;
    private float progress = 0f;
    private boolean done = false;

    //------------------- Core ----------------------------
    private final int screenW, screenH;
    private final String targetRoomId;

    //------------------- Bar Animation ------------------
    private float barFill = 0f;
    private static final int BAR_W = 300;
    private static final int BAR_H = 6;

    /**
     * Creates a loading screen that transitions into the given room
     * once its timer elapses.
     *
     * @param screenW screen width in pixels
     * @param screenH screen height in pixels
     * @param targetRoomId ID of the room to load when done
     */
    public LoadingScreen(int screenW, int screenH, String targetRoomId) {
        this.screenW = screenW;
        this.screenH = screenH;
        this.targetRoomId = targetRoomId;
    }

    //------------------- Getter --------------------------

    /**
     * @return the ID of the room this loading screen is transitioning into
     */
    public String getTargetRoomId() { return targetRoomId; }

    /**
     * @return true once the loading duration has elapsed — poll this
     *         from the state manager to trigger the room switch
     */
    public boolean isDone() { return done; }

    //---------------------- Update --------------------------

    /**
     * Advances the loading timer and bar fill each tick.
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(float dt) {
        progress += dt / DURATION;
        barFill = Math.min(progress, 1f);
        if (progress >= 1f) done = true;
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the black fade-in, progress bar, and loading label.
     *
     * @param g drawing object
     */
    @Override
    public void draw(Graphics2D g) {
        drawBackground(g);
        drawBar(g);
        drawLabel(g);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the background, fading from transparent to solid black
     * over the first third of the loading duration.
     *
     * @param g drawing object
     */
    private void drawBackground(Graphics2D g) {
        float alpha = Math.min(progress * 3f, 1f);
        g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
        g.fillRect(0, 0, screenW, screenH);
    }

    /**
     * Draws the progress bar centered on screen: a dark track
     * with a purple fill that grows as barFill increases.
     *
     * @param g drawing object
     */
    private void drawBar(Graphics2D g) {
        int barX = screenW / 2 - BAR_W / 2;
        int barY = screenH / 2;

        // Track
        g.setColor(new Color(40, 35, 55));
        g.fillRoundRect(barX, barY, BAR_W, BAR_H, BAR_H, BAR_H);

        // Fill
        g.setColor(new Color(160, 130, 200));
        g.fillRoundRect(barX, barY, (int)(BAR_W * barFill), BAR_H, BAR_H, BAR_H);
    }

    /**
     * Draws the loading label just above the progress bar.
     *
     * @param g drawing object
     */
    private void drawLabel(Graphics2D g) {
        int barY  = screenH / 2;
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(120, 100, 150));
        String label = "...";
        int labelW = g.getFontMetrics().stringWidth(label);
        g.drawString(label, screenW / 2 - labelW / 2, barY - 14);
    }

    //------------------- State Transition -------------------
    @Override
    public GameState nextState() { return null; }
}