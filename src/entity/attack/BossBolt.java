/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity.attack;

/**
 *
 * @author EDEN COMPUTERS
 */
public class BossBolt {
    

    private float x, y, vx;
    private boolean active = true;
    public static final int SIZE = 18 * 4;
        
    public BossBolt(float x, float y, float vx) { 
        this.x = x;
        this.y = y;
        this.vx = vx;
    }
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getVx() {
        return vx;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
        
}
