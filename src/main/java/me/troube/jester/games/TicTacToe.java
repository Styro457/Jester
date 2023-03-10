package me.troube.jester.games;

import me.troube.jester.games.objects.TwoPlayerGame;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.*;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.HashMap;
import java.util.Map;

public class TicTacToe extends ListenerAdapter {

    private final Map<Long, TicTacToeGame> games = new HashMap<>();

    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("tto")) {
            event.reply("**Starting the game...**").queue((interaction -> interaction.retrieveOriginal().queue((message -> {
                TicTacToeGame game = new TicTacToeGame(
                        message.getIdLong(),
                        event.getMember());

                message.editMessage(game.getGameStateMessage()).setComponents(game.buildBoard()).queue();
                games.put(message.getIdLong(), game);
            }))));
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("TTO-")) {
            TicTacToeGame game = games.get(event.getMessage().getIdLong());
            if(game == null) {
                event.deferEdit().queue();
                return;
            }
            String[] buttonData = event.getButton().getId().split("-");
            int row = Integer.parseInt(buttonData[2]);
            int column = Integer.parseInt(buttonData[3]);

            if(game.board[row][column] != PlayerSymbol.NONE || game.winner != null || (game.turn.equals(PlayerSymbol.X) && !event.getMember().equals(game.player1)))
            {
                event.deferEdit().queue();
                return;
            }

            if(game.turn.equals(PlayerSymbol.O)) {
                if(game.player2 == null)
                    game.player2 = event.getMember();
                else if(!event.getMember().equals(game.player2)){
                    event.deferEdit().queue();
                    return;
                }
            }

            game.click(row, column, event);
        }
    }

    private static class TicTacToeGame extends TwoPlayerGame {

        public int scorePlayer1;
        public int scorePlayer2;

        private int movesLeft = 9;

        public PlayerSymbol turn = PlayerSymbol.X;
        public PlayerSymbol winner;
        public boolean[][] winningPositions;
        public PlayerSymbol[][] board = new PlayerSymbol[3][3];

        public TicTacToeGame(Long id, Member player1) {
            this.id = id;
            this.player1 = player1;

            for(int i = 0; i < 3; i++) {
                for(int j = 0; j < 3; j++) {
                    this.board[i][j] = PlayerSymbol.NONE;
                }
            }

        }

        public ActionRow[] buildBoard() {
            ItemComponent[] components = new ItemComponent[3];
            ActionRow[] actionRows = new ActionRow[3];
            PlayerSymbol squareStatus;
            String buttonId;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    squareStatus = this.board[i][j];
                    buttonId = "TTO-" + this.id + "-" + i + "-" + j;
                    if (squareStatus.equals(PlayerSymbol.NONE))
                        components[j] = Button.of(squareStatus.buttonStyle, buttonId, Emoji.fromFormatted("<:ttoNone:1083320877334142987>"));
                    else
                        components[j] = Button.of(squareStatus.buttonStyle, buttonId, squareStatus.emoji);
                }

                if(this.winner != null) {
                    for(int j = 0; j < 3; j++) {
                        if(!this.winningPositions[i][j])
                            components[j] = ((Button)components[j]).asDisabled();
                    }
                }

                actionRows[i] = ActionRow.of(components);
            }
            return actionRows;
        }

        public String getGameStateMessage() {
            String message;
            if(winner == null) {
                if(turn.equals(PlayerSymbol.X))
                    message = this.player1.getAsMention() + "\'s turn.";
                else
                    if(player2 != null)
                        message = this.player2.getAsMention() + "\'s turn.";
                    else
                        message = "Waiting for someone to join...";
            }
            else {
                if(winner.equals(PlayerSymbol.X))
                    message = this.player1.getAsMention() + " won.";
                else if(winner.equals(PlayerSymbol.O))
                    message = this.player2.getAsMention() + " won.";
                else
                    message = "**DRAW**";
            }
            return "**Tic-Tac-Toe:** " + message;
        }

        public void click(int row, int column, ButtonInteractionEvent clickEvent) {
            this.board[row][column] = this.turn;

            movesLeft--;
            checkWin();

            this.turn = this.turn.reverse();

            //update message
            clickEvent.deferEdit()
                    .setContent(this.getGameStateMessage())
                    .setComponents(this.buildBoard())
                    .queue();
        }

        public void checkWin() {
            //check draw
            if(movesLeft == 0) {
                this.gameOver(PlayerSymbol.NONE, new boolean[3][3]);
                return;
            }

            for(int i = 0; i < 3; i++) {
                //check horizontal lines
                if(board[i][0].equals(this.turn) && board[i][1].equals(this.turn) && board[i][2].equals(this.turn)) {
                    boolean[][] winningPositions = new boolean[3][3];
                    winningPositions[i][0] = true; winningPositions[i][1] = true; winningPositions[i][2] = true;
                    this.gameOver(board[i][0], winningPositions);
                    return;
                }

                //check vertical lines
                if(board[0][i].equals(this.turn) && board[1][i].equals(this.turn) && board[2][i].equals(this.turn)) {
                    boolean[][] winningPositions = new boolean[3][3];
                    winningPositions[0][i] = true; winningPositions[1][i] = true; winningPositions[2][i] = true;
                    this.gameOver(board[0][i], winningPositions);
                    return;
                }
            }

            //check diagonals
            if(board[0][0].equals(this.turn) && board[1][1].equals(this.turn) && board[2][2].equals(this.turn)) {
                boolean[][] winningPositions = new boolean[3][3];
                winningPositions[0][0] = true; winningPositions[1][1] = true; winningPositions[2][2] = true;
                this.gameOver(board[0][0], winningPositions);
                return;
            }
            if(board[0][2].equals(this.turn) && board[1][1].equals(this.turn) && board[2][0].equals(this.turn)) {
                boolean[][] winningPositions = new boolean[3][3];
                winningPositions[0][2] = true; winningPositions[1][1] = true; winningPositions[2][0] = true;
                this.gameOver(board[0][2], winningPositions);
            }
        }

        public void gameOver(PlayerSymbol winner, boolean[][] winningPositions) {
            this.winner = winner;
            this.winningPositions = winningPositions;
        }

    }

    enum PlayerSymbol {
        X(ButtonStyle.DANGER, Emoji.fromFormatted("<:ttoX:897468291927388250>")),
        O(ButtonStyle.PRIMARY, Emoji.fromFormatted("<:ttoO:897468061261647903>")),
        NONE(ButtonStyle.SECONDARY, null);

        public final ButtonStyle buttonStyle;
        public final Emoji emoji;

        PlayerSymbol(ButtonStyle buttonStyle, Emoji emoji) {
            this.buttonStyle = buttonStyle;
            this.emoji = emoji;
        }

        public PlayerSymbol reverse() {
            return this.equals(PlayerSymbol.X) ? PlayerSymbol.O : PlayerSymbol.X;
        }
    }

}
