package io.github.pureza.happynews.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MV Command
 *
 * Moves an article to a different directory.
 * It is not allowed to move articles outside an editor's home.
 *
 * Usage:   MV <old path> <new path>
 * Example: MV <672@sapo.pt> pink-floyd
 *          MV ../<83@netcabo.pt> .
 * Permission: Editor
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class MvCommand extends Command {

    /** Used to validate article ids */
    private final ArticleValidator articleValidator = new ArticleValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MvCommand(User client, String args, NNTPServer server) throws IOException {
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
        if (args.length != 3) {
            out.print("501 command syntax error. args.length=" + args.length + "\n");
            return;
        }

        Editor editor = (Editor) client;
        Path cwd = editor.getPath();

        String dirName = args[1].replaceFirst("<.*", "");
        String fileName = args[1].replaceFirst(".*<", "<");
        Matcher matcher = ArticleValidator.ARTICLE_ID_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            fileName = matcher.group(1) + "@" + matcher.group(2);
        }
        if (fileName.isEmpty()) {
            out.print("501 command syntax error. fileName.isEmpty()\n");
        }
        String articleId = "<" + fileName + ">";

        Path srcPath = cwd.resolve(dirName + fileName).normalize();
        Path dstPath = cwd.resolve(args[2]).normalize();

        // Check if the article id follows the <id@host> format
        if (!(articleValidator.isValidArticleId(articleId))) {
            out.print("501 command syntax error. !isValidArticleId\n");
            return;
        }

        Path srcFilePath = srcPath.getParent().resolve(fileName).normalize();
        Path dstFilePath = dstPath.resolve(fileName);

        // Does the article exist?
        if (!Files.exists(srcFilePath)) {
            out.print("488 article not found\n");
            return;
        }

        // Does the new path exist?
        if (!Files.exists(dstPath)) {
            out.print("488 path not found\n");
            return;
        }


        Path home = editor.getHome();

        // Are we moving an article within our home?
        if (dstFilePath.startsWith(home) && (srcPath.startsWith(home))) {
            try {
                Files.move(srcFilePath, dstFilePath);
                out.print("288 article moved\n");
            } catch (IOException ex) {
                logger.debug("Unable to mv {} {}", srcFilePath, dstFilePath);
                out.print("488 move failed\n");
            }
        } else {
            out.print("502 Permission denied\n");
        }
    }
}

