/**
 * Copyright (c) 2009-2015, netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netbout Inc. located at www.netbout.com.
 * Federal copyright law prohibits unauthorized reproduction by any means
 * and imposes fines up to $25,000 for violation. If you received
 * this code accidentally and without intent to use it, please report this
 * incident to the author by email.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */
package com.netbout.email;

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.email.Envelope;
import com.jcabi.email.Postman;
import com.jcabi.email.enclosure.EnHTML;
import com.jcabi.email.stamp.StRecipient;
import com.jcabi.email.stamp.StSubject;
import com.netbout.rest.Markdown;
import com.netbout.spi.Bout;
import com.netbout.spi.Friend;
import java.io.IOException;
import lombok.ToString;

/**
 * Email Messages.
 *
 * @author Dragan Bozanovic (bozanovicdr@gmail.com)
 * @version $Id$
 * @since 2.18
 */
@Immutable
@Loggable(Loggable.DEBUG)
@ToString(of = { "postman", "bout" })
final class EmCourier {

    /**
     * Postman.
     */
    private final transient Postman postman;

    /**
     * Bout.
     */
    private final transient Bout bout;

    /**
     * Public ctor.
     * @param pst Postman
     * @param bot Bout we're in
     */
    public EmCourier(final Postman pst, final Bout bot) {
        this.postman = pst;
        this.bout = bot;
    }

    /**
     * Send an email.
     * @param friend Friend to send to
     * @param text The text of the new message
     * @throws IOException If fails
     * @todo #692:30min/DEV Header `Reply-To` should be added to
     *  an outgoing email. Format is crypt-string@reply.netbout.com
     *  where crypt-string is user-urn|bout-number
     *  Use EmCatch.encrypt() to encrypt the string
     *  see #692 and EmActionTest for additional info
     */
    public void email(final Friend friend, final String text)
        throws IOException {
        this.postman.send(
            new Envelope.MIME()
                .with(new StRecipient(friend.alias(), friend.email()))
                .with(
                    new StSubject(
                        String.format(
                            "#%d: %s",
                            this.bout.number(),
                            this.bout.title()
                        )
                    )
                )
                .with(
                    new EnHTML(
                        Joiner.on('\n').join(
                            new Markdown(text).html(),
                            "<p>--<br/>to reply click here: ",
                            String.format(
                                "http://www.netbout.com/b/%d",
                                this.bout.number()
                            ),
                            "</p><p style=\"color:#C8C8C8;font-size:2px;\">",
                            String.format(
                                "%d</p>",
                                System.nanoTime()
                            ),
                            new GmailViewAction(this.bout.number()).xml()
                        )
                    )
                )
        );
    }
}
