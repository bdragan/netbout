/**
 * Copyright (c) 2009-2012, Netbout.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are PROHIBITED without prior written permission from
 * the author. This product may NOT be used anywhere and on any computer
 * except the server platform of netBout Inc. located at www.netbout.com.
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
package com.netbout.inf;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Mocker of {@link Term}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class TermMocker {

    /**
     * The object.
     */
    private final transient Term term = Mockito.mock(Term.class);

    /**
     * Always shift to this msg.
     * @param msg The msg to shift to
     * @return This object
     */
    public TermMocker shiftTo(final long msg) {
        return this.shiftTo(new CursorMocker().withMsg(msg).mock());
    }

    /**
     * Always shift to this msg.
     * @param cursor The cursor to shift to
     * @return This object
     */
    public TermMocker shiftTo(final Cursor cursor) {
        Mockito.doAnswer(
            new Answer<Cursor>() {
                public Cursor answer(final InvocationOnMock invocation) {
                    return cursor;
                }
            }
        ).when(this.term).shift(Mockito.any(Cursor.class));
        return this;
    }

    /**
     * Build it.
     * @return The term
     */
    public Term mock() {
        return this.term;
    }

}
