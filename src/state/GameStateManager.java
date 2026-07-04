/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package state;

import core.InputHandler;
import java.awt.Graphics2D;

/**
 * Owns the active GameState and drives all transitions.
 *
 * During a room transition the playing state is drawn behind the
 * loading screen so the fade-in has something to cover.
 *
 * @author EDEN COMPUTERS
 */
public class GameStateManager {

    //------------------- Core ----------------------------
    private final InputHandler input;
    private final int screenW, screenH;

    //------------------- States --------------------------
    private GameState currentState;
    private PlayingState playingState;

    /**
     * Creates the manager and immediately enters the start screen.
     *
     * @param input the shared input handler
     * @param screenW screen width in pixels
     * @param screenH screen height in pixels
     */
    public GameStateManager(InputHandler input, int screenW, int screenH) {
        this.input = input;
        this.screenW = screenW;
        this.screenH = screenH;

        currentState = new StartScreen(input, screenW, screenH);
    }

    //---------------------- Update --------------------------

    /**
     * Advances the active state one tick, then checks whether a
     * transition should fire and switches states accordingly.
     *
     * @param dt delta time in seconds
     */
    public void update(float dt) {
        currentState.update(dt);

        checkStartToPlaying();
        checkPlayingTransitions();
        checkLoadingComplete();
    }

    //---------------- Transition Checks ---------------------

    /**
     * Switches from the start screen to a fresh playing state
     * once the player presses a start key.
     */
    private void checkStartToPlaying() {
        if (!(currentState instanceof StartScreen ss)) return;
        if (!ss.isReady()) return;

        playingState = new PlayingState(input, screenW, screenH);
        currentState = playingState;
    }

    /**
     * While in the playing state, checks for two exit conditions:
     *
     * Boss defeated - switch to win screen
     * Room transition pending - switch to loading screen
     *
     */
    private void checkPlayingTransitions() {
        if (!(currentState instanceof PlayingState ps)) return;

        if (ps.isBossDefeated()) {
            currentState = new WinScreen(input, screenW, screenH);
            return;
        }

        if (ps.isPendingTransition()) {
            currentState = new LoadingScreen(screenW, screenH, ps.getNextRoomId());
        }
    }

    /**
     * Once the loading screen finishes, resumes the playing state
     */
    private void checkLoadingComplete() {
        if (!(currentState instanceof LoadingScreen ls)) return;
        if (!ls.isDone()) return;

        playingState.transitionToRoom(ls.getTargetRoomId());
        currentState = playingState;
    }

    //------------------------ Draw --------------------------

    /**
     * Draws the active state. During a loading screen transition,
     * the playing state is drawn first as a background layer so
     * the loading fade-in has something to cover.
     *
     * @param g drawing object
     */
    public void draw(Graphics2D g) {
        if (currentState instanceof LoadingScreen && playingState != null)
            playingState.draw(g);

        currentState.draw(g);
    }
}