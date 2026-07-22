/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import core.AssetManager;
import core.Camera;
import core.InputHandler;
import core.SoundManager;
import entity.attack.BossBolt;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

/**
 * The boss enemy
 *
 *  Dormant - The boss is dormant -> fight begins once challenged
 *  Patrol - Moves towards the player, then randomly picks its next attack
 *  Leap - Jumps and slams the ground creating a shockwave
 *  Slam - Ground slam landing recovery before the beam fires
 *  Charge - Fast horizontal dash lunge toward the player
 *  Barrage - Fires a volley of ranged bolts at the player
 *  Vulnerable - can take damage during this state
 * 
 * @author EDEN COMPUTERS
 */
public class Boss extends Entity implements ActionListener{

    // ----------- Characteristics ----------------------- 
    public final static int BOSS_H = 210;
    public final static int BOSS_W = 140;

    //---------------- Health ------------------------ 

    private final static int BOSS_MAX_HP = 150;

    //--------------------- Physics -------------------------------
    private final float GRAVITY = 1100f;
    private final float WALK_SPEED = 220f;
    private final int groundY;
    private boolean onGround = false;

    private enum State { DORMANT, PATROL, LEAP, SLAM, CHARGE, BARRAGE, VULNERABLE }
    private State state = State.DORMANT;

    //------------------- Fight trigger --------------------------
    private final float TRIGGER_RANGE = 160f;
    private boolean fightStarted = false;

    /**
     * Sub-states of the {@code LEAP} attack, tracked as a single enum
     * instead of multiple independent booleans to guarantee only one
     * phase of the leap is ever active at a time.
     */
    private enum LeapPhase { RISING, HANGING, ARCING }
    private LeapPhase leapPhase = LeapPhase.RISING;

    /** Sub-phases of the {@code CHARGE} attack. */
    private enum ChargePhase { TELEGRAPH, DASHING, RECOVER }
    private ChargePhase chargePhase = ChargePhase.TELEGRAPH;

    /** Sub-phases of the {@code BARRAGE} attack. */
    private enum BarragePhase { TELEGRAPH, FIRING, RECOVER }
    private BarragePhase barragePhase = BarragePhase.TELEGRAPH;

    //-------------------- Patrol -----------------------
    private float patrolTimer = 0;
    private int patrolDir = -1; // 1 for right, -1 for left
    private final float PATROL_DURATION = 2f;

    //--------------------- Slam ----------------------
    private float slamTimer = 0;
    private final float SLAM_DURATION = 0.7f;

    //----------------- Leap tuning -------------------------------
    private float hangTimer = 0;
    private final float HANG_DURATION = 0.5f;
    private final float HANG_THRESHOLD_Y = 40f;
    private final float LEAP_LAUNCH_VY = -800f;
    private final float SLAM_GRAVITY = 1500f;
    private final float ARC_START_VY = 240f;
    private final float ARC_DURATION = 0.20f;

    private float leapTargetX = 0f;

    //----------------- Charge attack tuning -------------------------
    private float chargeTimer = 0;
    private final float CHARGE_TELEGRAPH_DURATION = 0.5f; // wind-up before the dash
    private final float CHARGE_DURATION = 0.6f;            // max dash time before stopping
    private final float CHARGE_RECOVER_DURATION = 0.4f;
    private final float CHARGE_SPEED = 900f;
    private float chargeTargetX = 0f;
    private Image dashEffectGif = AssetManager.getGif("/assets/sprites/boss/bossDash.gif");
    private final int dashGifWidth = 32 * 6, dashGifHeight = 32 * 6;


    //----------------- Barrage attack tuning -------------------------
    private float barrageTimer = 0;
    private final float BARRAGE_TELEGRAPH_DURATION = 0.4f;
    private final float BARRAGE_RECOVER_DURATION = 0.5f;
    private final float BOLT_SPEED = 500f;
    private final float BOLT_INTERVAL = 0.25f;
    private final int BOLT_COUNT = 3;
    private int boltsFired = 0;
    private float boltFireCooldown = 0;
    private final List<BossBolt> bolts = new ArrayList<>();
    private Image boltGif = AssetManager.getGif("/assets/sprites/boss/bossBolt.gif");
    

    //----------------- Vulnerable window --------------------------
    private float vulnerableTimer = 0;
    private final float VULN_DURATION = 2.5f;

    //---------------------- Beam --------------------------
    private boolean beamActive = false;
    private float beamLeftX, beamRightX;
    private float beamOriginX, beamOriginY;

    private final float BEAM_SPEED_MAX = 1500f;
    private final float BEAM_SPEED_MIN = 400f;
    
    private BufferedImage shockBeamImage = AssetManager.getImage("/assets/sprites/boss/leftShockWave.png");
    private final int shockBeamImageWidth = 204, shockBeamImageHeight = 116;
    private final int SLAB_WIDTH = shockBeamImageWidth;
    private final int SLAB_HEIGHT = shockBeamImageHeight;

    
    private final InputHandler input;

    //reference to player
    private float playerX;
    
    // Room values to enforce clamping
    private final int roomLeft;
    private final int roomRight;
    
    // ------------------- Sprite Stuff -----------------------
    private final BufferedImage spriteSheet = AssetManager.getImage("/assets/sprites/boss/spriteSheet.png");
    private final int SPRITE_WIDTH = 81, SPRITE_HEIGHT = 71;
    private int sheetX, sheetY;
    private enum SPRITEACTION{
        ATTACK, DEATH, FLYING, HURT, IDLE;
    }
    private int spriteRowNumber = SPRITEACTION.IDLE.ordinal();
    private final int spriteChangeDelay = 100;
    private final Timer spriteTimer;
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        sheetX += SPRITE_WIDTH;
        handleSpriteRow();
        handleSpriteRepetition();
        sheetY = spriteRowNumber * SPRITE_HEIGHT;
    }
    private void handleSpriteRow(){
        if (state != null)
            switch (state) 
            {
                case DORMANT, VULNERABLE -> spriteRowNumber = SPRITEACTION.IDLE.ordinal();
                case PATROL, CHARGE -> spriteRowNumber = SPRITEACTION.FLYING.ordinal();
                case SLAM, BARRAGE -> spriteRowNumber = SPRITEACTION.ATTACK.ordinal();
            }
        if (isDead())
            spriteRowNumber = SPRITEACTION.DEATH.ordinal();
        
    }
    private void handleSpriteRepetition(){
        switch (spriteRowNumber)
        {
            case 0 -> sheetX = (sheetX > 7 * SPRITE_WIDTH) ? 0 : sheetX;
            case 1 -> sheetX = (sheetX > 4 * SPRITE_WIDTH) ? 5 * SPRITE_WIDTH : sheetX;
            case 2 -> sheetX = (sheetX > 3 * SPRITE_WIDTH) ? 0 : sheetX;
            case 3 -> sheetX = (sheetX > 3 * SPRITE_WIDTH) ? 0 : sheetX;
            case 4 -> sheetX = (sheetX > 3 * SPRITE_WIDTH) ? 0 : sheetX;
        }
    }
    private void drawBoss(Graphics2D g, Camera cam) {
        float drawX = getLeft() - cam.offsetX;
        float drawY = getTop() - cam.offsetY;
        drawChallengeOption(g, drawX, drawY);

        // makes the boss a little transparent while the current state is vulnerable
        float alpha = 0.87f;
        Composite originalComposite = g.getComposite();
        if (state == State.VULNERABLE){
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.setColor(Color.WHITE);
        }
        // Body
        BufferedImage spriteFrame = spriteSheet.getSubimage(sheetX, sheetY, SPRITE_WIDTH, SPRITE_HEIGHT);
        if (patrolDir == -1)
            g.drawImage(spriteFrame, (int)drawX, (int)drawY, BOSS_W, BOSS_H, null);
        else if (patrolDir == 1)
            g.drawImage(spriteFrame, (int)drawX + BOSS_W, (int)drawY, -BOSS_W, BOSS_H, null);
        g.setComposite(originalComposite);
        if (chargePhase == ChargePhase.DASHING)
            drawDashEffect(g, cam);
        if(state != State.DORMANT)
            drawHealthBar(g, drawX, drawY);

    }
    private void drawDashEffect(Graphics2D g, Camera cam){
        int drawX = (int)(((patrolDir == 1) ? getLeft() - dashGifWidth + 20 : getRight() - 20) - cam.offsetX);
        int drawY = (int)(groundY - dashGifHeight - cam.offsetY);
        if (patrolDir == -1)
            g.drawImage(dashEffectGif, drawX, drawY, dashGifWidth, dashGifHeight, null);
        else if (patrolDir == 1)
            g.drawImage(dashEffectGif, drawX + dashGifWidth, drawY, -dashGifWidth, dashGifHeight, null);
    }
    private void drawChallengeOption(Graphics2D g, float drawX, float drawY){
        float dist = Math.abs((playerX) - (getLeft() + BOSS_W / 2f));
        if(!fightStarted && dist <= TRIGGER_RANGE){
            g.setFont(new Font("Monospaced", Font.BOLD, 30));
            g.setColor(Color.BLACK);
            String line = "Challenge";
            int lineW = g.getFontMetrics().stringWidth(line);
            g.drawString(line, (int)(drawX + getWidth() / 2f - lineW / 2f), (int)(drawY + getHeight() - 230));
        } 
    }
    /**
     * Creates the boss centered horizontally on screen, standing on the ground.
     * @param x
     * @param y
     * @param groundY
     * @param input
     * @param roomLeft
     * @param roomRight 
     */
    public Boss(float x, float y, int groundY ,InputHandler input, int roomLeft, int roomRight){
        super(x, y, BOSS_W, BOSS_H, BOSS_MAX_HP);
        this.groundY = groundY;
        this.input = input;
        this.roomLeft = roomLeft;
        this.roomRight = roomRight;
        
        spriteTimer = new Timer(spriteChangeDelay, this);
        spriteTimer.start();
    }

    //----------------- Getters ----------------------

    public boolean isBeamActive() { return beamActive; }
    public float getBeamLeftX() { return beamLeftX; }
    public float getBeamRightX() { return beamRightX; }
    public float getBeamOriginY() { return beamOriginY; }
    public int getSlabWidth() { return SLAB_WIDTH; }
    public int getSlabHeight() { return SLAB_HEIGHT; }

    /** 
     * @return true once the fight has been triggered (boss is no longer dormant).
     */
    public boolean isFightStarted() { return fightStarted; }

    /**
     * Attempts to start the boss fight, Mantis-Lords-style: the boss
     * sits inert in Dormant until the player walks within range and
     * presses UP to challenge it.
     *
     * @param playerX player's current x position
     * @return true if this call is what started the fight
     */
    public boolean tryStart(float playerX) {
        if (fightStarted) return false;
        
        float dist = Math.abs((playerX) - (getLeft() + BOSS_W / 2f));
        
        if(input.isJustPressed(KeyEvent.VK_UP) && dist <= TRIGGER_RANGE){
            fightStarted = true;
            state = State.PATROL;
            patrolTimer = 0;
            return true;
        }
        
        return false;
    }
    
    public void setPlayerPosition(float playerX){
        this.playerX = playerX; 
    }


    /**
     * Checks whether the given player hit box overlaps either side of the
     * active beam slab.
     *
     * @param px player x
     * @param py player y
     * @param pw player hit box width
     * @param ph player hit box height
     * @return true if the player's hit box intersects either beam slab
     */
    public boolean beamHits(float px, float py, int pw, int ph) {
        if (!beamActive) return false;
        float pr = px + pw, pb = py + ph;
        float slabTop = beamOriginY - SLAB_HEIGHT;

        boolean verticallyInBeam = pb > slabTop && py < beamOriginY;
        if (!verticallyInBeam) return false;

        boolean hitsRight = pr > beamRightX && px < beamRightX + SLAB_WIDTH;
        boolean hitsLeft  = pr > beamLeftX - SLAB_WIDTH && px < beamLeftX;
        return hitsRight || hitsLeft;
    }

    /**
     * Checks all active barrage bolts against the given player hit box.
     * Any bolt that overlaps is consumed (deactivated) on contact.
     *
     * @param px player x
     * @param py player y
     * @param pw player hit box width
     * @param ph player hit box height
     * @return true if at least one bolt hit the player this call
     */
    public boolean boltHits(float px, float py, float pw, float ph) {
        boolean hit = false;
        for (BossBolt b : bolts) {
            if (!b.isActive()) continue;
            float bx = b.getX() - BossBolt.SIZE / 2f;
            float by = b.getY() - BossBolt.SIZE / 2f;
            boolean overlap = px < bx + BossBolt.SIZE && px + pw > bx
                            && py < by + BossBolt.SIZE && py + ph > by;
            if (overlap) { 
                b.setActive(false);
                hit = true;
            }
        }
        return hit;
    }

    /** 
     * Applies gravity (boosted while slam-falling) and resolves ground contact.
     */
    private void updateVerticalPhysics(float dt) {
        boolean falling = state == State.LEAP && leapPhase == LeapPhase.ARCING;
        if (!onGround && !(state == State.LEAP && leapPhase == LeapPhase.HANGING)) {
            float g = falling ? GRAVITY + SLAM_GRAVITY : GRAVITY;
            setVelY(getVelY() + g * dt);
        }

        setY(getTop() + getVelY() * dt);

        if (getBottom() >= groundY ) {
            setY(groundY - BOSS_H);
            setVelY(0);
            boolean wasLeaping = !onGround && state == State.LEAP;
            onGround = true;
            if (wasLeaping) {
                enterSlam();
            }
        } else {
            onGround = false;
        }
    }

    /** Clamps the boss to the screen and flips patrol direction at the edges. */
    private void enforceScreenBounds() {
        if (getLeft() < roomLeft) {
            setX(roomLeft);
            setVelX(0);
            patrolDir = 1;
        }
        if (getLeft() > roomRight - BOSS_W) {
            setX(roomRight - BOSS_W);
            setVelX(0);
            patrolDir = -1;
        }
    }

    /** Walks toward the player and counts down to the next attack. */
    private void updatePatrol(float dt, float playerX) {
        patrolDir = (playerX > getLeft()) ? 1 : -1;
        setX(getLeft() + WALK_SPEED * patrolDir * dt);
        patrolTimer += dt;
        if (patrolTimer >= PATROL_DURATION) {
            patrolTimer = 0;
            chooseNextAttack(playerX);
        }
    }

    /** Randomly selects the boss's next attack once patrol time is up. */
    private void chooseNextAttack(float playerX) {
        int roll = (int) (Math.random() * 3);
        switch (roll) {
            case 0 -> enterLeap(playerX);
            case 1 -> enterCharge(playerX);
            default -> enterBarrage();
        }
    }

    /** Begins the leap: launches upward and resets the leap sub-state. */
    private void enterLeap(float playerX) {
        state = State.LEAP;
        leapPhase = LeapPhase.RISING;
        onGround = false;
        hangTimer = 0;
        leapTargetX = playerX;
        setVelX(0);
        setVelY(LEAP_LAUNCH_VY);
        SoundManager.playSfx("leap");
    }

    /**
     * Drives the leap's three sub-phases:
     */
    private void updateLeap(float dt){
        switch (leapPhase) {
            case RISING -> {
                if (getVelY() >= 0 && getTop() < groundY - BOSS_H - HANG_THRESHOLD_Y) {
                    leapPhase = LeapPhase.HANGING;
                    setVelY(0);
                }
            }
            case HANGING -> {
                setVelY(0);
                hangTimer += dt;
                if (hangTimer >= HANG_DURATION) {
                    leapPhase = LeapPhase.ARCING;
                    float distX = leapTargetX - getLeft();
                    setVelX(distX / ARC_DURATION);
                    setVelY(ARC_START_VY);
                }
            }
            case ARCING -> setX(getLeft() + getVelX() * dt);
        }
    }

    /** Transitions from landing into the slam recovery + beam attack. */
    private void enterSlam() {
        state = State.SLAM;
        slamTimer = 0;
        setVelX(0);

        beamActive = true;
        beamOriginX = getLeft() + BOSS_W / 2f;
        beamOriginY = groundY - SLAB_HEIGHT;
        beamRightX = beamOriginX;
        beamLeftX = beamOriginX;
        
        SoundManager.playSfx("slam");
    }

    /** Counts down the post-landing recovery before the vulnerable window opens. */
    private void updateSlam(float dt) {
        slamTimer += dt;
        if (slamTimer >= SLAM_DURATION) enterVulnerable();
    }

    /** Begins the charge attack: a brief telegraph before dashing at the player. */
    private void enterCharge(float playerX) {
        state = State.CHARGE;
        chargePhase = ChargePhase.TELEGRAPH;
        chargeTimer = 0;
        chargeTargetX = playerX;
        setVelX(0);
    }

    /**
     * Drives the charge's three sub-phases: a wind-up telegraph, the fast
     * horizontal dash itself (stopped by a wall or a time limit), then a
     * short recovery before the boss becomes vulnerable.
     */
    private void updateCharge(float dt) {
        switch (chargePhase) {
            case TELEGRAPH -> {
                chargeTimer += dt;
                if (chargeTimer >= CHARGE_TELEGRAPH_DURATION) {
                    chargePhase = ChargePhase.DASHING;
                    chargeTimer = 0;
                    float dir = (chargeTargetX > getLeft()) ? 1 : -1;
                    patrolDir = (int) dir;
                    setVelX(CHARGE_SPEED * dir);
                }
            }
            case DASHING -> {
                chargeTimer += dt;
                setX(getLeft() + getVelX() * dt);
                boolean hitWall = getLeft() <= roomLeft || getLeft() >= roomRight - BOSS_W;
                boolean timeUp = chargeTimer >= CHARGE_DURATION;
                if (hitWall || timeUp) {
                    setX(Math.max(roomLeft, Math.min(getLeft(), roomRight - BOSS_W)));
                    setVelX(0);
                    chargePhase = ChargePhase.RECOVER;
                    chargeTimer = 0;
                    SoundManager.playSfx("bossDash");
                }
            }
            case RECOVER -> {
                chargeTimer += dt;
                if (chargeTimer >= CHARGE_RECOVER_DURATION) enterVulnerable();
            }
        }
    }

    /** Begins the barrage attack: a brief telegraph before firing a volley of bolts. */
    private void enterBarrage() {
        state = State.BARRAGE;
        barragePhase = BarragePhase.TELEGRAPH;
        barrageTimer = 0;
        boltsFired = 0;
        setVelX(0);
    }

    /**
     * Drives the barrage's three sub-phases: a wind-up telegraph, firing
     * {@code BOLT_COUNT} bolts spaced {@code BOLT_INTERVAL} apart, then a
     * short recovery before the boss becomes vulnerable.
     */
    private void updateBarrage(float dt) {
        switch (barragePhase) {
            case TELEGRAPH -> {
                barrageTimer += dt;
                if (barrageTimer >= BARRAGE_TELEGRAPH_DURATION) {
                    barragePhase = BarragePhase.FIRING;
                    boltFireCooldown = 0;
                }
            }
            case FIRING -> {
                boltFireCooldown -= dt;
                if (boltFireCooldown <= 0 && boltsFired < BOLT_COUNT) {
                    fireBolt();
                    boltsFired++;
                    boltFireCooldown = BOLT_INTERVAL;
                }
                if (boltsFired >= BOLT_COUNT) {
                    barragePhase = BarragePhase.RECOVER;
                    barrageTimer = 0;
                }
            }
            case RECOVER -> {
                barrageTimer += dt;
                if (barrageTimer >= BARRAGE_RECOVER_DURATION) enterVulnerable();
            }
        }
    }

    /** Spawns a single bolt from the boss's center, aimed toward the player's side. */
    private void fireBolt() {
        float originX = getLeft() + BOSS_W / 2f;
        float originY = getTop() + BOSS_H / 2f;
        float dir = (playerX > originX) ? 1 : -1;
        patrolDir = (int) dir;
        bolts.add(new BossBolt(originX, originY, BOLT_SPEED * dir));
        SoundManager.playSfx("fireBolt"); 
    }

    /** Advances all active bolts and culls any that have left the arena. */
    private void updateBolts(float dt) {
        for (BossBolt b : bolts) {
            if (!b.isActive()) continue;
            b.setX(b.getX() + b.getVx() * dt );
            if (b.getX() < roomLeft - BossBolt.SIZE || b.getX() > roomRight + BossBolt.SIZE) {
                b.setActive(false);
            }
        }
        bolts.removeIf(b -> !b.isActive());
    }

    private void enterVulnerable() {
        state = State.VULNERABLE;
        vulnerableTimer = 0;
        setVelX(0);
    }

    /** Counts down the vulnerable window before returning to patrol. */
    private void updateVulnerable(float dt) {
        vulnerableTimer += dt;
        if (vulnerableTimer >= VULN_DURATION) {
            state = State.PATROL;
            patrolTimer = 0;
        }
    }
    
    @Override
    public boolean takeDamage(int amount){
        if(state != State.VULNERABLE) return false;
        super.takeDamage(amount);

        SoundManager.playSfx("damageBoss");
        return true;
    }

    /**
     * Expands both beam slabs outward from the origin point. Beam speed
     * scales down as the slabs get farther from the origin, so the beam
     * starts fast and decelerates as it approaches the screen edges.
     */
    private void updateBeam(float dt) {
        float distRight = beamRightX - beamOriginX;
        float distLeft = beamOriginX - beamLeftX;
        float maxDist = Math.max(distRight, distLeft);

        float roomWidth = roomRight - roomLeft;
        float t = Math.min(maxDist / roomWidth, 1f);
        float speed = BEAM_SPEED_MAX + (BEAM_SPEED_MIN - BEAM_SPEED_MAX) * t;

        beamRightX = Math.min(beamRightX + speed * dt, roomRight);
        beamLeftX  = Math.max(beamLeftX - speed * dt, roomLeft);

        boolean rightGone = beamRightX - SLAB_WIDTH > roomRight;
        boolean leftGone = beamLeftX + SLAB_WIDTH < roomLeft;
        if (rightGone && leftGone) beamActive = false;
    }
    
    public void deactivateBeam(){
        this.beamActive = false;
    }
    
    @Override
    public void update(float deltaTime) {
        if (!fightStarted) return; // inert until tryStart() wakes it up

        updateVerticalPhysics(deltaTime);
        enforceScreenBounds();

        switch (state) {
            case PATROL -> updatePatrol(deltaTime, playerX);
            case LEAP -> updateLeap(deltaTime);
            case SLAM -> updateSlam(deltaTime);
            case CHARGE -> updateCharge(deltaTime);
            case BARRAGE -> updateBarrage(deltaTime);
            case VULNERABLE -> updateVulnerable(deltaTime);
        }

        if (beamActive) updateBeam(deltaTime);
        updateBolts(deltaTime);
        
    }

    @Override
    public void draw(Graphics2D g, Camera cam) {
        
        drawBoss(g, cam);
        
        if(beamActive)
            drawBeam(g, cam);

        drawBolts(g, cam);
    }
    
    //--------------- Draw Helpers ---------------
    
    /** 
     * Draws both beam slabs with a soft outer glow and a bright core + outline.
     */
    private void drawBeam(Graphics2D g, Camera cam) {
        int oy = (int)(beamOriginY - cam.offsetY);
        int sh = SLAB_HEIGHT;
        int sw = SLAB_WIDTH;

        // Right slab
        int rx = (int)(beamRightX - cam.offsetX);
        g.drawImage(shockBeamImage, rx + shockBeamImageWidth, oy, -shockBeamImageWidth, shockBeamImageHeight, null);

        // Left slab
        int lx = (int)(beamLeftX - cam.offsetX) - sw;
        g.drawImage(shockBeamImage, lx, oy, shockBeamImageWidth, shockBeamImageHeight, null);

        g.setStroke(new BasicStroke(1f));
    }

    /** Draws every active barrage bolt as a simple glowing orb. */
    private void drawBolts(Graphics2D g, Camera cam) {
        for (BossBolt b : bolts) {
            if (!b.isActive()) continue;
            int size = BossBolt.SIZE;
            int drawX = (int) (b.getX() - size / 2f - cam.offsetX);
            int drawY = (int) (b.getY() - size / 2f - cam.offsetY);
            if (patrolDir == 1)
                g.drawImage(boltGif, drawX, drawY, size, size, null);
            else if (patrolDir == -1)
                g.drawImage(boltGif, drawX + size, drawY, -size, size, null);
        }
    }
    
    
    private void drawHealthBar(Graphics2D g, float drawX, float drawY) {
        int barW = 100, barH = 8;
        int barX = (int)(drawX + BOSS_W / 2f - barW / 2f);
        int barY = (int)(drawY - 20);

        // Track
        g.setColor(new Color(30, 25, 40));
        g.fillRoundRect(barX, barY, barW, barH, barH, barH);

        // Fill — purple when vulnerable, red otherwise
        float hpRatio  = (float) getHealth() / getMaxHealth();
        Color fillColor = (state == State.VULNERABLE) ? new Color(100, 60,  220) : new Color(200, 40,  40);
        g.setColor(fillColor);
        g.fillRoundRect(barX, barY, (int)(barW * hpRatio), barH, barH, barH);

        // Outline
        g.setColor(new Color(80, 70, 100));
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(barX, barY, barW, barH, barH, barH);
        g.setStroke(new BasicStroke(1f));
    }
}