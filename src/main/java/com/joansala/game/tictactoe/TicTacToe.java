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

/**
 * This class provides a collection of constants, bit masks, and other
 * definitions that are used for implementing the game logic and board
 * representation of the tic-tac-toe game.
 *
 * The game state is represented by two bitboards, one for each player,
 * where each bit set represents a piece placed on the board by the
 * respective player. Bitboards are a convenient way to represent the state
 * of the game and make it easy to check if someone has won.
 *
 * The 3x3 board is represented as a grid of 9 cells where each cell
 * corresponds to a bit on the bitboards as follows:
 *
 * +---+---+---+
 * | 6 | 7 | 8 |
 * +---+---+---+
 * | 3 | 4 | 5 |
 * +---+---+---+
 * | 0 | 1 | 2 |
 * +---+---+---+
 */
final class TicTacToe {

    // -------------------------------------------------------------------
    // Size and distribution of the board
    // -------------------------------------------------------------------

    /** Total number of cells on the board */
    static final int BOARD_SIZE = 9;

    /** Number of rows on the board */
    static final int BOARD_RANKS = 3;

    /** Number of columns on the board */
    static final int BOARD_FILES = 3;

    /** Total number of distinct player symbols */
    static final int PIECE_COUNT = 2;

    // -------------------------------------------------------------------
    // How the bitboard array is represented
    // -------------------------------------------------------------------

    /** Bitboard of the player who moves first */
    static final int SOUTH_PIECE =  0;

    /** Bitboard of the player who moves second */
    static final int NORTH_PIECE =  1;

    // -------------------------------------------------------------------
    // How the players are shown to the user
    // -------------------------------------------------------------------

    /** Name of the player who moves first */
    static final String SOUTH_NAME = "Crosses";

    /** Name of the player who moves second */
    static final String NORTH_NAME = "Noughts";

    /** Symbol used by the player who moves first */
    static final char SOUTH_SYMBOL = 'X';

    /** Symbol used by the player who moves second */
    static final char NORTH_SYMBOL = 'O';

    // -------------------------------------------------------------------
    // How the board is shown to the user
    // -------------------------------------------------------------------

    /** Indexed game piece symbols */
    static final char[] PIECES = {
        SOUTH_SYMBOL,
        NORTH_SYMBOL
    };

    /** Bit indices of the checkers */
    static final int[] BITS = {
        0,  1,  2,
        3,  4,  5,
        6,  7,  8
    };

    /** Indexed board cell names */
    static final String[] COORDINATES = {
        "a1", "b1", "c1",
        "a2", "b2", "c2",
        "a3", "b3", "c3"
    };

    // -------------------------------------------------------------------
    // Utility masks to operate with the bitboards
    // -------------------------------------------------------------------

    /** Bitboard representing an empty board */
    static final long EMPTY_MASK = 0b000000000L;

    /** Bits where pieces are allowed to be placed */
    static final long BOARD_MASK = 0b111111111L;

    /** Bits for each possible winning combination */
    static final long[] WINNING_MASKS = {
        0b111000000L, // Bottom row
        0b000111000L, // Middle row
        0b000000111L, // Upper row
        0b100100100L, // Right column
        0b010010010L, // Middle column
        0b001001001L, // Left column
        0b100010001L, // Bottom-left diagonal
        0b001010100L  // Bottom-right diagonal
    };

    // -------------------------------------------------------------------
    // Bitboards of the initial state of the game
    // -------------------------------------------------------------------

    /** Start position bitboards */
    static final long[] START_POSITION = {
        EMPTY_MASK, // Crosses
        EMPTY_MASK, // Noughts
    };
}
