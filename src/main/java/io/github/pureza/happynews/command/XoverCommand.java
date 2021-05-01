package io.github.pureza.happynews.command;

import io.github.pureza.happynews.newsgroup.Article;
import io.github.pureza.happynews.newsgroup.ArticleHeader;
import io.github.pureza.happynews.newsgroup.Newsgroup;
import io.github.pureza.happynews.server.NNTPServer;
import io.github.pureza.happynews.user.User;

import java.io.IOException;

/**
 * XOVER Command
 *
 * Displays a summary of a set of articles.
 *
 * Usage:   XOVER [article-number | range]
 * Example: XOVER 1
 *          XOVER 2-7
 *          XOVER 87-
 * See also: Command HEAD
 * Permission: Reader
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class XoverCommand extends Command {

    public XoverCommand(User client, String args, NNTPServer server) throws IOException {
        super(client, args, server);
    }


    @Override
    public void process() throws IOException {
        Newsgroup group = client.getCurrentGroup();
        if (group == null) {
            out.print("412 no newsgroup has been selected\n");
            return;
        }

        int lastInGroup = group.getLastArticleNum();
        int startIndex;
        int endIndex;
        if (args.length < 2) {
            if (client.getCurrentArticleIndex() <= 0 || group.isEmpty()) {
                out.print("420 no current article has been selected\n");
                return;
            }

            startIndex = endIndex = client.getCurrentArticleIndex();
        } else {
            String range = args[1];
            String[] limits = range.split("-");

            try {
                startIndex = Integer.parseInt(limits[0]);
                endIndex = (limits.length == 1 ?
                        (!range.contains("-") ? startIndex : lastInGroup)
                        : Integer.parseInt(limits[1]));
            } catch (Exception ex) {
                out.print("501 command syntax error\n");
                return;
            }

            if (endIndex < startIndex || endIndex < group.getFirstArticleNum() || startIndex > lastInGroup) {
                out.print("420 No article(s) selected\n");
                return;
            }

            if (startIndex < group.getFirstArticleNum()) {
                startIndex = group.getFirstArticleNum();
            }

            if (endIndex > lastInGroup) {
                endIndex = lastInGroup;
            }
        }

        out.print("224 Overview information follows\n");

        for (int i = startIndex; i <= endIndex; i++) {
            Article article = server.getArticle(group.getArticleId(i));
            if (article != null) {
                ArticleHeader header = article.getHeader();

                out.printf("%d\t%s\t%s\t%s\t%s\n", i,
                        header.get("Subject"),
                        header.get("From"),
                        header.get("Date"),
                        header.get("Message-ID"));
            }
        }
        out.print(".\n");
    }
}
