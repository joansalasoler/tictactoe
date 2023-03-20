package com.joansala.game.tictactoe;

/*
 * Samurai framework.
 * Copyright (c) 2023 Joan Sala Soler <contact@joansala.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.joansala.engine.Board;
import com.joansala.engine.base.BaseGame;
import com.joansala.uci.UCIService;

import static com.joansala.util.bits.Bits.*;
import static com.joansala.game.tictactoe.TicTacToe.*;

/**
 * Represents a tic-tac-toe game between two players.
 *
 * This class implements the abstract methods of {@link BaseGame}, which
 * provides some common functionality for most games, and provides their
 * implementation specific to the game of tic-tac-toe. This includes support
 * for decision-making algorithms such as minimax or MCTS.
 *
 * The game is played on a 3x3 board, where each player takes turns to place
 * their pieces on an empty cell. The player who succeeds in placing three of
 * their marks in a horizontal, vertical, or diagonal row wins the game. If
 * all cells are filled without a player winning, the game ends in a draw.
 *
 * This class can be seen as a sequence of game states. The initial state of
 * the game is set by providing a {@link TicTacToeBoard} object, and from
 * there, moves can be made and undone, updating the internal state of this
 * {@link Game} object and archiving previous states. This allows for a record
 * of the match to be kept while it provides efficient access to the current
 * state of the match being disputed.
 *
 * Methods are provided for making and undoing moves, generating legal moves
 * for the current state, and evaluating the state of the game.
 *
 * In addition, this class also keeps track of the moves generated for each
 * state, allowing for quick generation of the next moves without having
 * to regenerate all legal moves each time a move is made or unmade. This
 * is particularly useful when using decision-making algorithms such as
 * {@link Negamax}, as they require access to the list of legal moves for a
 * given state, and generating them repeatedly can be a costly operation.
 *
 * A cursor is stored for each state to track the generated moves, which
 * points to the next move to generate for the state. For tic-tac-toe, the
 * cursor is a bitboard of moves yet to be generated.
 *
 * @see TicTacToeBoard
 * @see BaseGame
 */
public class TicTacToeGame extends BaseGame {

    /** Stores the initial state of this game */
    private TicTacToeBoard board;

    /** Move generation cursors for each state */
    private int[] cursors;

    /** Bitboards of the current game state */
    private long[] state;

    /** Bitboard of legal moves on the current state */
    private long mobility;

    /** Player to move on the current state */
    private int player;

    /** Opponent player on the current state */
    private int rival;

    /** Current move generation cursor */
    private int cursor;


    /**
     * Creates a new game for which its initial state is the default
     * start position and turn of tic-tac-toe. The game can store up
     * to {@code BOARD_SIZE} states, which is the maximum number of
     * moves that can be performed, plus the initial state.
     */
    public TicTacToeGame() {
        super(BOARD_SIZE);
        cursors = new int[BOARD_SIZE];
        setBoard(new TicTacToeBoard());
    }


    /**
     * Returns the initial state of the game being played.
     *
     * @return      An immutable board containing the state
     */
    @Override
    public Board getBoard() {
        return board;
    }


    /**
     * Sets the initial state of the game being played.
     *
     * @param board     Immutable representation of the state
     */
    @Override
    public void setBoard(Board board) {
        setBoard((TicTacToeBoard) board);
    }


    /**
     * Sets the initial state of the game being played. Notice that by
     * changing the start board all the previously recorded game history
     * and move generation state is also cleared.
     *
     * @param board     Immutable representation of the state
     */
    public void setBoard(TicTacToeBoard board) {
        this.index = -1;
        this.board = board;
        this.state = board.position();
        setTurn(board.turn());
        setMove(NULL_MOVE);
        resetCursor();
        updateState();
    }


    /**
     * Returns the current move generation cursor.
     *
     * @return          A move generation cursor
     */
    @Override
    public int getCursor() {
        return cursor;
    }


    /**
     * Sets the current move generation cursor.
     *
     * @return          A new move generation cursor
     */
    @Override
    public void setCursor(int cursor) {
        this.cursor = cursor;
    }


    /**
     * Resets the move generation cursor to the beginning of the list
     * of legal moves. That is, the bitboard of all the legal moves.
     */
    @Override
    public void resetCursor() {
        setCursor((int) mobility);
    }


    /**
     * Store the move that lead to the current game state.
     *
     * @param move      Move performed to reach the current state
     */
    protected void setMove(int move) {
        this.move = move;
    }


    /**
     * Changes the player to move on the current state. The turn is
     * represented as a signed integer, with the value {@code SOUTH} for
     * the starting player and {@code NORTH} for the opponent.
     *
     * @param turn      Either {@code SOUTH} or {@code NORTH}
     */
    protected void setTurn(int turn) {
        this.turn = turn;

        if (turn == SOUTH) {
            player = SOUTH_PIECE;
            rival = NORTH_PIECE;
        } else {
            player = NORTH_PIECE;
            rival = SOUTH_PIECE;
        }
    }


    /**
     * Current state of the game as an immutable board instance.
     *
     * @return          A new {@link Board} instance
     */
    @Override
    public TicTacToeBoard toBoard() {
        return new TicTacToeBoard(state, turn);
    }


    /**
     * Checks if the game finished on the current state because no
     * more moves are possible or a player has won.
     *
     * @return          {@code true} if the match is finished
     */
    @Override
    public boolean hasEnded() {
        return empty(mobility);
    }


    /**
     * Determines if a move is legal on the current state.
     *
     * @param move      The move to be checked
     * @return          {@code true} if the move is legal
     */
    @Override
    public boolean isLegal(int move) {
        return contains(mobility, bit(move));
    }


    /**
     * Converts an heuristic or utility score to centi-pawns. This method
     * provides a convenient way to convert between different score units.
     * It is used by the UCI protocol implementation.
     *
     * @param score     Evaluation score
     * @return          Score in centi-pawns
     * @see UCIService
     */
    @Override
    public int toCentiPawns(int score) {
        return (int) (2.5 * score);
    }


    /**
     * Returns the default contempt score for tic-tac-toe.
     *
     * A positive contempt means that the algorithm will try to avoid draws
     * even if it means playing a weaker move, while a negative contempt
     * means that the algorithm will be more willing to accept a draw.
     *
     * @return          A value between {@code -MAX_SCORE} and
     *                  {@code MAX_SCORE}
     */
    @Override
    public int contempt() {
        return DRAW_SCORE;
    }


    /**
     * Computes an heuristic evaluation score for the current state.
     *
     * An heuristic evaluation is a score assigned to the current state of
     * the game. It is used to guide a decision-making algorithm, such as
     * {@link Negamax} or {@link UCT}, to make the best possible move from
     * the current game state.
     *
     * The score should always be higher than {@code -MAX_SCORE}, lower than
     * {@code +MAX_SCORE} and, preferably, distinct from {@code DRAW_SCORE}.
     * It should reflect how likely is the {@code SOUTH} player to win or
     * loss the game given the current state of the match.
     *
     * In this particular implementation, the method always returns a fixed
     * {@code DRAW_SCORE}, which means that we predict that from the current
     * game state the match is likely going to end in a draw.
     *
     * @return          Heuristic score of the current state
     * @see #outcome()
     */
    @Override
    public int score() {
        return DRAW_SCORE;
    }


    /**
     * Computes a utility evaluation score for the current state.
     *
     * This method calculates a numerical value to reflect how favorable the
     * current game state is for a particular player. Unlike heuristic
     * evaluation, which estimates a state's value based on heuristics or
     * rules of thumb, utility evaluation always evaluates the current state
     * as if the game is finished.
     *
     * The computed score will be used by a decision-making algorithm, such
     * as {@link Negamax}, {@link UCT}, or {@link Montecarlo}, to determine
     * the best move. It must return {@code +MAX_SCORE} if the {@code SOUTH}
     * player has won the game, {@code -MAX_SCORE} if {@code NORTH} has won,
     * and {@code DRAW_SCORE} if the match ended in a draw or is still ongoing.
     *
     * @return          Utility score for the current game state
     * @see #score
     */
    @Override
    public int outcome() {
        if (hasWon(NORTH_PIECE)) return -MAX_SCORE;
        if (hasWon(SOUTH_PIECE)) return MAX_SCORE;
        return DRAW_SCORE;
    }


    /**
     * Returns the next legal move on the current state of the game.
     *
     * This method generates the next legal move on the current state of
     * the game. It uses a move generation cursor to efficiently generate
     * moves one by one. The cursor is updated each time a move is generated
     * so that the next call to this method returns the next legal move.
     *
     * @return The next legal move on the current state of the game, or 
     *         {@link #NULL_MOVE} if there are no more legal moves.
     */
    @Override
    public int nextMove() {
        if (count(cursor) > 0) {
            final int move = first(cursor);
            cursor ^= (1 << move);
            return move;
        }

        return NULL_MOVE;
    }


    /**
     * Appends a move to this game and updates its state.
     *
     * Adds a move to this game's history, updates the board state by
     * applying the move made to the current state, and sets the updated
     * state as the current state of the match. This method is used by a
     * decision-making algorithm to explore different possible moves and
     * evaluate their potential outcomes.
     *
     * @param move      A legal move identifier
     * @see #unmakeMove()
     */
    @Override
    public void makeMove(int move) {
        index++;
        moves[index] = this.move;
        cursors[index] = this.cursor;
        state[player] ^= bit(move);
        setTurn(-turn);
        setMove(move);
        resetCursor();
        updateState();
    }


    /**
     * Undoes a move that was previously made with {@link #makeMove()}.
     *
     * This method restores the match state to the state before the last
     * move was made. The move generation cursor is restored to the previous
     * position, but not reset, so that it can continue generating moves
     * from where it left off. This is done so that a decision-making
     * algorithm can continue to explore other possible moves.
     *
     * @see #makeMove()
     */
    @Override
    public void unmakeMove() {
        state[rival] ^= bit(move);
        setTurn(-turn);
        setMove(moves[index]);
        setCursor(cursors[index]);
        updateState();
        index--;
    }


    /**
     * Determines if the given player has won the game.
     *
     * This method checks if the given player has won the game on the
     * current state by checking if their game pieces occupy any of the
     * winning positions on the board.
     *
     * @param player        Identifier of a player to check for a win
     * @return              {@code true} if the player has won the game
     */
    protected boolean hasWon(int player) {
        final long pieces = state[player];

        for (long mask : WINNING_MASKS) {
            if ((pieces & mask) == mask) {
                return true;
            }
        }

        return false;
    }


    /**
     * Computes the legal moves of the current game state.
     *
     * This method computes a bitboard with a bit set for each cell of
     * the board where the current player to move is allowed to place one
     * of their pieces. Notice that if the match is already finished it
     * returns an empty bitboard.
     *
     * @return              A bitboard representing the empty squares on
     *                      the board if the match is ongoing; otherwise
     *                      a bitboard with no bits set
     */
    protected long computeMobility() {
        long mobility = EMPTY_MASK;

        if (outcome() == DRAW_SCORE) {
            final long south = state[SOUTH_PIECE];
            final long north = state[NORTH_PIECE];
            mobility = BOARD_MASK & ~(south | north);
        }

        return mobility;
    }


    /**
     * Computes a hash code for the current game state.
     *
     * The hash code is generated from the current game state and serves
     * as a unique or near-unique identifier for that state. It is used to
     * efficiently look up previously computed evaluations of the same game
     * state and avoid having to recompute them.
     *
     * This method computes the hash value for the current game state by
     * combining the bitboards for player's pieces into a single long value.
     * The south pieces are shifted to occupy the higher-order bits.
     *
     * @return              A value that uniquely identifies the game state
     */
    @Override
    protected long computeHash() {
        return (
            state[SOUTH_PIECE] << BOARD_SIZE |
            state[NORTH_PIECE]
        );
    }


    /**
     * Updates the state variables for the current game state by
     * recomputing the mobility and hash values.
     */
    protected void updateState() {
        mobility = computeMobility();
        hash = computeHash();
    }


    /**
     * Increases the capacity of this game, if necessary, to ensure that
     * it can hold at least the number of states specified. This method is
     * a noop for this tic-tac-toe implementation because the maximum number
     * of states that a match can contain is small and fixed.
     *
     * @param size          New size as a number of moves
     */
    @Override
    public void ensureCapacity(int size) {}
}
