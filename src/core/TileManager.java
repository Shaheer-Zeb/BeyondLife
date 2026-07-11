/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import room.BossRoom;
import room.VillageRoom;

/**
 *
 * @author ShaheerZK
 */
public class TileManager {
    
    // -------------------- Village Room Tiles --------------------
    private static int villageTileWidth = 34;
    private static int villageTileHeight = 17;
    private static int numberOfRowsInVillage = (int)VillageRoom.ROOM_H / villageTileHeight;
    private static int numberOfColumnsInVillage = (int)VillageRoom.ROOM_W / villageTileWidth;
    private static BufferedImage villageGrassTile = AssetManager.getImage("/assets/rooms/village/groundTile.png");
    private static BufferedImage villageDirtTile = AssetManager.getImage("/assets/rooms/village/villageDirt.png");
    private static Image villageWallTile = new ImageIcon(AssetManager.getImage("/assets/rooms/village/wallTile.png")).getImage();
    private static int padding = 7;
    
    // -------------------- Boss Room Tiles ------------------------
    private static int bossGroundTileWidth = 372, bossGroundTileHeight = 119;
    /**
     * Calls the drawGrass and the drawDirt method. It is called by the VillageRoom.
     * @param g
     * @param cam 
     */
    public static void drawVillageTiles(Graphics2D g, Camera cam){
        drawGrass(g, cam);
        drawDirt(g, cam);
        //drawVillageWalls(g, cam);
        
    }
    /**
     * Draws the grass tiles every frame.
     * @param g
     * @param cam 
     */
    private static void drawGrass(Graphics2D g, Camera cam){
        int xPos = 0;
        int yPos = VillageRoom.GROUND_Y;
        for (int i = 0; i <= numberOfColumnsInVillage; i++){
            if (xPos > VillageRoom.ROOM_W)
                xPos = 0;
            int drawX = (int)(xPos - cam.offsetX);
            int drawY = (int)(yPos - cam.offsetY) - padding; // subtracting padding because the grass tile's top wasn't aligning with the Player and the other stuff in the VillageRoom
            g.drawImage(villageGrassTile, drawX, drawY, villageTileWidth, villageTileHeight, null);
            xPos += villageTileWidth;
        }
    }
    /**
     * Draws the dirt in the remaining vertical space below the grass.
     * @param g
     * @param cam 
     */
    private static void drawDirt(Graphics2D g, Camera cam){
        int xPos = 0;
        int startingY = VillageRoom.GROUND_Y + villageTileHeight - padding;
        int yPos = startingY;

        // Calculating how many rows of dirt tiles we've got to draw under the grass tile
        int remainingHeight = (int)VillageRoom.ROOM_H - (VillageRoom.GROUND_Y + villageTileHeight);
        int rowsOfDirtToDraw = remainingHeight / villageTileHeight;
        
        for (int i = 0; i <= rowsOfDirtToDraw; i++)
        {
            xPos = 0;
            if (yPos > VillageRoom.ROOM_H)
                yPos = startingY;
            for (int j = 0; j < numberOfColumnsInVillage; j++)
            {
                int drawX = (int)(xPos - cam.offsetX);
                int drawY = (int)(yPos - cam.offsetY);
                g.drawImage(villageDirtTile, drawX, drawY, villageTileWidth, villageTileHeight, null);
                xPos += villageTileWidth;
            }
            yPos += villageTileHeight;
        }
    }
    /**
     * Draws the wall tiles to the left and the right of the VillageRoom.
     * @param g
     * @param cam 
     */
    private static void drawVillageWalls(Graphics2D g, Camera cam){
        int xPos = 0, yPos = 0;
        while (yPos < VillageRoom.GROUND_Y){
            int drawX = (int)(xPos - cam.offsetX);
            int drawY = (int)(yPos - cam.offsetY);
            g.drawImage(villageWallTile, drawX, drawY, villageTileWidth, villageTileHeight, null);
            yPos += villageTileHeight;
        }
        
        xPos = (int)VillageRoom.ROOM_W - villageTileWidth;
        yPos = 0;
        while (yPos < VillageRoom.GROUND_Y){
            int drawX = (int)(xPos - cam.offsetX);
            int drawY = (int)(yPos - cam.offsetY);
            g.drawImage(villageWallTile, drawX, drawY, villageTileWidth, villageTileHeight, null);
            yPos += villageTileHeight;
        }
    }
}
