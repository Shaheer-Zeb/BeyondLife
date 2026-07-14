package room;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import entity.Player;
import entity.Walker;
import entity.attack.Slash;
import entity.attack.SoulProjectile;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author EDEN COMPUTERS
 */
public class GauntletRoom2 extends Room {

    //--------------- Room Layout ----------------------
    public static final float ROOM_H = 4000;
    public static final float ROOM_W = 2000;
    private static final int CEILING_Y = 60;
    private static final int ROOM_LEFT = 0;
    private static final int ROOM_RIGHT = (int) ROOM_W;

    private static final String NEXT_ROOM_ID = "shaft_room";

    //------------------ Platform & Kill Zone ----------------
    private static final int PLATFORM_H = 40;
    private static final float LAND_TOLERANCE = 14f;
    private static final int PLATFORM_COUNT = 20;
    private final int KILLZONE_HEIGHT = 40;
    private final KillZone killZone = new KillZone(ROOM_H - KILLZONE_HEIGHT, ROOM_W, ROOM_H);

    //------------------- Door ---------------------------
    private static final int DOOR_W = 128;
    private static final int DOOR_H = 128;
    private final int doorX;
    private final int doorY;
    private boolean doorTriggered = false;
    private Image doorPortalGif = Toolkit.getDefaultToolkit().getImage("src/assets/rooms/village/doorPortal1.gif");

    //------------------- Checkpoint (respawn point) ------------
    private float lastSafeX;
    private float lastSafeY;

    //----------------- Contact damage iframes ------------------
    private static final float CONTACT_IFRAME_DURATION = 1.0f;
    private float contactIframeTimer = 0f;

    // -------------------- Parallax Background --------------------
    private BufferedImage sky = AssetManager.getImage("/assets/rooms/gauntlet/sky.png");
    private BufferedImage cloudsOne = AssetManager.getImage("/assets/rooms/gauntlet/clouds_1.png");
    private BufferedImage cloudsTwo = AssetManager.getImage("/assets/rooms/gauntlet/clouds_2.png");
    private BufferedImage rocks = AssetManager.getImage("/assets/rooms/gauntlet/rocks.png");
    private BufferedImage ground = AssetManager.getImage("/assets/rooms/gauntlet/ground.png");

    ArrayList<Platform> platforms;
    ArrayList<Walker> walkers;

    InputHandler input;

    private final float startX = 80f;
    private final float startY = 800f;

    public GauntletRoom2(InputHandler input) {
        super("gauntlet2_room", ROOM_W, ROOM_H);

        this.input = input;

        platforms = new ArrayList<>();
        walkers = new ArrayList<>();

        // First platform is fixed, so the player has somewhere safe to spawn.
        platforms.add(new Platform((int) startX, (int) startY, 220, PLATFORM_H));

        initPlatforms();

        Platform pLast = findLowestPlatform();
        doorX = (int) (pLast.getX() + pLast.getWidth() / 2f - DOOR_W / 2f);
        doorY = (int) (pLast.getY() - DOOR_H + 17);

        // Initial checkpoint = spawn platform, not a per-frame guess.
        lastSafeX = startX;
        lastSafeY = startY - PLATFORM_H;
    }

    /**
     * Scatters platforms (and every other one, a Walker) down the shaft.
     * Each platform gets its own random slot, split into vertical bands so
     * there's always a reachable gap between one platform and the next
     * instead of everything landing on the same random spot.
     */
    public void initPlatforms() {
        Random rand = new Random();

        float usableHeight = ROOM_H - KILLZONE_HEIGHT - 60; 
        float bandHeight = usableHeight / PLATFORM_COUNT;

        for (int i = 0; i < PLATFORM_COUNT; i++) {
            int platformWidth = rand.nextInt(150, 250);
            int platformX = rand.nextInt(0, (int) (ROOM_W - platformWidth));

            float bandTop = 60 + i * bandHeight;
            float bandBottom = bandTop + bandHeight;
            int platformY = (int) (bandTop + rand.nextFloat() * (bandBottom - bandTop));

            Platform platform = new Platform(platformX, platformY, platformWidth, PLATFORM_H);
            platforms.add(platform);

            if (i % 2 == 0) {
                walkers.add(new Walker(
                        platform.getX() + 10,
                        platform.getY() - Walker.WALKER_H,
                        platform.getY(),
                        input,
                        platform.getX(),
                        platform.getRight(),
                        rand.nextInt(2) == 0 ? Walker.WalkerType.DINO : Walker.WalkerType.SLIME));
            }
        }
    }

    /**
     * @return the platform sitting deepest in the shaft (highest Y value),
     *         used to place the exit door at the true bottom of the room.
     */
    private Platform findLowestPlatform() {
        Platform lowest = platforms.get(0);
        for (Platform p : platforms) {
            if (p.getY() > lowest.getY()) {
                lowest = p;
            }
        }
        return lowest;
    }

    @Override
    public void update(float dt, Player player, Camera cam) {
        float playerCenterX = player.getLeft() + player.getWidth() / 2f;
        float playerFeetY = player.getTop() + player.getHeight();

        for (Walker w : walkers) {
            w.setPlayerPosition(playerCenterX, playerFeetY);
            checkWalkerDamage(player, w);

            if (!w.isDead()) {
                w.update(dt);
                checkWalkerContact(player, w, cam);
            }
        }

        updatePlayerPhysics(player);
        checkKillZone(player);
        checkDoorTrigger(player);
        if (contactIframeTimer > 0f) {
            contactIframeTimer -= dt;
        }
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        drawBackground(g, cam);

        for (Platform p : platforms) {
            p.draw(g, cam);
        }
        killZone.draw(g, cam);
        drawDoor(g, cam);

        for (Walker w : walkers) {
            w.draw(g, cam);
        }
    }

    private void drawBackground(Graphics2D g, Camera cam) {
        int roomWidth = (int) ROOM_W, roomHeight = (int) ROOM_H;

        int drawX = (int) (-cam.offsetX), drawY = (int) (-cam.offsetY);
        g.drawImage(sky, drawX, drawY, roomWidth, roomHeight, null);

        drawX = (int) (-cam.offsetX * 0.2);
        drawY = (int) (-cam.offsetY * 0.3);
        g.drawImage(cloudsOne, drawX, drawY, roomWidth, roomHeight, null);

        drawX = (int) (-cam.offsetX * 0.3);
        drawY = (int) (-cam.offsetY * 0.3);
        g.drawImage(cloudsTwo, drawX, drawY, roomWidth, roomHeight, null);

        drawX = (int) (-cam.offsetX * 0.5);
        drawY = (int) (-cam.offsetY * 0.5);
        g.drawImage(rocks, drawX, drawY, roomWidth, roomHeight, null);
    }

    private void drawDoor(Graphics2D g, Camera cam) {
        int drawX = (int) (doorX - cam.offsetX);
        int drawY = (int) (doorY - cam.offsetY);
        g.drawImage(doorPortalGif, drawX, drawY, DOOR_W, DOOR_H, null);
    }

    // ------------------- Kill Zone ---------------------
    private void checkKillZone(Player player) {
        if (killZone.catches(player)) {
            player.takeDamage(1);
            player.setX(lastSafeX);
            player.setY(lastSafeY);
            player.setVelX(0);
            player.setVelY(0);
        }
    }

    //------------------- Door Trigger -------------------------
    private void checkDoorTrigger(Player player) {
        if (doorTriggered) return;

        boolean overlapping =
            player.getLeft() < doorX + DOOR_W &&
            player.getLeft() + player.getWidth() > doorX &&
            player.getTop() < doorY + DOOR_H &&
            player.getTop() + player.getHeight() > doorY;

        if (overlapping) doorTriggered = true;
    }

    //------------------- Room Transition --------------------
    @Override
    public String getNextRoomId() {
        return doorTriggered ? NEXT_ROOM_ID : null;
    }

    //---------------------- Walker Check ---------------------
    private void checkWalkerContact(Player player, Walker walker, Camera cam) {
        if (player.isInvincible()) return;
        if (contactIframeTimer > 0) return;

        if (player.overlap(walker)) {
            player.takeDamage(1);
            contactIframeTimer = CONTACT_IFRAME_DURATION;

            float walkerCenter = walker.getLeft() + walker.getWidth() / 2f;
            float playerCenter = player.getLeft() + player.getWidth() / 2f;
            float knockDir = playerCenter < walkerCenter ? -1f : 1f;
            player.applyKnockback(knockDir * 350f, -200f, cam);
        }
    }

    private void checkWalkerDamage(Player player, Walker walker) {
        if (walker.isDead()) return;

        for (var slash : player.getSlashes()) {
            if (!slash.isActive()) continue;
            if (slash.overlapsRect(walker.getLeft(), walker.getTop(), walker.getWidth(), walker.getHeight())) {
                walker.takeDamage(Slash.DAMAGE);
                player.gainSoul(Player.SOUL_PER_HIT);
                slash.deactivate();
            }
        }

        for (var proj : player.getProjectiles()) {
            if (!proj.isActive()) continue;
            if (proj.overlapsRect(walker.getLeft(), walker.getTop(), walker.getWidth(), walker.getHeight())) {
                walker.takeDamage(SoulProjectile.DAMAGE);
                proj.deactivate();
            }
        }
    }

    /**
     * Resolves platform landings, ceiling, and side walls.
     * Only updates the checkpoint when the player actually lands.
     *
     * @param player the active player
     */
    private void updatePlayerPhysics(Player player) {
        player.leaveGround();
        for (Platform p : platforms) {
            if (p.tryLand(player, LAND_TOLERANCE)) {
                lastSafeX = player.getLeft();
                lastSafeY = player.getTop();
            }
        }

        if (player.getTop() <= CEILING_Y) {
            player.setY(CEILING_Y);
            player.setVelY(0);
        }

        if (player.getLeft() <= ROOM_LEFT) {
            player.setX(ROOM_LEFT);
            player.setVelX(0);
            player.clingToWall(false);
        } else if (player.getLeft() + player.getWidth() >= ROOM_RIGHT) {
            player.setX(ROOM_RIGHT - player.getWidth());
            player.setVelX(0);
            player.clingToWall(true);
        } else {
            player.leaveWall();
        }
    }
}