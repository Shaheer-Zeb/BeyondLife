/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import core.InputHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Shown after the boss is defeated.
 *
 * Plays a fade-in from black to deep purple, then reveals
 * orbiting sparkles, the victory title, subtitle, and a
 * closing prompt once the fade completes.
 *
 * @author EDEN COMPUTERS
 */
public class WinScreen implements GameState {

    //------------------- Core ----------------------------
    private final InputHandler input;
    private final int screenW, screenH;

    //------------------- Animation -----------------------
    private float fadeIn = 0f;
    private float starTimer = 0f;

    //------------------- Fade tuning --------------------
    private static final float FADE_SPEED      = 0.6f;
    private static final float CONTENT_START   = 0.3f;  // fade progress before content appears
    private static final int SPARKLE_COUNT   = 60;

    /**
     * Creates the win screen.
     *
     * @param input the input handler
     * @param screenW screen width in pixels
     * @param screenH screen height in pixels
     */
    public WinScreen(InputHandler input, int screenW, int screenH) {
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;
    }

    //---------------------- Update --------------------------

    /**
     * Advances the fade-in and sparkle animation each tick.
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(float dt) {
        if (fadeIn < 1f) fadeIn = Math.min(fadeIn + dt * FADE_SPEED, 1f);
        starTimer += dt;
        input.flushJustPressed();
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the background fade, sparkles, title, subtitle, and closing prompt in layers.
     *
     * @param g drawing object
     */
    @Override
    public void draw(Graphics2D g) {
        drawBackground(g);

        if (fadeIn < CONTENT_START) return;

        float contentAlpha = Math.max(0f, (fadeIn - CONTENT_START) / (1f - CONTENT_START));

        drawSparkles(g, contentAlpha);
        drawTitle(g, contentAlpha);
        drawSubtitle(g, contentAlpha);

        if (fadeIn >= 1f) drawPrompt(g);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the background, fading from black to deep purple
     *
     * @param g drawing object
     */
    private void drawBackground(Graphics2D g) {
        int r = (int)(10 * fadeIn);
        int gr = (int)(8  * fadeIn);
        int b = (int)(20 * fadeIn);
        g.setColor(new Color(r, gr, b));
        g.fillRect(0, 0, screenW, screenH);
    }

    /**
     * Draws orbiting sparkle dots that slowly rotate around the screen center.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawSparkles(Graphics2D g, float alpha) {
        g.setColor(new Color(200, 180, 255, (int)(80 * alpha)));
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            double angle = i * 0.523 + starTimer * 0.4;
            double dist  = 80 + (i % 5) * 60 + Math.sin(starTimer + i) * 20;
            int sx = (int)(screenW / 2 + Math.cos(angle) * dist);
            int sy = (int)(screenH / 2 - 30 + Math.sin(angle) * dist * 0.5);
            int sr = 1 + i % 3;
            g.fillOval(sx - sr, sy - sr, sr * 2, sr * 2);
        }
    }

    /**
     * Draws the victory title centered on screen.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawTitle(Graphics2D g, float alpha) {
        g.setFont(new Font("Serif", Font.BOLD, 58));
        g.setColor(new Color(230, 220, 255, (int)(255 * alpha)));
        String title = "Kash Itni Mehnat Parhai main kar letay";
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, screenW / 2 - titleW / 2, screenH / 2 - 20);
    }

    /**
     * Draws the subtitle below the title.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawSubtitle(Graphics2D g, float alpha) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        g.setColor(new Color(160, 140, 200, (int)(200 * alpha)));
        String[] lines = {
            "Thanks For Playing the Game",
            "Developed By:",
            "Shaheer Zeb Khan x Faseeh Ur Rehman"
        };
        int lineY = screenH / 2 + 28;
        for (String line : lines) {
            int lineW = g.getFontMetrics().stringWidth(line);
            g.drawString(line, screenW / 2 - lineW / 2, lineY);
            lineY += 30;
        }
       
    }

    /**
     * Draws the closing prompt, shown only once the fade is complete.
     *
     * @param g drawing object
     */
    private void drawPrompt(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        g.setColor(new Color(110, 95, 140));
        String prompt = "Go Touch Some Grass Now";
        int promptW = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, screenW / 2 - promptW / 2, screenH / 2 + 120);
    }

    //------------------- State Transition -------------------

    @Override
    public GameState nextState() { return null; }
}
