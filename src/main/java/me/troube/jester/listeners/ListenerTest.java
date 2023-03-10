package me.troube.jester.listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ListenerTest extends ListenerAdapter {

    public void onSlashCommand(SlashCommandInteractionEvent event) {
        if (event.getName().equals("hello")) {

        }
    }

    public void onButtonClick(ButtonInteractionEvent event) {
        if (event.getComponentId().startsWith("TTO")) {
/*            event.deferEdit().setActionRows(
                    ActionRow.of(
                            Button.danger("TTO11", "X"),
                            Button.danger("TTO12", " "),
                            Button.danger("TTO13", " ")
                    ),
                    ActionRow.of(
                            Button.danger("TTO21", " "),
                            Button.danger("TTO22", " "),
                            Button.danger("TTO23", " ")
                    ),
                    ActionRow.of(
                            Button.danger("TTO31", " "),
                            Button.danger("TTO32", " "),
                            Button.danger("TTO33", " ")
                    )
            ).queue();*/
        }
    }
}