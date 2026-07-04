/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import core.InputHandler;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/**
 * Title screen shown when the game first launches.
 *
 * Displays the game title, subtitle, a flickering prompt,
 * and a controls legend. Transitions to the playing state
 * once the player presses Enter, Z, or Space.
 *
 * @author EDEN COMPUTERS
 */
public class StartScreen implements GameState {

    //------------------- Core ----------------------------
    private final InputHandler input;
    private final int screenW, screenH;
    private boolean ready = false;

    //------------------- Flicker -------------------------
    private float flickerTimer = 0f;
    private boolean flickerOn = true;
    private static final float FLICKER_INTERVAL = 0.55f;

    //------------------- Controls Legend ----------------
    private static final String[] CONTROLS = {
        "Arrow keys - Move",
        "Z - Jump / Wall jump",
        "X - Slash",
        "C - Soul projectile",
        "Shift - Dash (invincible)",
        "[Up Arrow Key] - Interact",
        "Hope We Win the Competition xd"
    };
    private static final int CONTROLS_LINE_HEIGHT = 32;

    /**
     * Creates the start screen.
     *
     * @param input the input handler
     * @param screenW screen width in pixels
     * @param screenH screen height in pixels
     */
    public StartScreen(InputHandler input, int screenW, int screenH) {
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;
    }

    //------------------- Getter --------------------------

    /**
     * @return true once the player has pressed a start key -
     *         pull this from the state manager to begin the game
     */
    public boolean isReady() { return ready; }

    //---------------------- Update --------------------------

    /**
     * Advances the flicker timer and checks for a start input.
     * Accepts Enter, Z, or Space as the trigger key.
     *
     * @param dt delta time
     */
    @Override
    public void update(float dt) {
        flickerTimer += dt;
        if(flickerTimer >= FLICKER_INTERVAL) {
            flickerOn = !flickerOn;
            flickerTimer = 0f;
        }

        if(input.isJustPressed(KeyEvent.VK_ENTER) ||
            input.isJustPressed(KeyEvent.VK_Z) ||
            input.isJustPressed(KeyEvent.VK_SPACE)){
            ready = true;
        }

        input.flushJustPressed();
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the background, title, subtitle, prompt, and controls legend.
     *
     * @param g drawing object
     */
    @Override
    public void draw(Graphics2D g) {
        drawBackground(g);
        drawTitle(g);
        drawSubtitle(g);
        drawPrompt(g);
        drawControls(g);
    }

    //---------------- Draw Helpers --------------------------

    /**
     * Draws the solid dark background.
     *
     * @param g drawing object
     */
    private void drawBackground(Graphics2D g) {
        g.setColor(new Color(8, 6, 14));
        g.fillRect(0, 0, screenW, screenH);
    }

    /**
     * Draws the game title centered above the midpoint.
     *
     * @param g drawing object
     */
    private void drawTitle(Graphics2D g){
        g.setFont(new Font("SansSerif", Font.BOLD, 100));
        g.setColor(new Color(220, 210, 240));
        String title = "Beyond Life";
        int titleW = g.getFontMetrics().stringWidth(title);
        g.drawString(title, screenW / 2 - titleW / 2, screenH / 2 - 100);
    }

    /**
     * Draws the subtitle just below the title.
     *
     * @param g drawing object
     */
    private void drawSubtitle(Graphics2D g) {
    g.setFont(new Font("Monospaced", Font.PLAIN, 18));
    g.setColor(Color.WHITE);

    String[] lines = {
        "A Hollow Knight inspired project by",
        "Shaheer Zeb Khan x Faseeh Ur Rehman"
    };

    int lineY = screenH / 2 - 250;
    for (String line : lines) {
        int lineW = g.getFontMetrics().stringWidth(line);
        g.drawString(line, screenW / 2 - lineW / 2, lineY);
        lineY += 40;
    }
}

    /**
     * Draws the flickering "Press Enter to begin" prompt.
     * Visibility is toggled every FLICKER INTERVAL seconds.
     *
     * @param g drawing object
     */
    private void drawPrompt(Graphics2D g) {
        if (!flickerOn) return;
        g.setFont(new Font("SansSerif", Font.PLAIN, 28));
        g.setColor(new Color(200, 190, 220));
        String prompt  = "Press Enter / Z / Space to begin";
        int promptW = g.getFontMetrics().stringWidth(prompt);
        g.drawString(prompt, screenW / 2 - promptW / 2, screenH / 2 - 10);
    }

    /**
     * Draws the controls legend centered below the prompt.
     *
     * @param g drawing object
     */
    private void drawControls(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.PLAIN, 32));
        g.setColor(new Color(90, 80, 110));
        int lineY = screenH / 2 + 70;
        for (String line : CONTROLS) {
            int lineW = g.getFontMetrics().stringWidth(line);
            g.drawString(line, screenW / 2 - lineW / 2, lineY);
            lineY += CONTROLS_LINE_HEIGHT;
        }
    }

    //------------------- State Transition -------------------
    @Override
    public GameState nextState() { return null; }
}