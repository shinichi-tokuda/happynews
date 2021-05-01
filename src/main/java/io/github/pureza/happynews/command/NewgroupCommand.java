package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * NEWGROUP Command
 *
 * Creates a new newsgroup.
 *
 * Usage:   NEWGROUP <name>
 * Example: NEWGROUP alt.tv.twin-peaks
 * Permission: Reader
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class NewgroupCommand extends Command {

    public NewgroupCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Admin)) {
            out.print("450 create newsgroup not allowed\r");
            return;
        }

        try {
            String groupName = args[1];
            if (server.createGroup(groupName)) {
                out.print("250 group created\r");
            } else {
                out.print("451 create failed\r");
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            out.print("501 command syntax error\n");
        }
    }
}
