package me.troube.jester;

import me.troube.jester.games.Connect4;
import me.troube.jester.games.TicTacToe;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.utils.Compression;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class Jester {

    private static JDA jda;

    public static JDA getJDA() {
        return jda;
    }

    public static void main(String[] args) {
        JDABuilder builder = JDABuilder.createDefault(args[0]);

        // Disable parts of the cache
        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        // Enable the bulk delete event
        builder.setBulkDeleteSplittingEnabled(false);
        // Disable compression (not recommended)
        builder.setCompression(Compression.NONE);
        // Set activity (like "playing Something")
        builder.setActivity(Activity.listening("/games"));


        builder.addEventListeners(new TicTacToe());
        builder.addEventListeners(new Connect4());

        try {
            jda = builder.build();
            jda.awaitReady();
        } catch(Exception ignore) {};
        for(Guild guild : jda.getGuilds()) {
            guild.upsertCommand("tto", "Play tic tac toe").queue();
            guild.upsertCommand("connect4", "Play connect 4").queue();
        }
    }

}
