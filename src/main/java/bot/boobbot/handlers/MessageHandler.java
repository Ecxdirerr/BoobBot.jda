package bot.boobbot.handlers;

import bot.boobbot.BoobBot;
import bot.boobbot.flight.Command;
import bot.boobbot.flight.Context;
import bot.boobbot.misc.Constants;
import bot.boobbot.misc.Formats;
import bot.boobbot.misc.Utils;
import de.mxro.metrics.jre.Metrics;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;


public class MessageHandler extends ListenerAdapter {

    private static String prefix = BoobBot.Companion.isDebug() ? "!bb" : "bb";

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        BoobBot.Companion.getMetrics().record(Metrics.happened("MessageReceived"));


        if (!BoobBot.Companion.isReady()) {
            return;
        }

        if (event.getAuthor().isFake() || event.getAuthor().isBot()) {
            return;
        }

        if (event.getChannelType().isGuild()) {
            if (!event.getGuild().isAvailable() || !event.getTextChannel().canTalk()) {
                return;
            }
            if (event.getMessage().mentionsEveryone()){
                BoobBot.Companion.getMetrics().record(Metrics.happened("atEveryoneSeen"));
            }
        }


        final String messageContent = event.getMessage().getContentRaw();
        final String mention = event.getChannelType().isGuild()
                ? event.getGuild().getSelfMember().getAsMention()
                : event.getJDA().getSelfUser().getAsMention();

        boolean isMentionTrigger = messageContent.startsWith(mention);
        boolean hasPrefix = isMentionTrigger || messageContent.toLowerCase().startsWith(prefix);

        if (isMentionTrigger && !messageContent.contains(" ") || !hasPrefix) {
            return;
        }

        final String trigger = isMentionTrigger ? mention + " " : prefix;

        final String[] content = messageContent.substring(trigger.length())
                .split(" +", 2);

        if (content.length == 0) {
            return;
        }

        final String commandString = content[0].toLowerCase();
        final String[] args = content.length > 1 ? content[1].split(" +") : new String[0];

        final Command command = Utils.Companion.getCommand(commandString);

        if (command == null) {
            return; // TODO Check if mention prefix and call Nekos.getChat?
        }

        if (!command.getProperties().enabled()) { // Is command enabled?
            return;
        }

        if (command.getProperties().developerOnly() &&
                !Constants.Companion.getOWNERS().contains(event.getAuthor().getIdLong())) { // Is command developer only?
            return;
        }

        if (command.getProperties().guildOnly() && !event.getChannelType().isGuild()) { // Is command guild-only?
            event.getChannel().sendMessage("No, whore you can only use this in a guild").queue();
            return;
        }

        if (command.getProperties().nsfw() && event.getChannelType().isGuild() &&
                !event.getTextChannel().isNSFW()) {
            event.getChannel().sendMessage("This isn't a NSFW channel you whore. Confused? try `bbhuh`").queue();
            return;
        }

        if (event.getChannelType().isGuild() && !event.getGuild().getSelfMember().hasPermission(event.getTextChannel(), Permission.MESSAGE_EMBED_LINKS)) {
            event.getChannel().sendMessage("I do not have permission to use embeds, da fuck?").queue();
            return;
        }

        if (command.getProperties().donorOnly() && !Utils.Companion.isDonor(event.getAuthor())) {
            event.getChannel().sendMessage(Formats.Companion.error(
                    " Sorry this command is only available to our Patrons.\n"
                            + event
                            .getJDA()
                            .asBot()
                            .getShardManager()
                            .getEmoteById(475801484282429450L)
                            .getAsMention()
                            + "Stop being a cheap fuck and join today!\nhttps://www.patreon.com/OfficialBoobBot")).queue();
            return;
        }

        try {
            Utils.Companion.logCommand(event.getMessage());
            BoobBot.Companion.getMetrics().record(Metrics.happened("command"));
            BoobBot.Companion.getMetrics().record(Metrics.happened(command.getName()));
            command.execute(new Context(trigger, event, args));
        } catch (Exception e) {
            BoobBot.Companion.getLog().error("Command `" + command.getName() + "` encountered an error during execution", e);
            event.getMessage().addReaction("\uD83D\uDEAB").queue();
        }
    }
}
