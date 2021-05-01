package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Admin;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * PASSWD Command
 *
 * Updates the password of an user.
 * This command can be called in two different ways:
 * - PASSWD <new password> changes the password of the user itself
 * - PASSWD <username> <new password> updates the password of another user.
 *   Only administrators can do this.
 *
 * Usage: PASSWD [username] <new password>
 * Permission: Reader or Admin if changing the password of another user
 */
@SuppressWarnings("unused")
public class PasswdCommand extends Command {

    public PasswdCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        switch(args.length) {
            case 2:
                // Update own password
                if (server.changeUserPassword(client.getUsername(), args[1])) {
                    out.print("284 password changed\n");
                } else {
                    out.print("484 password not changed - maybe it is invalid?\n");
                }
                break;
            case 3:
                // Update the password of another user
                User u = server.getUser(args[1]);
                if (!(client instanceof Admin) && u != client) {
                    out.print("502 permission denied\n");
                    return;
                }

                if (u == null) {
                    out.print("484 user doesn't exist\n");
                    return;
                }
                if (server.changeUserPassword(u.getUsername(), args[2])) {
                    out.print("284 password changed\n");
                } else {
                    out.print("484 password not changed - maybe it is invalid?\n");
                }
                break;
            default:
                out.print("501 command syntax error\n");
                break;
        }
    }
}
