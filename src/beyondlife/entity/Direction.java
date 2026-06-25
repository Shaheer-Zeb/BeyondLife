/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package beyondlife.entity;

/**
 *
 * @author EDEN COMPUTERS
 */
public enum Direction {
    UP(0),
    RIGHT(1),
    DOWN(2),
    LEFT(-1);
    
    private final int dir;
    
    Direction(int x){
        this.dir = x;
    }
    
    public int getValue(){
        return dir;
    }
}
