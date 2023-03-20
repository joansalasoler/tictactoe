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

import java.util.StringJoiner;
import com.joansala.engine.base.BaseBoard;
import com.joansala.util.bits.BitsetConverter;
import com.joansala.util.notation.CoordinateConverter;
import com.joansala.util.notation.DiagramConverter;
import static com.joansala.game.tictactoe.TicTacToe.*;
import static com.joansala.game.tictactoe.TicTacToeGame.*;


/**
 * Encapsulates the state of a tic-tac-toe match.
 *
 * A {@link Board} is an immutable object that stores all the data required
 * to represent a concrete state of a game and provides methods to convert
 * between different notations, such as from bitboards to a Forsyth–Edwards
 * Notation (FEN) string, or from a sequence of move identifiers to their
 * equivalent algebraic notation.
 *
 * To represent a game state, the position on the board, the player to move,
 * and any other feature that may affect the game play and/or its serialization
 * must be stored. Notice that for some games, such as Checkers, moves may be
 * encoded differently depending of the current state of the game.
 *
 * This implementation for tic-tac-toe stores the current board state as a
 * bitboard array, the player to move as an integer value, and each move as a
 * cell number from the board. See {@link TicTacToe} for the definitions.
 *
 * This {@link TicTacToeBoard} class uses the conventions defined in the
 * Universal Chess Interface to encode the board state (position) and the
 * sequences of moves as strings. That is, a board diagram is represented as
 * a FEN string, and a sequence of moves is encoded in algebraic notation.
 *
 * @see TicTacToe
 * @see UCIService
 */
public class TicTacToeBoard extends BaseBoard<long[]> {

    /** Conversion from bitboards to bidimensional arrays */
    private static BitsetConverter bitset;

    /** Conversion from numeric arrays to algebraic coordinates */
    private static CoordinateConverter algebraic;

    /** Conversion from bidimensional arrays to string */
    private static DiagramConverter fen;


    static {
        bitset = new BitsetConverter(BITS);
        algebraic = new CoordinateConverter(COORDINATES);
        fen = new DiagramConverter(PIECES);
    }


    /**
     * Creates a new board that represents the default initial game
     * state of tic-tac-toe where no pieces are placed on the board.
     */
    public TicTacToeBoard() {
        this(START_POSITION, SOUTH);
    }


    /**
     * Creates a new board instance for a given game state.
     *
     * @param position      Bitboards array
     * @param turn          Player to move
     */
    public TicTacToeBoard(long[] position, int turn) {
        super(position.clone(), turn);
    }


    /**
     * Returns a clone of this board's position.
     *
     * @return              Bitboards array
     */
    @Override
    public long[] position() {
        return position.clone();
    }


    /**
     * Converts an algebraic move notation to a cell index.
     *
     * @param notation      Move notation
     * @return              Move identifier
     */
    @Override
    public int toMove(String notation) {
        return algebraic.toIndex(notation);
    }


    /**
     * Converts a cell index to an algebraic move notation.
     *
     * @param move          Move identifier
     * @return              Move notation
     */
    @Override
    public String toCoordinates(int move) {
        return algebraic.toCoordinate(move);
    }


    /**
     * Converts a FEN string to a new {@code Board} instance.
     *
     * @param notation      Game state notation (FEN)
     * @return              A new {@code TicTacToeBoard}
     */
    @Override
    public TicTacToeBoard toBoard(String notation) {
        String[] fields = notation.split(" ");

        long[] position = toPosition(fen.toArray(fields[0]));
        int turn = toTurn(fields[1].charAt(0));

        return new TicTacToeBoard(position, turn);
    }


    /**
     * Encodes this {@code Board} instance as a FEN string.
     *
     * @return              Game state notation (FEN)
     */
    @Override
    public String toDiagram() {
        StringJoiner notation = new StringJoiner(" ");

        notation.add(fen.toDiagram(toOccupants(position)));
        notation.add(String.valueOf(toPlayerSymbol(turn)));

        return notation.toString();
    }


    /**
     * Bitboard array to a bidimensional array of piece symbols.
     */
    private Object[] toPieceSymbols(long[] position) {
        return fen.toSymbols(toOccupants(position));
    }


    /**
     * Bitboard array to a bidimensional array of piece identifiers.
     */
    private int[][] toOccupants(long[] position) {
        int[][] occupants = new int[BOARD_RANKS][BOARD_FILES];
        return bitset.toOccupants(occupants, position);
    }


    /**
     * Bidimensional array of piece identifiers to bitboards.
     */
    private long[] toPosition(int[][] occupants) {
        long[] position = new long[PIECE_COUNT];
        return bitset.toPosition(position, occupants);
    }


    /**
     * Converts a turn identifier to a player symbol.
     */
    private static char toPlayerSymbol(int turn) {
        return turn == SOUTH ? SOUTH_SYMBOL : NORTH_SYMBOL;
    }


    /**
     * Converts a turn identifier to a player name.
     */
    private static String toPlayerName(int turn) {
        return turn == SOUTH ? SOUTH_NAME : NORTH_NAME;
    }


    /**
     * Converts a player symbol to a turn identifier.
     */
    private static int toTurn(char symbol) {
        return symbol == SOUTH_SYMBOL ? SOUTH : NORTH;
    }


    /**
     * Converts the game state encapsulated by this board to a string
     * suitable to be shown on the command line interface.
     */
    @Override
    public String toString() {
        return String.format((
            "===( %turn to move )===%n" +
            "      +---+---+---+%n" +
            "    3 | # | # | # |%n" +
            "      +---+---+---+%n" +
            "    2 | # | # | # |%n" +
            "      +---+---+---+%n" +
            "    1 | # | # | # |%n" +
            "      +---+---+---+%n" +
            "        a   b   c%n" +
            "=========================").
            replaceAll("(#)", "%1s").
            replace("%turn", toPlayerName(turn)),
            toPieceSymbols(position)
        );
    }
}
