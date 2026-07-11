/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.Camera;
import entity.Player;
import java.awt.Graphics2D;

/**
 *  Base class for all rooms
 *  Each room draws its tile map, entities, and update/draw logic
 * 
 * Each room is considered a mini world
 * 
 * @author EDEN COMPUTERS
 */
public abstract class Room {
    
    public final String id;
    
    //World(Actually room) Dimensions
    public final float roomW;
    public final float roomH;
    
    protected Room(String id ,float roomW, float roomH){
        this.id = id;
        this.roomW = roomW;
        this.roomH = roomH;
    }
    
    /**
     * Called every tick. Player is passed so that
     * collision detection and interactions can be checked
     * 
     * @param dt delta time 
     * @param player player object
     */
    public abstract void update(float dt, Player player, Camera cam);
    
    /**
     * Draw everything in the room except for
     * HUD which is handled by gameState
     * 
     * @param g drawing object
     * @param cam the active camera used to convert the entity's
     *            world coordinates into screen coordinates by applying the camera offsets
     */
    public abstract void draw(Graphics2D g, Camera cam);
    
    /**
     * checked every tick
     * 
     * @return String id for next room else null 
     */
    public String getNextRoomId(){
        return null;
    }
}
