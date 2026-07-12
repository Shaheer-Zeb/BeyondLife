/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package core;

import java.io.*;

/**
 * Plain data object that holds everything needed to re spawn / reload.
 * Serialized to disk by Bench.saveGame() and read on death/load.
 */
public class SaveData implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String SAVE_PATH = "save.dat";

    public float spawnX;
    public float spawnY;
    public String roomId;

    public SaveData(float spawnX, float spawnY, String roomId) {
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.roomId = roomId;
    }

    //---------------------- Persistence --------------------------

    public void writeToDisk() {
        try(ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_PATH))){
            System.out.println("Game Saved");
            out.writeObject(this);
            System.out.println("Game Saved");
        }catch(IOException e){
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    /**
     * Returns null if no save file exists. 
     */
    public static SaveData readFromDisk(){
        
        File f = new File(SAVE_PATH);
        if (!f.exists()) return null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_PATH))){
            return (SaveData) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Load failed: " + e.getMessage());
            return null;
        }
    }
}
