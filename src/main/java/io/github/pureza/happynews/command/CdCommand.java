package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * CD Command
 *
 * Allows the user to change the current working directory
 *
 * Usage:   CD <new directory>
 * Example: CD cinema
 *          CD ..
 *          CD directors/david-lynch
 *          CD ../../twin-peaks
 * Permission: Editor
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class CdCommand extends Command {

    public CdCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }

    @Override
    public void process() throws IOException {
        // Check if the user has permission
        if (!(client instanceof Editor)) {
            out.print("502 permission denied\n");
            return;
        }

        Editor editor = (Editor) client;

        // Check if the command is well formed
        if (args.length != 2) {
            out.print("501 command syntax error\n");
            return;
        }

        String arg = args[1];

        Path path = editor.getPath().resolve(arg).normalize();

        // Do not go above /users
        Path usersHome = config().usersHome();
        if (!path.startsWith(usersHome)) {
            path = usersHome;
        }

        if (Files.isDirectory(path)) {
            editor.setPath(path);
            out.print("285 directory changed\n");
        } else {
            out.print("485 directory not changed\n");
        }
    }
}
