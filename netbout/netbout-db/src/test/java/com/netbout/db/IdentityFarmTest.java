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
package com.netbout.db;

import com.netbout.spi.Urn;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case of {@link IdentityFarm}.
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
public final class IdentityFarmTest {

    /**
     * Farm to work with.
     */
    private final transient IdentityFarm farm = new IdentityFarm();

    /**
     * IdentityFarm can change photo of identity.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void changesIdentityPhoto() throws Exception {
        final Urn identity = new IdentityRowMocker().mock();
        final URL photo = new URL("http://localhost/img.png");
        this.farm.changedIdentityPhoto(identity, photo);
        MatcherAssert.assertThat(
            this.farm.getIdentityPhoto(identity),
            Matchers.equalTo(photo)
        );
    }

    /**
     * IdentityFarm can find identities by their aliases.
     * @throws Exception If there is some problem inside
     */
    @Test
    @org.junit.Ignore
    public void findsIdentitiesByTheirAliases() throws Exception {
        final Urn who = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().mock();
        new ParticipantRowMocker(bout).namedAs(who).mock();
        final Urn identity = new IdentityRowMocker()
            .withAlias("martin.fowler@example.com")
            .withAlias("Martin Fowler")
            .withAlias("marty")
            .withAlias("\u0443\u0440\u0430!")
            .mock();
        new ParticipantRowMocker(bout).namedAs(identity).mock();
        final String[] keywords = new String[] {
            "martin",
            "@example.com",
            "Fowler",
            "martin fowler",
            "\u0443\u0440\u0430",
        };
        for (String keyword : keywords) {
            MatcherAssert.assertThat(
                this.farm.findIdentitiesByKeyword(who, keyword),
                Matchers.hasItem(identity)
            );
        }
    }

    /**
     * IdentityFarm can find identities by keyword using their names.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentitiesByTheirNames() throws Exception {
        final Urn who = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().mock();
        new ParticipantRowMocker(bout).namedAs(who).mock();
        final Urn identity = new IdentityRowMocker()
            .namedAs("urn:test:test%40example%2Ecom")
            .withAlias("test@example.com")
            .mock();
        new ParticipantRowMocker(bout).namedAs(identity).mock();
        final String[] keywords = new String[] {
            "test",
            "@example",
        };
        for (String keyword : keywords) {
            MatcherAssert.assertThat(
                this.farm.findIdentitiesByKeyword(who, keyword),
                Matchers.describedAs(keyword, Matchers.hasItem(identity))
            );
        }
    }

    /**
     * IdentityFarm can find identities by exact name.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsIdentityByExactName() throws Exception {
        final Urn identity = new IdentityRowMocker().mock();
        MatcherAssert.assertThat(
            this.farm.findIdentitiesByKeyword(
                new Urn("urn:foo:absent-identity"), identity.toString()
            ),
            Matchers.hasItem(identity)
        );
    }

    /**
     * IdentityFarm can exclude non-facebook and non-test identities.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void excludeNonObviousIdentities() throws Exception {
        final Urn who = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().mock();
        new ParticipantRowMocker(bout).namedAs(who).mock();
        final Urn identity = new IdentityRowMocker()
            .namedAs("urn:netbout:hh")
            .withAlias("freeDOM")
            .mock();
        new ParticipantRowMocker(bout).namedAs(identity).mock();
        MatcherAssert.assertThat(
            this.farm.findIdentitiesByKeyword(who, "DOM"),
            Matchers.nullValue()
        );
    }

    /**
     * IdentityFarm can join identities.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void joinsIdentityOnDemand() throws Exception {
        final Urn main = new IdentityRowMocker().mock();
        final Urn child = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().withParticipant(child).mock();
        new ParticipantRowMocker(bout).namedAs(main).mock();
        final String alias = "the-same-alias-for-both-identities";
        new AliasRowMocker(main).namedAs(alias).mock();
        new AliasRowMocker(child).namedAs(alias).mock();
        new AliasRowMocker(child).mock();
        final Long msg = new MessageRowMocker(bout).mock();
        new SeenRowMocker(msg, main).mock();
        new SeenRowMocker(msg, child).mock();
        this.farm.identitiesJoined(main, child);
    }

    /**
     * IdentityFarm can find all silent identities.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void findsSilentIdentities() throws Exception {
        final Urn who = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().withParticipant(who).mock();
        final Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -1);
        new MessageRowMocker(bout)
            .withDate(cal.getTime())
            .withAuthor(who)
            .mock();
        MatcherAssert.assertThat(
            this.farm.findSilentIdentities(),
            Matchers.hasItem(who)
        );
        MatcherAssert.assertThat(
            this.farm.getSilenceMarker(who),
            Matchers.notNullValue()
        );
    }

    /**
     * IdentityFarm can return empty list if there are no silent identities.
     * @throws Exception If there is some problem inside
     */
    @Test
    public void emptyListIfNoSilentIdentities() throws Exception {
        final Urn who = new IdentityRowMocker().mock();
        final Long bout = new BoutRowMocker().withParticipant(who).mock();
        final Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MINUTE, -1);
        new MessageRowMocker(bout)
            .withDate(cal.getTime())
            .withAuthor(who)
            .mock();
        MatcherAssert.assertThat(
            this.farm.findSilentIdentities(),
            Matchers.not(Matchers.hasItem(who))
        );
        MatcherAssert.assertThat(
            this.farm.getSilenceMarker(who),
            Matchers.equalTo("")
        );
    }

}
