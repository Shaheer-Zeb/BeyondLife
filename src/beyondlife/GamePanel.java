/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package beyondlife;

import beyondlife.core.InputHandler;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JPanel;

/**
 * Owns the delta-time based game loop, the InputHandler,
 * and the GameStateManager.
 *
 * Uses a background thread that updates game state and calls
 * repaint(); Swing's own double buffering handles the drawing.
 * 
 * @author Faseeh Ur Rehman
 */
public class GamePanel extends JPanel implements Runnable{
    
    //General constants
    public static final int SCREEN_W = 1550;
    public static final int SCREEN_H = 700;
    private static final int TARGET_FPS = 60;
    private static final long NS_PER_FRAME = 1_000_000_000L / TARGET_FPS; //nanoseconds per frame
    
    //a seperate thread so that swings event thread is not blocked
    private Thread gameThread;
    //volatile means when one thread changes this value, other threads immediately see the new value
    private volatile boolean isRunning = false;
    
    private final InputHandler input;
    
    public GamePanel(){
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setFocusable(true);
        
//      If set to true, all the drawing from this component will be done
//      in an offscreen painting buffer. 
//      The offscreen painting buffer will the be copied onto the screen. This prevents flickering
        setDoubleBuffered(true);
        
        input = new InputHandler();
        addKeyListener(input);
        
    }

 
    
    
    public void startGame(){
        if (gameThread == null){
            isRunning = true;
            gameThread = new Thread(this, "GameLoop");
            
            //this would trigger the run method automatically
            gameThread.start();
        }
    }
    
    public void stopGame(){
        isRunning = false;
    }
    
    @Override
    public void run(){
        
        long lastTime = System.nanoTime();
        
        while(isRunning){
            long now = System.nanoTime();
            
            //time elapsed since last second
            float deltaTime = (now - lastTime) * 1000_000_000f;
            lastTime = now;
            
//          gameStateManager.update(dt);
            //schedule the drawing on Event Dispatch Thread. paintcomponent method does the drawing
            repaint();
            
            long frameProcessingTime = System.nanoTime() - now;
            long leftNS = NS_PER_FRAME - frameProcessingTime;
            
            try {
                Thread.sleep(leftNS/100_000L, (int)(leftNS % 1000_000L));
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
            
        }
        
    }
    
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
    
    
}
