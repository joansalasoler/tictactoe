package com.joansala.game.tictactoe;

/*
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

import java.io.IOException;
import java.util.Random;
import com.joansala.engine.Game;
import com.joansala.engine.Roots;
import static com.joansala.engine.Game.*;


/**
 * An opening book that picks the first move randomly.
 */
public class TicTacToeRoots implements Roots<Game> {

    /** Random number generator */
    private final Random random = new Random();


    /**
     * {@inheritDoc}
     */
    @Override
    public void newMatch() {}


    /**
     * {@inheritDoc}
     */
    @Override
    public int pickBestMove(Game game) throws IOException {
        return (game.length() == 0) ? pickRandomMove(game) : NULL_MOVE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int pickPonderMove(Game game) throws IOException {
        return pickRandomMove(game);
    }


    /**
     * Picks a move at random from the list of legal moves.
     *
     * @param game      Game strate
     * @return          Move identifier
     */
    private int pickRandomMove(Game game) {
        int[] moves = game.legalMoves();

        if (moves.length > 0) {
            int index = random.nextInt(moves.length);
            return moves[index];
        }

        return NULL_MOVE;
    }
}
