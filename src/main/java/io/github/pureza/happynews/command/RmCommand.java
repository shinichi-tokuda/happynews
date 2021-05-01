package io.github.pureza.happynews.command;

import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.user.Editor;
import io.github.pureza.happynews.validation.ArticleValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;

/**
 * RM Command
 *
 * Deletes an article from the editor's home.
 * The corresponding newsgroup article is not deleted.
 *
 * Usage:   RM [path/]<message-id>
 * Example: RM <786@yahoo.com>
 *          RM ../<83@netcabo.pt>
 * Permission: Editor
 */
@SuppressWarnings("unused")
public class RmCommand extends Command {

    /** Used to validate article ids */
    private final ArticleValidator articleValidator = new ArticleValidator();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public RmCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        if (!(client instanceof Editor)) {
            out.print("502 permission denied\n");
            return;
        }

        Editor editor = (Editor) client;

        if (args.length != 2) {
            out.print("501 command syntax error\n");
            return;
        }

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

        Path argPath = editor.getPath().resolve(dirName + fileName).normalize();

        // Check if the article id follows the <id@host> format
        if (!(articleValidator.isValidArticleId(articleId))) {
            out.print("501 command syntax error\n");
            return;
        }

        Path filePath = argPath.getParent().resolve(fileName);

        Path userHome = editor.getHome();

        // Can't delete other user's files
        if (filePath.startsWith(userHome)) {
            try {
                Files.delete(filePath);
                out.print("290 article removed from your local directory\n");
            } catch (IOException ex) {
                logger.debug("Unable to rm {}", argPath, ex);
                out.print("490 no such article\n");
            }
        } else {
            out.print("502 permission denied\n");
        }
    }
}


