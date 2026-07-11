/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Loads and stores the BufferedImages (used for sprites) in a Hashmap, and could be retrieved with the path. Already loaded images don't require reloading.
 * @author ShaheerZK
 */
public class AssetManager {
    private static final Map<String, BufferedImage> images = new HashMap<>();

    /**
     * Returns the BufferedImage if its path already exists in the Hashmap, else it'd load the image in the Hashmap and return it.
     * @param path
     * @return 
     */
    public static BufferedImage getImage(String path) {
        return images.computeIfAbsent(path, AssetManager::loadImage);
    }
    /**
     * Loads the BufferedImage in the Hashmap.
     * @param path
     * @return 
     */
    private static BufferedImage loadImage(String path) {
        try 
        {
            return ImageIO.read(AssetManager.class.getResourceAsStream(path));
        } 
        catch (IOException | IllegalArgumentException e) {
            throw new RuntimeException("Failed to load image: " + path, e);
        }
    }
}
