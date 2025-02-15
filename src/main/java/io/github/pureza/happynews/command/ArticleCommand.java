package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;
import io.github.pureza.happynews.validation.ArticleValidator;

import java.io.IOException;


/**
 * ARTICLE command
 *
 * Displays an article.
 *
 * This command may be called in three different ways:
 * - With no arguments, it prints the article referenced by a pointer within
 *   the newsgroup the user is reading
 * - With a numeric argument, displays the article with that number and updates
 *   the pointer to reference to it
 * - With an article id (a string of the form <unique@host>), displays the
 *   article with said id. This option allows the user to read an article
 *   without subscribing to the newsgroup, because unlike article numbers, which
 *   are internal to the newsgroup, article ids are global.
 *
 * Usage:   ARTICLE [article-id | article-number]
 * Example: ARTICLE <876@sapo.pt>
 *          ARTICLE 7842
 * See also: Commands BODY, HEAD, STAT, NEXT, LAST
 * Permission: Reader
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class ArticleCommand extends Command {

    /** Validator for article ids */
    private final ArticleValidator articleValidator = new ArticleValidator();


    public ArticleCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }

    @Override
    public void process() throws IOException {
        int articleIndex = client.getCurrentArticleIndex();

        // Is the argument an article id?
        boolean isArticleId = false;
        if (args.length == 2) {
            try {
                articleIndex = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                // The articleIndex presented in the output should be 0 in this case
                articleIndex = 0;
                isArticleId = true;
            }
        }

        // Check for invalid article ids
        if (args.length > 2 || (isArticleId && !articleValidator.isValidArticleId(args[1]))) {
            out.print("501 command syntax error\n");
            return;
        }


        // To read the message with the given number, the user must subscribe
        // the newsgroup
        if (!isArticleId && client.getCurrentGroup() == null) {
            out.print("412 no newsgroup has been selected\n");
            return;
        }

        Article a;
        switch (args.length) {
            case 1:
                // Displays the current message
                if (client.getCurrentGroup().isEmpty()) {
                    out.print("420 no current article has been selected\n");
                    return;
                }
                a = server.getArticle(client.getCurrentArticleId());
                break;
            case 2:
                if (isArticleId) {
                    String articleID = args[1];
                    a = server.getArticle(articleID);
                    if (a == null) {
                        out.print("430 no such article found\n");
                        return;
                    }
                } else {
                    // Throws an exception if the first argument is an id
                    if (!client.getCurrentGroup().containsArticleNum(articleIndex)) {
                        out.print("423 no such article number in this group\n");
                        return;
                    }

                    // Updates the pointer
                    client.setCurrentArticleIndex(articleIndex);
                    a = server.getArticle(client.getCurrentArticleId());
                }

                break;
            default:
                throw new IllegalArgumentException("Can't happen!");
        }

        out.printf("220 %d %s article retrieved - head and body follows\n", articleIndex, a.getId());
        out.print(a.getHeader() + "\n");
        String body = a.getBody();
        if (body.length() > 0) {
            out.print("\n");
            out.print(a.getBody() + "\n");
        }
        out.print(".\n");
    }
}

