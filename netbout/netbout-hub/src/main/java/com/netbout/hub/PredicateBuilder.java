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
package com.netbout.hub;

import com.netbout.hub.predicates.CustomPred;
import com.netbout.spi.Urn;
import com.ymock.util.Logger;
import java.util.List;
import java.util.Map;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.apache.commons.lang.ArrayUtils;

/**
 * Builder of a predicate.
 *
 * @author Yegor Bugayenko (yegor@netbout.com)
 * @version $Id$
 */
public final class PredicateBuilder {

    /**
     * All functions and their classes.
     */
    @SuppressWarnings("PMD.UseConcurrentHashMap")
    private static final Map<String, String> FUNCS = ArrayUtils.toMap(
        new String[][] {
            {"and", "com.netbout.hub.predicates.logic.AndPred"},
            {"equal", "com.netbout.hub.predicates.math.EqualPred"},
            {"greater-than", "com.netbout.hub.predicates.math.GreaterThanPred"},
            {"less-than", "com.netbout.hub.predicates.math.LessThanPred"},
            {"matches", "com.netbout.hub.predicates.text.MatchesPred"},
            {"not", "com.netbout.hub.predicates.logic.NotPred"},
            {"ns", "com.netbout.hub.predicates.xml.NsPred"},
            {"or", "com.netbout.hub.predicates.logic.OrPred"},
            {"pos", "com.netbout.hub.predicates.PosPred"},
            {"talks-with", "com.netbout.hub.predicates.TalksWithPred"},
        }
    );

    /**
     * Hub to find custom predicates.
     */
    private final transient Hub ihub;

    /**
     * Public ctor.
     * @param hub The hub to work with
     */
    public PredicateBuilder(final Hub hub) {
        this.ihub = hub;
    }

    /**
     * Build a predicate from a query string.
     * @param query The query
     * @return The predicate
     */
    public Predicate parse(final String query) {
        Predicate predicate;
        if (!query.isEmpty() && query.charAt(0) == '(') {
            final CharStream input = new ANTLRStringStream(query);
            final QueryLexer lexer = new QueryLexer(input);
            final TokenStream tokens = new CommonTokenStream(lexer);
            final QueryParser parser = new QueryParser(tokens);
            parser.setPredicateBuilder(this);
            try {
                predicate = parser.query();
            } catch (org.antlr.runtime.RecognitionException ex) {
                throw new PredicateException(query, ex);
            } catch (PredicateException ex) {
                throw new PredicateException(query, ex);
            }
            Logger.debug(
                this,
                "#parse('%s'): predicate found: '%s'",
                query,
                predicate
            );
        } else {
            predicate = this.parse(
                String.format(
                    "(matches \"%s\" $text)",
                    query.replace("\"", "\\\"")
                )
            );
        }
        return predicate;
    }

    /**
     * Build a predicate from name and list of preds.
     * @param name Its name
     * @param preds List of arguments
     * @return The predicate
     */
    protected Predicate build(final String name, final List<Predicate> preds) {
        Predicate predicate;
        if (this.FUNCS.containsKey(name)) {
            try {
                predicate = (Predicate) Class.forName(this.FUNCS.get(name))
                    .getConstructor(List.class)
                    .newInstance(preds);
            } catch (ClassNotFoundException ex) {
                throw new PredicateException(ex);
            } catch (NoSuchMethodException ex) {
                throw new PredicateException(ex);
            } catch (InstantiationException ex) {
                throw new PredicateException(ex);
            } catch (IllegalAccessException ex) {
                throw new PredicateException(ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw new PredicateException(ex);
            }
        } else if (Urn.isValid(name)) {
            predicate = new CustomPred(this.ihub, Urn.create(name), preds);
        } else {
            throw new PredicateException(
                String.format("Unknown function '%s'", name)
            );
        }
        return predicate;
    }

}