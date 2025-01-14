package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * MKDIR Command
 *
 * Creates a new directory within the editor home.
 * It is forbidden to create directories within the home of another editor.
 *
 * Usage:   MKDIR <directory name>
 * Example: MKDIR bored
 * Permission: Editor
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MkdirCommand extends Command {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MkdirCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        // Check if the user has the right permission
        if (!(client instanceof Editor)) {
            out.print("502 permission denied\n");
            return;
        }

        // Check if the command is well formed
        if (args.length != 2) {
            out.print("501 command syntax error\n");
            return;
        }

        Editor editor = (Editor) client;
        String arg = args[1];
        Path path = editor.getPath().resolve(arg).normalize();

        // Do not allow creating directories in other homes
        if (path.startsWith(editor.getHome())) {
            try {
                Files.createDirectory(path);
                out.print("287 directory created\n");
            } catch (Exception ex) {
                logger.debug("Unable to mkdir {}", path, ex);
                out.print("487 directory not created\n");
            }
        } else {
            out.print("502 permission denied\n");
        }
    }
}
