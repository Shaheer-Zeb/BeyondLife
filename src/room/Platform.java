/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package room;

import core.AssetManager;
import core.Camera;
import entity.Player;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * A static, one-way floating platform. The player can land on top of it
 * while falling, but passes freely through it from below or the sides.
 *
 * @author EDEN COMPUTERS
 */
public class Platform {

    private final int x, y, w, h;
    
    private static BufferedImage platfromImage = AssetManager.getImage("/assets/rooms/gauntlet/platform.png");

    public Platform(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public int getRight() { return x + w; }
    public int getBottom() { return y + h; }

    /**
     * Attempts to land the player on top of this platform. Only succeeds if
     * the player is horizontally over the platform, falling (not moving
     * upward), and within several pixels of the surface
     *
     * @param player  the player 
     * @param tolerance how many pixels below the platform's top still counts
     *                  as "landing" this tick
     * @return true if the player landed on this platform this tick
     */
    public boolean tryLand(Player player, float tolerance) {
        float playerLeft = player.getLeft();
        float playerRight = playerLeft + player.getWidth();
        float playerBottom = player.getTop() + player.getHeight();

        boolean horizontallyOverlapping = playerRight > x && playerLeft < getRight();
        boolean withinLandingRange = playerBottom >= y && playerBottom <= y + tolerance;
        boolean isFalling = player.getVelY() >= 0f;

        if (horizontallyOverlapping && withinLandingRange && isFalling) {
            player.setY(y - player.getHeight());
            player.setVelY(0);
            player.landOnGround();
            return true;
        }
        return false;
    }

    /**
     * Draws the platform 
     *
     * @param g drawing object
     * @param cam active camera
     */
    public void draw(Graphics2D g, Camera cam) {
        int drawX = (int) (x - cam.offsetX);
        int drawY = (int) (y - cam.offsetY);
        g.drawImage(platfromImage, drawX, drawY, w, h, null);
    }
}