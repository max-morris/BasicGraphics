package lightsout;

import basicgraphics.BasicFrame;
import basicgraphics.Clock;
import basicgraphics.SpriteComponent;
import basicgraphics.Task;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * @author Max Morris
 */
class LightsOutGame {
    public static final int TILE_SIZE = 75;
    public static final int N_TILES = 3;
    public static final int PROBLEM_STEPS = 10;
    public static final Dimension WINDOW_SIZE = new Dimension(TILE_SIZE * N_TILES,TILE_SIZE * N_TILES);

    private SpriteComponent sc;
    private final BasicFrame frame = new BasicFrame("Lights Out");

    Tile[][] tiles;
    int litTiles = 0;

    public void run() {
        sc = new SpriteComponent();
        sc.setPreferredSize(WINDOW_SIZE);
        frame.createBasicLayout(sc);

        tiles = new Tile[N_TILES][N_TILES];
        for (int i = 0; i < N_TILES; i++) {
            tiles[i] = new Tile[N_TILES];
            for (int j = 0; j < N_TILES; j++) {
                tiles[i][j] = new Tile(sc, this, j, i, false);
            }
        }

        generateProblem();

        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                final int keycode = e.getKeyCode();

                if (keycode == KeyEvent.VK_S || keycode == KeyEvent.VK_H) {
                    Solver.solveAsync(tiles).thenAccept(moves -> {
                        if (moves.isEmpty()) {
                            JOptionPane.showMessageDialog(sc, "Couldn't find a solution fast enough.");
                            return;
                        }

                        if (keycode == KeyEvent.VK_S) {
                            Clock.addTask(new Task() {
                                @Override
                                public void run() {
                                    if (moves.isEmpty()) {
                                        this.setFinished();
                                        return;
                                    }

                                    tiles[moves.peek() / N_TILES][moves.peek() % N_TILES].setHint(iteration() % 400 < 200);

                                    if (iteration() % 800 == 0 && iteration() > 0){
                                        int move = moves.pop();
                                        tiles[move / N_TILES][move % N_TILES].toggleWithNeighbors(true);
                                    }
                                }
                            });
                        } else {
                            Clock.addTask(new Task(1000) {
                                @Override
                                public void run() {
                                    assert !moves.isEmpty();
                                    tiles[moves.peek() / N_TILES][moves.peek() % N_TILES].setHint(iteration() % 400 < 200);
                                }
                            });
                        }
                    });
                }
            }
        });

        frame.show();
        Clock.start(1);
        Clock.addTask(sc.moveSprites());
    }

    // Generating the board with a sequence of toggleWithNeighbors guarantees the board is solvable
    public void generateProblem() {
        var rand = new Random();
        for (int i = 0; i < PROBLEM_STEPS; i++) {
            var tile = tiles[rand.nextInt(N_TILES)][rand.nextInt(N_TILES)];
            tile.toggleWithNeighbors(false);
        }
    }

    public void checkWin() {
        if (litTiles == 0) {
            JOptionPane.showMessageDialog(sc, "You're winner!");
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        var game = new LightsOutGame();
        game.run();
    }
}
