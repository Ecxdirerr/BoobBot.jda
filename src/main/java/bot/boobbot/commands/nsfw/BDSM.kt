package bot.boobbot.commands.nsfw

import bot.boobbot.flight.CommandProperties
import bot.boobbot.models.BbApiCommand

@CommandProperties(
        description = "Bondage and Discipline (BD), Dominance and Submission (DS), Sadism and Masochism (SM)",
        nsfw = true,
        category = CommandProperties.category.KINKS
)
class BDSM : BbApiCommand("bdsm")
