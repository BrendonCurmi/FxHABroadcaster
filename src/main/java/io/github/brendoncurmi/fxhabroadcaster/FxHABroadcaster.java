package io.github.brendoncurmi.fxhabroadcaster;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ReceiveType;
import com.pixelmonmod.pixelmon.api.events.PixelmonReceivedEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

@Plugin(id = "fxhabroadcaster",
        name = "FxHABroadcaster",
        version = "1.0",
        authors = {"BrendonCurmi/FusionDev"},
        description = "Broadcasts when a player catches a hidden ability legendary pokemon",
        dependencies = {
                @Dependency(id = "pixelmon", version = "7.0.8"),
                @Dependency(id = "spongeapi", version = "7.1.0")
        })
public class FxHABroadcaster {

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        Pixelmon.EVENT_BUS.register(this);
    }

    private static final int MAX_IVS = IVStore.MAX_IVS * StatsType.getStatValues().length;

    @SubscribeEvent
    public void onPokemonCaught(PixelmonReceivedEvent event) {
        if (event.receiveType == ReceiveType.PokeBall) {
            Pokemon pokemon = event.pokemon;
            if (pokemon.isLegendary() && hasHiddenAbility(pokemon)) {
                Player player = (Player) event.player;
                int total = getTotalIVs(pokemon);

                Text.Builder hoverBuilder = Text.builder();
                for (StatsType type : StatsType.getStatValues()) {
                    if (!hoverBuilder.getChildren().isEmpty())
                        hoverBuilder.append(Text.NEW_LINE);
                    hoverBuilder.append(Text.of("§8" + type.name() + ": §e" + pokemon.getIVs().get(type)));
                }

                Sponge.getServer().getBroadcastChannel().send(
                        Text.builder()
                                .append(Text.of(TextColors.GREEN, player.getName() + " has caught an HA " + pokemon.getDisplayName() + " with " + prettyTally(total)))
                                .onHover(TextActions.showText(hoverBuilder.build()))
                                .build()
                );
            }
        }
    }

    private static int getTotalIVs(Pokemon pokemon) {
        int total = 0;
        for (StatsType stats : StatsType.getStatValues()) total += pokemon.getIVs().get(stats);
        return total;
    }

    private static boolean hasHiddenAbility(Pokemon pokemon) {
        return pokemon.getBaseStats().abilities[2] != null;
    }

    private static String prettyTally(int val) {
        return "§e" + val + "§8/§e" + MAX_IVS + " §8(§a" + (Math.round((float) val / MAX_IVS * 100)) + "%§8)";
    }
}
