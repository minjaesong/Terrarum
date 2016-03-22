package com.Torvald.Terrarum.ConsoleCommand;

import com.Torvald.Terrarum.Actors.ActorValue;
import com.Torvald.Terrarum.Game;
import com.Torvald.Terrarum.Terrarum;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by minjaesong on 16-01-19.
 */
public class GetAV implements ConsoleCommand {
    @Override
    public void execute(String[] args) {
        Echo echo = new Echo();

        try {
            if (args.length == 1) {
                // print all actorvalue of player
                ActorValue av = Terrarum.game.player.getActorValue();
                Set keyset = av.getKeySet();

                keyset.forEach(
                        elem -> echo.execute(elem + " = " + av.get((String) elem))
                );

            }
            else if (args.length != 3 && args.length != 2) {
                printUsage();
            }
            else if (args.length == 2) {
                echo.execute("player." + args[1] + " = "
                        + Terrarum.game.player.getActorValue().get(args[1])
                        + " ("
                        + Terrarum.game.player.getActorValue().get(args[1]).getClass()
                                       .getSimpleName()
                        + ")"
                );
            }
            else if (args.length == 3) {

            }
        }
        catch (NullPointerException e) {
            if (args.length == 2) {
                echo.execute(args[1] + ": actor value does not exist.");
            }
            else if (args.length == 3) {
                echo.execute(args[2] + ": actor value does not exist.");
            }
            else {
                throw new NullPointerException();
            }
        }
    }

    @Override
    public void printUsage() {
        Echo echo = new Echo();
        echo.execute("Get desired actor value of specific target.");
        echo.execute("Usage: getav (id) <av>");
        echo.execute("blank ID for player");
    }
}
