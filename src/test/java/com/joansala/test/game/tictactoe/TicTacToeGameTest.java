package com.joansala.test.game.tictactoe;

import org.junit.jupiter.api.*;
import com.joansala.engine.Game;
import com.joansala.test.engine.GameContract;
import com.joansala.game.tictactoe.TicTacToeGame;


@DisplayName("TicTacToe game")
public class TicTacToeGameTest implements GameContract {

    /**
     * {@inheritDoc}
     */
    @Override
    public Game newInstance() {
        return new TicTacToeGame();
    }
}
