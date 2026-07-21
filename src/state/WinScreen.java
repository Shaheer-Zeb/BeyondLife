/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import core.InputHandler;
import core.SoundManager;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.util.Random;

/**
 * Shown after the boss is defeated.
 *
 * Plays a fade-in from black to deep purple with a soft radial vignette,
 * then reveals softly falling sparkles, a glowing victory title, and a
 * continuously scrolling credits crawl (movie-credits style) listing the
 * team, before settling into a breathing closing prompt once the fade
 * completes.
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
    private static final float FADE_SPEED = 0.6f;
    private static final float CONTENT_START = 0.3f;
    private static final int SPARKLE_COUNT = 60;

    //------------------- Falling sparkles -----------------
    private final float[] sparkleX = new float[SPARKLE_COUNT];
    private final float[] sparkleY = new float[SPARKLE_COUNT];
    private final float[] sparkleSpeed = new float[SPARKLE_COUNT];
    private final float[] sparklePhase = new float[SPARKLE_COUNT];
    private final int[] sparkleSize = new int[SPARKLE_COUNT];

    private static final float SPARKLE_MIN_SPEED = 35f;
    private static final float SPARKLE_MAX_SPEED = 95f;
    private static final float SPARKLE_DRIFT = 14f;

    //------------------- Credits crawl -----------------
    private static final float CREDITS_SCROLL_SPEED = 26f;
    private static final int CREDITS_LINE_HEIGHT = 26;

    private static final String[] CREDITS_LINES = {
        "",
        "We tried our best, man.",
        "",
        "",
        "Developed By",
        "",
        "Faseeh Ur Rehman",
        "Backend & Game Logic",
        "",
        "",
        "Shaheer Zeb Khan",
        "Frontend & Visuals",
        "",
        "",
        "Hope We Win the Competition",
        "",
        ""
    };

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
        SoundManager.playMusic("winScreen");
        initSparkles();
    }

    /**
     * Seeds each sparkle with a random horizontal position, fall speed,
     * sway phase, and size, staggering their starting heights above the
     * screen so they don't all begin falling in a single visible row.
     */
    private void initSparkles() {
        Random rng = new Random();
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            sparkleX[i] = rng.nextFloat() * screenW;
            sparkleY[i] = -rng.nextFloat() * screenH; // staggered start above the screen
            sparkleSpeed[i] = SPARKLE_MIN_SPEED + rng.nextFloat() * (SPARKLE_MAX_SPEED - SPARKLE_MIN_SPEED);
            sparklePhase[i] = rng.nextFloat() * (float)(Math.PI * 2);
            sparkleSize[i] = 1 + rng.nextInt(3);
        }
    }

    //---------------------- Update --------------------------

    /**
     * Advances the fade-in, falling sparkles, and credits scroll each tick.
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(float dt) {
        if (fadeIn < 1f) fadeIn = Math.min(fadeIn + dt * FADE_SPEED, 1f);
        starTimer += dt;

        for (int i = 0; i < SPARKLE_COUNT; i++) {
            sparkleY[i] += sparkleSpeed[i] * dt;
            if (sparkleY[i] > screenH + 10) {
                sparkleY[i] = -10;
                sparkleX[i] = (float) Math.random() * screenW;
            }
        }

        input.flushJustPressed();
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the background fade, falling sparkles, title, and credits
     * crawl in layers.
     *
     * @param g drawing object
     */
    @Override
    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g);

        if (fadeIn < CONTENT_START) return;

        float contentAlpha = Math.max(0f, (fadeIn - CONTENT_START) / (1f - CONTENT_START));

        drawSparkles(g, contentAlpha);
        drawTitle(g, contentAlpha);
        drawCredits(g, contentAlpha);

        if (fadeIn >= 1f) drawPrompt(g);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the background as a radial gradient (lighter purple center,
     * darker edges) that fades in from black, giving the scene depth
     * instead of a flat wash of color.
     *
     * @param g drawing object
     */
    private void drawBackground(Graphics2D g) {
        int centerR = (int)(28 * fadeIn);
        int centerG = (int)(20 * fadeIn);
        int centerB = (int)(48 * fadeIn);
        int edgeR = (int)(6 * fadeIn);
        int edgeG = (int)(4 * fadeIn);
        int edgeB = (int)(14 * fadeIn);

        float radius = Math.max(screenW, screenH) * 0.75f;
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point2D.Float(screenW / 2f, screenH / 2f - 20),
            radius,
            new float[]{0f, 1f},
            new Color[]{
                new Color(centerR, centerG, centerB),
                new Color(edgeR, edgeG, edgeB)
            }
        );

        g.setPaint(gradient);
        g.fillRect(0, 0, screenW, screenH);
    }

    /**
     * Draws sparkles gently falling from the top of the screen to the
     * bottom, each with its own speed and a slight side-to-side sway so
     * they read as drifting motes rather than uniform rain.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawSparkles(Graphics2D g, float alpha) {
        for (int i = 0; i < SPARKLE_COUNT; i++) {
            float sway = (float) Math.sin(starTimer * 0.8 + sparklePhase[i]) * SPARKLE_DRIFT;
            int sx = (int)(sparkleX[i] + sway);
            int sy = (int) sparkleY[i];

            // Per-star twinkle: independent phase so sparkles flicker unevenly
            float twinkle = 0.4f + 0.6f * (float)(0.5 + 0.5 * Math.sin(starTimer * 2.2 + sparklePhase[i]));
            int sparkleAlpha = (int)(130 * alpha * twinkle);
            int sr = sparkleSize[i];

            g.setColor(new Color(210, 190, 255, sparkleAlpha));
            g.fillOval(sx - sr, sy - sr, sr * 2, sr * 2);

            // Occasional larger sparkles get a faint cross glint for variety
            if (i % 7 == 0) {
                g.setColor(new Color(230, 210, 255, sparkleAlpha / 2));
                g.drawLine(sx - sr * 3, sy, sx + sr * 3, sy);
                g.drawLine(sx, sy - sr * 3, sx, sy + sr * 3);
            }
        }
    }

    /**
     * Draws the victory title centered on screen, with a soft layered
     * glow behind it and a gentle upward slide as it fades in.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawTitle(Graphics2D g, float alpha) {
        String title = "It's Over.";
        g.setFont(new Font("Serif", Font.BOLD, 58));
        int titleW = g.getFontMetrics().stringWidth(title);
        int titleX = screenW / 2 - titleW / 2;

        // Slide up slightly as it fades in, easing out
        float slide = (1f - alpha) * 14f;
        int titleY = (int)(screenH / 2 - 60 + slide);

        // Soft glow: a few offset passes at low alpha behind the crisp text
        for (int i = 3; i >= 1; i--) {
            g.setColor(new Color(150, 110, 255, (int)(18 * alpha)));
            g.drawString(title, titleX - i, titleY);
            g.drawString(title, titleX + i, titleY);
            g.drawString(title, titleX, titleY - i);
            g.drawString(title, titleX, titleY + i);
        }

        g.setColor(new Color(232, 224, 255, (int)(255 * alpha)));
        g.drawString(title, titleX, titleY);

        // Thin decorative underline that grows in with the title
        int lineW = (int)(titleW * 0.5f * alpha);
        g.setColor(new Color(150, 110, 255, (int)(140 * alpha)));
        g.fillRect(screenW / 2 - lineW / 2, titleY + 14, lineW, 2);
    }

    /**
     * Draws the credits as a continuously scrolling crawl, movie-credits
     * style: lines rise from below a clipped viewport and loop back to
     * the bottom once the whole list has scrolled past the top, so the
     * team's names and roles cycle indefinitely while the screen is up.
     *
     * @param g drawing object
     * @param alpha content fade-in alpha (0.0 - 1.0)
     */
    private void drawCredits(Graphics2D g, float alpha) {
        int areaTop = screenH / 2 - 4;
        int areaBottom = screenH - 30;
        int areaHeight = areaBottom - areaTop;
        if (areaHeight <= 0) return;

        int contentHeight = CREDITS_LINES.length * CREDITS_LINE_HEIGHT;
        float totalScrollRange = contentHeight + areaHeight;

        // Continuously loop the crawl: once it fully scrolls past the top,
        // wrap back around so the credits keep cycling.
        float scrollY = (starTimer * CREDITS_SCROLL_SPEED) % totalScrollRange;

        Shape previousClip = g.getClip();
        g.clipRect(0, areaTop, screenW, areaHeight);

        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        for (int i = 0; i < CREDITS_LINES.length; i++) {
            String line = CREDITS_LINES[i];
            if (line.isEmpty()) continue;

            int lineY = (int)(areaBottom - scrollY + i * CREDITS_LINE_HEIGHT);
            if (lineY < areaTop - CREDITS_LINE_HEIGHT || lineY > areaBottom + CREDITS_LINE_HEIGHT) continue;

            boolean isName = line.equals("Faseeh Ur Rehman") || line.equals("Shaheer Zeb Khan");
            boolean isHeading = line.equals("Developed By");

            if (isName) {
                g.setFont(new Font("SansSerif", Font.BOLD, 18));
                g.setColor(new Color(220, 205, 255, (int)(230 * alpha)));
            } else if (isHeading) {
                g.setFont(new Font("SansSerif", Font.BOLD, 15));
                g.setColor(new Color(180, 160, 220, (int)(200 * alpha)));
            } else {
                g.setFont(new Font("SansSerif", Font.PLAIN, 15));
                g.setColor(new Color(160, 140, 200, (int)(190 * alpha)));
            }

            int lineW = g.getFontMetrics().stringWidth(line);
            g.drawString(line, screenW / 2 - lineW / 2, lineY);
        }

        g.setClip(previousClip);
    }

    /**
     * Draws the closing prompt, shown only once the fade is complete,
     * with a slow breathing pulse so it reads as an active invitation
     * rather than static text.
     *
     * @param g drawing object
     */
    private void drawPrompt(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        String prompt = "Go Touch Some Grass Now";
        int promptW = g.getFontMetrics().stringWidth(prompt);

        float pulse = 0.5f + 0.5f * (float) Math.sin(starTimer * 1.6);
        int promptAlpha = (int)(90 + 70 * pulse);

        g.setColor(new Color(130, 112, 165, promptAlpha));
        g.drawString(prompt, screenW / 2 - promptW / 2, screenH - 10);
    }

    //------------------- State Transition -------------------

    @Override
    public GameState nextState() { return null; }
}