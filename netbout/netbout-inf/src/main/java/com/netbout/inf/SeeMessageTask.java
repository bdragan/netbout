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
 * this code occasionally and without intent to use it, please report this
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

import com.netbout.spi.Message;
import com.netbout.spi.Participant;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.HashSet;
import java.util.Set;

/**
 * The task to review one message.
 *
 * <p>This class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
final class SeeMessageTask extends AbstractTask {

    /**
     * The bout.
     */
    private final transient Message message;

    /**
     * The listener.
     */
    private final transient TaskListener listener;

    /**
     * Dependants.
     */
    private final transient Set<Urn> deps = new HashSet<Urn>();

    /**
     * Public ctor.
     * @param what The message to update
     * @param store The store to use
     * @param ltr Listener of result
     */
    public SeeMessageTask(final Message what, final Store store,
        final TaskListener ltr) {
        super(store);
        this.message = what;
        this.listener = ltr;
        for (Participant dude : what.bout().participants()) {
            this.deps.add(dude.identity().name());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Urn> dependants() {
        return this.deps;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("see-message-#%d", this.message.number());
    }

    /**
     * {@inheritDoc}
     *
     * <p>There is no synchronization, intentionally. Msg class is thread-safe
     * and we don't worry about concurrent changes to it.
     */
    @Override
    protected void execute() {
        this.store().see(this.message);
        this.listener.done(this.message);
        Logger.debug(
            this,
            "#execute(): cached message #%d in %[nano]s",
            this.message.number(),
            this.time()
        );
    }

}
