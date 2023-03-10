package me.troube.jester.games;

import me.troube.jester.games.objects.TwoPlayerGame;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.HashMap;
import java.util.Map;

public class Connect4 extends ListenerAdapter {

    private final Map<Long, Connect4Game> games = new HashMap<>();

/*    @Override
    public void onReady(ReadyEvent event) {
        JDA jda = event.getJDA();
    }*/

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if(event.getName().equals("connect4")) {
            event.reply("**Starting the game...**").queue((interaction -> interaction.retrieveOriginal().queue((message -> {
                Connect4Game game = new Connect4Game(
                        message.getIdLong(),
                        event.getMember());

                message.editMessage(game.getGameStateMessage()).setComponents(game.buildBoard()).queue();
                games.put(message.getIdLong(), game);
            }))));
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("CN4-")) {
            Connect4Game game = games.get(event.getMessage().getIdLong());
            if(game == null) {
                event.deferEdit().queue();
                return;
            }
            String[] buttonData = event.getButton().getId().split("-");
            int row = Integer.parseInt(buttonData[2]);
            int column = Integer.parseInt(buttonData[3]);

            if(game.winner != null || (game.turn.equals(PlayerSymbol.RED) && !event.getMember().equals(game.player1)))
            {
                event.deferEdit().queue();
                return;
            }

            if(game.turn.equals(PlayerSymbol.YELLOW)) {
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

    private static class Connect4Game extends TwoPlayerGame {

        private final static int[] diagonals = new int[]{1, -1};

        public int scorePlayer1;
        public int scorePlayer2;

        private int movesLeft = 25;

        public PlayerSymbol turn = PlayerSymbol.RED;
        public PlayerSymbol winner;
        public boolean[][] winningPositions;
        public PlayerSymbol[][] board = new PlayerSymbol[5][5];

        public Connect4Game(Long id, Member player1) {
            this.id = id;
            this.player1 = player1;

            for(int i = 0; i < 5; i++) {
                for(int j = 0; j < 5; j++) {
                    this.board[i][j] = PlayerSymbol.NONE;
                }
            }

        }

        public ActionRow[] buildBoard() {
            ItemComponent[] components = new ItemComponent[5];
            ActionRow[] actionRows = new ActionRow[5];
            PlayerSymbol squareStatus;
            String buttonId;
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    squareStatus = this.board[i][j];
                    buttonId = "CN4-" + this.id + "-" + i + "-" + j;
                    if (squareStatus.equals(PlayerSymbol.NONE))
                        components[j] = Button.of(squareStatus.buttonStyle, buttonId, Emoji.fromFormatted("<:ttoNone:1083320877334142987>"));
                    else
                        components[j] = Button.of(squareStatus.buttonStyle, buttonId, squareStatus.emoji);
                }

                if(this.winner != null) {
                    for(int j = 0; j < 5; j++) {
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
                if(turn.equals(PlayerSymbol.RED))
                    message = this.player1.getAsMention() + "\'s turn.";
                else
                if(player2 != null)
                    message = this.player2.getAsMention() + "\'s turn.";
                else
                    message = "Waiting for someone to join...";
            }
            else {
                if(winner.equals(PlayerSymbol.RED))
                    message = this.player1.getAsMention() + " won.";
                else if(winner.equals(PlayerSymbol.YELLOW))
                    message = this.player2.getAsMention() + " won.";
                else
                    message = "**DRAW**";
            }
            return "**Connect 4:** " + message;
        }

        public void click(int row, int column, ButtonInteractionEvent clickEvent) {
            boolean valid = false;
            for(int i = 4; i >= 0; i--) {
                if(this.board[i][column].equals(PlayerSymbol.NONE)) {
                    valid = true;
                    row = i;
                    break;
                }
            }
            if(!valid) {
                clickEvent.deferEdit().queue();
                return;
            }
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
                this.gameOver(PlayerSymbol.NONE, new boolean[5][5]);
                return;
            }

            for(int i = 0; i < 5; i++) {
                //check horizontal lines
                boolean[][] winningPositions = new boolean[5][5];
                for(int j = 0; j < 4; j++) {
                    if(!board[i][j].equals(this.turn))
                        break;
                    winningPositions[i][j] = true;
                    if(j == 3) {
                        this.gameOver(board[i][0], winningPositions);
                        return;
                    }
                }
                winningPositions = new boolean[5][5];
                for(int j = 1; j < 5; j++) {
                    if(!board[i][j].equals(this.turn))
                        break;
                    winningPositions[i][j] = true;
                    if(j == 4) {
                        this.gameOver(board[i][1], winningPositions);
                        return;
                    }
                }

                //check vertical lines
                winningPositions = new boolean[5][5];
                for(int j = 0; j < 4; j++) {
                    if(!board[j][i].equals(this.turn))
                        break;
                    winningPositions[j][i] = true;
                    if(j == 3) {
                        this.gameOver(board[0][i], winningPositions);
                        return;
                    }
                }
                winningPositions = new boolean[5][5];
                for(int j = 1; j < 5; j++) {
                    if(!board[j][i].equals(this.turn))
                        break;
                    winningPositions[j][i] = true;
                    if(j == 4) {
                        this.gameOver(board[1][i], winningPositions);
                        return;
                    }
                }

                //check diagonal lines
                for(int j = 0; j < 2; j++) {
                    int k, ki, kj;
                    for(int i1 : diagonals) {
                        winningPositions = new boolean[5][5];
                        for (k = 0; k < 4; k++) {
                            ki = i+k*i1;
                            kj = j+k;
                            if (ki > 4 || kj > 4 || ki < 0 || kj < 0)
                                break;
                            if (!board[ki][kj].equals(this.turn))
                                break;
                            winningPositions[ki][kj] = true;
                            if (k == 3) {
                                this.gameOver(board[i][j], winningPositions);
                                return;
                            }
                        }
                    }
                }
            }
        }

        public void gameOver(PlayerSymbol winner, boolean[][] winningPositions) {
            this.winner = winner;
            this.winningPositions = winningPositions;
        }

    }

    enum PlayerSymbol {
        YELLOW(ButtonStyle.PRIMARY, Emoji.fromUnicode("U+1F7E1")),
        RED(ButtonStyle.PRIMARY, Emoji.fromUnicode("U+1F534")),
        NONE(ButtonStyle.PRIMARY, Emoji.fromFormatted("<:ttoNone:1083320877334142987>"));

        public final ButtonStyle buttonStyle;
        public final Emoji emoji;

        PlayerSymbol(ButtonStyle buttonStyle, Emoji emoji) {
            this.buttonStyle = buttonStyle;
            this.emoji = emoji;
        }

        public PlayerSymbol reverse() {
            return this.equals(PlayerSymbol.YELLOW) ? PlayerSymbol.RED : PlayerSymbol.YELLOW;
        }
    }

}