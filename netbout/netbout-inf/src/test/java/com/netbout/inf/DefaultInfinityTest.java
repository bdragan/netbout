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

import com.jcabi.log.VerboseThreads;
import com.netbout.inf.notices.MessagePostedNotice;
import com.netbout.spi.Bout;
import com.netbout.spi.BoutMocker;
import com.netbout.spi.Message;
import com.netbout.spi.MessageMocker;
import com.netbout.spi.UrnMocker;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link DefaultInfinity}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle MagicNumber (500 lines)
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.DoNotUseThreads")
public final class DefaultInfinityTest {

    /**
     * DefaultInfinity can find messages.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsMessageJustPosted() throws Exception {
        final Infinity inf = new DefaultInfinity(new FolderMocker().mock());
        final Bout bout = new BoutMocker()
            .withParticipant(new UrnMocker().mock())
            .mock();
        final Message msg = new MessageMocker()
            .withText("some text to index")
            .withNumber(MsgMocker.number())
            .inBout(bout)
            .mock();
        InfinityMocker.waitFor(
            inf,
            inf.see(
                new MessagePostedNotice() {
                    @Override
                    public Message message() {
                        return msg;
                    }
                }
            )
        );
        final String query = String.format(
            "(and (equal $number %d) (pos 0))",
            msg.number()
        );
        MatcherAssert.assertThat(
            inf.messages(query),
            Matchers.<Long>iterableWithSize(1)
        );
        inf.close();
    }

    /**
     * DefaultInfinity can restore its state from files.
     * @throws Exception If there is some problem inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void restoresItselfFromFileSystem() throws Exception {
        final Folder folder = new FolderMocker().mock();
        final Infinity inf = new DefaultInfinity(folder);
        final int total = 100;
        final ExecutorService svc = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(),
            new VerboseThreads()
        );
        final AtomicInteger added = new AtomicInteger();
        final long start = MsgMocker.number();
        for (int pos = 0; pos < total; ++pos) {
            final long number = start + pos;
            svc.submit(
                new Runnable() {
                    @Override
                    public void run() {
                        final Message message = new MessageMocker()
                            .withText("about Jeffrey")
                            .withNumber(number)
                            .mock();
                        inf.see(
                            new MessagePostedNotice() {
                                @Override
                                public Message message() {
                                    return message;
                                }
                            }
                        );
                        added.incrementAndGet();
                    }
                }
            );
        }
        svc.shutdown();
        svc.awaitTermination(5, TimeUnit.SECONDS);
        inf.close();
        for (int attempt = 0; attempt <= 2; ++attempt) {
            final Infinity restored = new DefaultInfinity(folder);
            InfinityMocker.waitFor(restored);
            MatcherAssert.assertThat(
                restored.messages("(matches 'Jeffrey')"),
                Matchers.<Long>iterableWithSize(added.get())
            );
            restored.close();
        }
    }

    /**
     * DefaultInfinity can convert itselt to string.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void convertsItseltToString() throws Exception {
        MatcherAssert.assertThat(
            new DefaultInfinity(new FolderMocker().mock()),
            Matchers.hasToString(Matchers.notNullValue())
        );
    }

    /**
     * DefaultInfinity can search in parallel threads.
     * @throws Exception If there is some problem inside
     * @checkstyle ExecutableStatementCount (100 lines)
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void searchesInParallelThreads() throws Exception {
        final Folder folder = new FolderMocker().mock();
        final Infinity inf = new DefaultInfinity(folder);
        for (int num = 0; num < 10; ++num) {
            final Bout bout = new BoutMocker()
                .withParticipant(new UrnMocker().mock())
                .withNumber(MsgMocker.number())
                .mock();
            final Message msg = new MessageMocker()
                .withText("Jeffrey Lebowski, \u0443\u0440\u0430! What's up?")
                .withNumber(MsgMocker.number())
                .inBout(bout)
                .mock();
            inf.see(
                new MessagePostedNotice() {
                    @Override
                    public Message message() {
                        return msg;
                    }
                }
            );
        }
        InfinityMocker.waitFor(inf);
        final int threads = 10;
        final CountDownLatch start = new CountDownLatch(1);
        final CountDownLatch latch = new CountDownLatch(threads);
        final Callable<?> task = new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                start.await();
                for (int attempt = 0; attempt < 10; ++attempt) {
                    MatcherAssert.assertThat(
                        inf.messages("(and (matches 'Jeffrey') (bundled))"),
                        Matchers.<Long>iterableWithSize(Matchers.greaterThan(0))
                    );
                }
                latch.countDown();
                return null;
            }
        };
        final ExecutorService svc =
            Executors.newFixedThreadPool(threads, new VerboseThreads());
        for (int thread = 0; thread < threads; ++thread) {
            svc.submit(task);
        }
        start.countDown();
        latch.await(1, TimeUnit.SECONDS);
        svc.shutdown();
        inf.close();
    }

}
