/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the NetBout.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.netbout.spi.client;

import com.netbout.spi.Bout;
import com.netbout.spi.UrnMocker;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link RestBout}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class RestBoutTest {

    /**
     * RestBout can fetch a date of the bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesDateOfBout() throws Exception {
        final Date date = new Date();
        final String text =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.ENGLISH)
                .format(date);
        final RestClient client = new RestClientMocker()
            .onXPath("/page/bout/date/text()", text)
            .mock();
        final Bout bout = new RestBout(client);
        MatcherAssert.assertThat(bout.date(), Matchers.equalTo(date));
    }

    /**
     * RestBout can fetch a list of participants.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void fetchesListOfParticipants() throws Exception {
        final String first = new UrnMocker().mock().toString();
        final String second = new UrnMocker().mock().toString();
        final RestClient client = new RestClientMocker()
            .onXPath("ticipant/identity/text()", Arrays.asList(first, second))
            .mock();
        final Bout bout = new RestBout(client);
        MatcherAssert.assertThat(
            bout.participants(),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            bout.participants().contains(first),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            bout.participants().contains(second),
            Matchers.is(true)
        );
        MatcherAssert.assertThat(
            bout.participants().contains("bla-bla-bla"),
            Matchers.is(false)
        );
    }

}
