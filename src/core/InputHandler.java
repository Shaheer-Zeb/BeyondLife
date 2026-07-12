/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Faseeh Ur Rehman
 */
public class InputHandler extends KeyAdapter{
    
    private final Set<Integer> held = new HashSet<>();
    private final Set<Integer> justPressed = new HashSet<>();
    private final Set<Integer> justReleased = new HashSet<>();
    
    public boolean isHeld(int keyCode){

        return held.contains(keyCode);
    }
    
    public boolean isJustPressed(int keyCode){

        return justPressed.contains(keyCode);
    }
    
    public boolean isJustReleased(int keyCode){

        return justReleased.contains(keyCode);
    }
    
    @Override
    public void keyPressed(KeyEvent e){
        
        if(!held.contains(e.getKeyCode()))
            justPressed.add(e.getKeyCode());
        held.add(e.getKeyCode());
    }
    
    @Override
    public void keyReleased(KeyEvent e){
        held.remove(e.getKeyCode());
        justReleased.add(e.getKeyCode());
    }
    
    public void flushJustPressed(){
        justPressed.clear();
        justReleased.clear();
    }
    
}
