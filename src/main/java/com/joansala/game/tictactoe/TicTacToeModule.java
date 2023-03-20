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


import picocli.CommandLine.Command;

import com.joansala.cli.*;
import com.joansala.engine.*;
import com.joansala.engine.base.BaseModule;
import com.joansala.engine.mcts.Montecarlo;


/**
 * Binds together the components of the tic-tac-toe engine.
 */
public class TicTacToeModule extends BaseModule {

    @Command(
      name = "tictactoe",
      version = "1.0.0",
      description = "Tic-tac-toe is a strategy board game"
    )
    private static class TicTacToeCommand extends MainCommand {}


    /**
     * Configures the components of this game module.
     */
    @Override protected void configure() {
        bind(Game.class).to(TicTacToeGame.class);
        bind(Board.class).to(TicTacToeBoard.class);
        bind(Roots.class).to(TicTacToeRoots.class);
        bind(Engine.class).to(Montecarlo.class);
    }


    /**
     * Executes the command line interface.
     *
     * @param args      Command line parameters
     */
    public static void main(String[] args) throws Exception {
        BaseModule module = new TicTacToeModule();
        TicTacToeCommand main = new TicTacToeCommand();
        System.exit(main.execute(module, args));
    }
}
