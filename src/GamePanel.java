/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import core.InputHandler;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import state.GameStateManager;

/**
 * Owns the delta-time based game loop, the InputHandler,
 * and the GameStateManager.
 *
 * Uses a background thread that updates game state and calls
 * repaint(); Swing's own double buffering handles the drawing.
 *
 * @author Faseeh Ur Rehman
 */
public class GamePanel extends JPanel implements Runnable {

    //------------------- Screen Constants ----------------
    public static final int SCREEN_W = 1300;
    public static final int SCREEN_H = 700;
    private static final int TARGET_FPS = 60;
    private static final long NS_PER_FRAME = 1_000_000_000L / TARGET_FPS;

    // Maximum delta clamped to avoid huge physics jumps on debugger pauses or focus loss. 
    private static final float MAX_DT = 0.05f;

    //------------------- Thread --------------------------
    private Thread gameThread;
    private volatile boolean isRunning = false;

    //------------------- Core ----------------------------
    private final InputHandler input;
    private final GameStateManager gameStateManager;

    /**
     * Creates the panel, wires up input, and initializes the state manager.
     */
    public GamePanel(){
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setFocusable(true);
        setDoubleBuffered(true);

        input = new InputHandler();
        addKeyListener(input);

        gameStateManager = new GameStateManager(input, SCREEN_W, SCREEN_H);
    }

    //------------------- Thread Control ------------------

    /**
     * Starts the game loop on a dedicated thread.
     * No-op if the loop is already running.
     */
    public void startGame() {
        if (gameThread == null) {
            isRunning  = true;
            gameThread = new Thread(this, "GameLoop");
            gameThread.start();
        }
    }

    /**
     * Signals the game loop to stop after the current frame.
     */
    public void stopGame(){
        isRunning = false;
    }

    //------------------- Game Loop -----------------------

    /**
     * Delta-time game loop: updates state then schedules a repaint,
     * sleeping for the remainder of each frame budget.
     * Delta is clamped to prevent physics spikes.
     */
    @Override
    public void run() {
        long lastTime = System.nanoTime();

        while (isRunning) {
            long now = System.nanoTime();

            float dt = (now - lastTime) / 1_000_000_000f;
            if(dt > MAX_DT) dt = MAX_DT;
            lastTime = now;

            update(dt);
            repaint();

            long frameTime = System.nanoTime() - now;
            long sleepNs = NS_PER_FRAME - frameTime;

            if(sleepNs > 0){
                try {
                    Thread.sleep(sleepNs / 1_000_000L, (int)(sleepNs % 1_000_000L));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Advances the active game state by one tick.
     *
     * @param dt delta time in seconds
     */
    private void update(float dt) {
        gameStateManager.update(dt);
    }

    //------------------- Draw ----------------------------

    /**
     * Delegates all drawing to the active game state via the manager.
     *
     * @param g the graphics context provided by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gameStateManager.draw(g2);
    }
}