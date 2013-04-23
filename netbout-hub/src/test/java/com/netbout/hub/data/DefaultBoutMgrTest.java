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
package com.netbout.hub.data;

import com.jcabi.urn.URNMocker;
import com.netbout.bus.Bus;
import com.netbout.bus.BusMocker;
import com.netbout.hub.BoutMgr;
import com.netbout.hub.DefaultHub;
import com.netbout.hub.PowerHub;
import com.netbout.hub.PowerHubMocker;
import com.netbout.inf.InfinityMocker;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultBoutMgr}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class DefaultBoutMgrTest {

    /**
     * DefaultBoutMgr can produce stats.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void producesStatistics() throws Exception {
        MatcherAssert.assertThat(
            new DefaultBoutMgr(new PowerHubMocker().mock()).statistics(),
            Matchers.notNullValue()
        );
    }

    /**
     * DefaultBoutMgr can create new bout.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void createsNewBout() throws Exception {
        final PowerHub hub = new PowerHubMocker().mock();
        final BoutMgr mgr = new DefaultBoutMgr(hub);
        final Long num = mgr.create(new URNMocker().mock());
        MatcherAssert.assertThat(num, Matchers.equalTo(1L));
    }

    /**
     * DefaultBoutMgr can create new bout on top of real hub.
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void createsNewBoutWithRealHub() throws Exception {
        final Bus bus = new BusMocker().mock();
        final BoutMgr mgr = new DefaultBoutMgr(
            new DefaultHub(bus, new InfinityMocker().mock())
        );
        final Long first = mgr.create(new URNMocker().mock());
        final Long second = mgr.create(new URNMocker().mock());
        MatcherAssert.assertThat(first, Matchers.not(Matchers.equalTo(second)));
    }

}