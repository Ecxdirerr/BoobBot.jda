package bot.boobbot.commands.nsfw

import bot.boobbot.flight.CommandProperties
import bot.boobbot.models.BbApiCommand

@CommandProperties(description = "Pokemon Porn!", nsfw = true, category = CommandProperties.category.FANTASY)
class Poke : BbApiCommand("PokePorn")
