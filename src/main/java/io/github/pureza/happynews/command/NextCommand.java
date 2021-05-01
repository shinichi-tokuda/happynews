package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

import io.github.pureza.happynews.newsgroup.Article;

/**
 * NEXT Command
 *
 * Moves the pointer to the next article within the newsgroup.
 *
 * This command does not print the contents of the article. For that, the user
 * should use the ARTICLE, HEAD or BODY commands.
 *
 * Usage:   NEXT
 * Permission: Reader
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class NextCommand extends Command {

    public NextCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        Newsgroup currentGroup = client.getCurrentGroup();
        if (currentGroup == null) {
            out.print("412 no newsgroup has been selected\r");
            return;
        }

        synchronized (currentGroup) {
            if (currentGroup.isEmpty()) {
                out.print("420 no current article has been selected\n");
                return;
            }

            int articleIndex = client.getCurrentArticleIndex();
            if (!currentGroup.hasNext(articleIndex)) {
                out.print("421 no next article in this group\n");
                return;
            }

            client.setCurrentArticleIndex(currentGroup.nextIndex(articleIndex));
            Article a = server.getArticle(client.getCurrentArticleId());
            out.printf("223 %d %s article retrieved - request text separately\n", client.getCurrentArticleIndex(), a.getId());
        }
    }
}
