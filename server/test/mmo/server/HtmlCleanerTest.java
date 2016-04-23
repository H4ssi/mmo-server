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

import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HtmlCleanerTest {
    private HtmlCleaner cleaner = new HtmlCleaner();

    @Test
    public void superfluousClosingTag() {
        assertThat(cleaner.clean("<i>a</i></b>"), is("<i>a</i>"));
    }

    @Test
    public void missingClosingTag() {
        assertThat(cleaner.clean("<b><i>a</i>"), is("<b><i>a</i></b>"));
    }

    @Test
    public void forbiddenTag() {
        assertThat(cleaner.clean("asdf<script>asdf</script>asdf"),
                is("asdfasdf"));
    }

    @Test
    public void completelyStrippedMessage() {
        assertThat(cleaner.clean("<script>asdf</script>"), is(""));
    }

    @Test
    public void singleQuotesForAttributes() {
        assertThat(cleaner.clean("<img src=\"file\">"), is("<img src='file'>"));
    }

    @Test
    public void replaceInvalidCharsWithHtmlEntities() {
        assertThat(
                cleaner.clean(
                        "<a href=\"x>\">'\"</a>'\"<a href=\"x>\">'\"</a>"),
                is("<a href='x>'>'\"</a>'\"<a href='x>'>'\"</a>"));
    }
}
