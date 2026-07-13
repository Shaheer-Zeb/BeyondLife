/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.Camera;
import entity.Player;
import java.awt.Graphics2D;

/**
 * A horizontal boundary to mark a no entry zone
 * Room will decide the damage number
 *
 * @author EDEN COMPUTERS
 */
public class KillZone {

    private final float killY;
    private final float roomW;
    private final float roomH;

    public KillZone(float killY, float roomW, float roomH) {
        this.killY = killY;
        this.roomW = roomW;
        this.roomH = roomH;
    }

    public float getKillY() { return killY; }

    /**
     * @param player the active player
     * @return true if the player has fallen past this boundary
     */
    public boolean catches(Player player) {
        return player.getTop() > killY;
    }

    /**
     * Draws a filled band from the kill line to the bottom of the room —
     * a visual hint of danger even before real hazard art exists.
     *
     * @param g drawing object
     * @param cam active camera
     */
    public void draw(Graphics2D g, Camera cam) {

    }
}