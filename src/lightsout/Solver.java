package lightsout;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static lightsout.LightsOutGame.N_TILES;
import static lightsout.LightsOutGame.PROBLEM_STEPS;

/**
 * @author Max Morris
 */
class Solver {
    private static final int MAX_DEPTH;
    private static final int GOOD_ENOUGH;
    private static final long MAX_STEPS;
    private static final ExecutorService pool = Executors.newSingleThreadExecutor();

    static {
        if (N_TILES == 4) {
            MAX_DEPTH = PROBLEM_STEPS;
            GOOD_ENOUGH = 5;
            MAX_STEPS = 75_000_000;
        } else if (N_TILES == 5) {
            MAX_DEPTH = PROBLEM_STEPS;
            GOOD_ENOUGH = 12;
            MAX_STEPS = -1;
        } else if (N_TILES == 6) {
            MAX_DEPTH = PROBLEM_STEPS;
            GOOD_ENOUGH = 15;
            MAX_STEPS = -1;
        } else {
            MAX_DEPTH = 8;
            GOOD_ENOUGH = 4;
            MAX_STEPS = 50_000_000;
        }
    }

    private final TreeMap<Integer, Node> winningNodes = new TreeMap<>();
    private long steps;

    // So we don't block the AWT thread...
    static CompletableFuture<Deque<Integer>> solveAsync(Tile[][] board) {
        CompletableFuture<Deque<Integer>> fut = new CompletableFuture<>();
        pool.submit(() -> fut.complete(solve(board)));
        return fut;
    }

    static Deque<Integer> solve(Tile[][] board) {
        Solver solver = new Solver();

        boolean[] initialBoard = new boolean[N_TILES*N_TILES];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                initialBoard[i * N_TILES + j] = board[i][j].lit;
            }
        }

        solver.walkSolutions(initialBoard);

        if (solver.winningNodes.isEmpty()) {
            return new ArrayDeque<>();
        }

        Node winningNode = solver.winningNodes.firstEntry().getValue();

        Deque<Integer> moves = new ArrayDeque<>();
        for (Node n = winningNode; n.move != -1; n = n.parent) {
            moves.push(n.move);
        }

        return moves;
    }

    private void walkSolutions(boolean[] board) {
        Node root = new Node(board);
        walkSolutions(root, 0);
    }

    private void walkSolutions(Node parent, int depth) {
        if (depth >= MAX_DEPTH || (MAX_STEPS > 0 && steps >= MAX_STEPS)) {
            //System.out.printf("Depth: %d, Steps: %d\n", depth, steps);
            return;
        }

        steps++;

        for (int move = 0; move < N_TILES*N_TILES; move++) {
            if (move == parent.move) {
                continue;
            }

            boolean[] newBoard = new boolean[N_TILES*N_TILES];
            System.arraycopy(parent.board, 0, newBoard, 0, N_TILES*N_TILES);
            toggleWithNeighbors(newBoard, move);

            Node child = new Node(newBoard, parent, move);

            if (checkWin(newBoard)) {
                winningNodes.putIfAbsent(depth, child);
                if (depth <= GOOD_ENOUGH) {
                    return;
                }
            } else {
                walkSolutions(child, depth + 1);
            }
        }
    }

    private static void toggleWithNeighbors(boolean[] board, int move) {
        for (int m : new int[] {move, move - N_TILES, move + N_TILES}) {
            if (m >= 0 && m < N_TILES*N_TILES) {
                board[m] = !board[m];
            }
        }

        if (move % N_TILES != 0) {
            board[move - 1] = !board[move - 1];
        }

        if (move != N_TILES*N_TILES - 1 && (move + 1) % N_TILES != 0) {
            board[move + 1] = !board[move + 1];
        }
    }

    private static boolean checkWin(boolean[] board) {
        for (boolean b : board) {
            if (b) {
                return false;
            }
        }
        return true;
    }

    private static class Node {
        boolean[] board;
        int move;
        Node parent;

        Node(boolean[] board, Node parent, int move) {
            this.board = board;
            this.parent = parent;
            this.move = move;
        }

        Node(boolean[] board) {
            this.board = board;
            this.move = -1;
        }
    }
}


