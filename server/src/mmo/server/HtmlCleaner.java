/*
 * Copyright 2015 Florian Hassanen
 *
 * This file is part of mmo-server.
 *
 * mmo-server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * mmo-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with mmo-server.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package mmo.server;

import com.google.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import javax.inject.Inject;

public class HtmlCleaner {

    public static final Document.OutputSettings OUTPUT_SETTINGS =
            new Document.OutputSettings().prettyPrint(false);

    public static final Whitelist WHITELIST =
            Whitelist.relaxed().preserveRelativeLinks(true);


    @Inject
    public HtmlCleaner() {
    }

    public String useSingleQuotesForAttributeQuotes(String html) {
        StringBuilder b = new StringBuilder();
        boolean inTag = false;
        boolean inAttribute = false;
        for (char c : Lists.charactersOf(html)) {
            if (!inTag) {
                if (c == '<') {
                    inTag = true;
                }
                b.append(c);
                continue;
            }

            // now we are in a Tag
            if (c == '"') {
                inAttribute = !inAttribute;
                b.append('\'');
                continue;
            }

            inTag = inAttribute || c != '>';

            b.append(c);
        }

        return b.toString();
    }

    public String clean(String unsafeHtml) {
        return useSingleQuotesForAttributeQuotes(Jsoup.clean(
                unsafeHtml,
                "http://example.com/",
                WHITELIST,
                OUTPUT_SETTINGS));
    }
}
