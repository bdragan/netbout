/**
 * Copyright (c) 2009-2011, netBout.com
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
package com.netbout.hub.data;

import com.netbout.bus.Bus;
import com.ymock.util.Logger;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Manager of all bouts.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class BoutMgr {

    /**
     * All bouts existing in the system.
     */
    private final transient ConcurrentMap<Long, BoutData> bouts =
        new ConcurrentHashMap<Long, BoutData>();

    /**
     * Bus to work with.
     */
    private final transient Bus bus;

    /**
     * Public ctor.
     * @param ibus The bus
     */
    public BoutMgr(final Bus ibus) {
        this.bus = ibus;
    }

    /**
     * Create statistics in the given XML document and return their element.
     * @param doc The document to work in
     * @return The element just created
     */
    public Element stats(final Document doc) {
        final Element root = doc.createElement("manager");
        final Element total = doc.createElement("total");
        total.appendChild(
            doc.createTextNode(String.valueOf(this.bouts.size()))
        );
        root.appendChild(total);
        return root;
    }

    /**
     * Create new bout.
     * @return It's number (unique)
     */
    public Long create() {
        BoutData data;
        synchronized (this.bouts) {
            final Long number = this.bus
                .make("get-next-bout-number")
                .synchronously()
                .asDefault(this.defaultNextBoutNumber())
                .exec();
            data = new BoutData(this.bus, number);
            this.bouts.put(data.getNumber(), data);
        }
        data.setTitle("");
        this.bus.make("started-new-bout")
            .asap()
            .arg(data.getNumber())
            .asDefault(true)
            .exec();
        Logger.debug(
            this,
            "#create(): bout #%d created",
            data.getNumber()
        );
        return data.getNumber();
    }

    /**
     * Find and return bout from collection.
     * @param number Number of the bout
     * @return The bout found or restored
     * @throws BoutMissedException If this bout is not found
     */
    public BoutData find(final Long number) throws BoutMissedException {
        assert number != null;
        BoutData data;
        if (this.bouts.containsKey(number)) {
            data = this.bouts.get(number);
            Logger.debug(
                this,
                "#find(#%d): bout data found",
                number
            );
        } else {
            final Boolean exists = this.bus
                .make("check-bout-existence")
                .synchronously()
                .arg(number)
                .asDefault(false)
                .exec();
            if (!exists) {
                throw new BoutMissedException(number);
            }
            data = new BoutData(this.bus, number);
            this.bouts.put(number, data);
            Logger.debug(
                this,
                "#find(#%d): bout data restored",
                number
            );
        }
        return data;
    }

    /**
     * Get next bout number, which is by default.
     * @return The number
     */
    private Long defaultNextBoutNumber() {
        Long number;
        if (this.bouts.isEmpty()) {
            number = 1L;
        } else {
            number = Collections.max(this.bouts.keySet()) + 1;
        }
        return number;
    }

}
