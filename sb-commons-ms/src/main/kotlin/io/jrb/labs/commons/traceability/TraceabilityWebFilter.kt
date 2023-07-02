/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2023 Jon Brule <brulejr@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.jrb.labs.commons.traceability

import mu.KotlinLogging
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Provides a web filter that adds traceability data to reactive web service responses.
 *
 * @author brulejr@gmail.com
 */
class TraceabilityWebFilter(
    private val datafill: TraceabilityDatafill,
    private val requestHeaderExtractor: TraceabilityRequestHeaderExtractor,
    private val responseHeaderCompositor: TraceabilityResponseHeaderCompositor
) : WebFilter {

    private val log = KotlinLogging.logger {}

    /**
     * Wraps the given reactive Web request in a block that adds traceability data, including a unique request identifier
     * and duration, before delegating to the next {@code WebFilter} through the given {@link WebFilterChain}.
     *
     * Note that this WebFilter adds a {@code beforeCommit()} hook to the response in order to capture the duration and
     * inject it into the response headers after the entire chain has completed. It is important to note that using the
     * {@link Mono#doFinally(Consumer)} block does not work as the response is already committed and the response
     * headers are readonly at that point.
     *
     * @param exchange the current server exchange
     * @param chain provides a way to delegate to the next filter
     * @return {@code Mono<Void>} to indicate when request processing is complete
     */
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val traceData: MutableMap<String, String> = requestHeaderExtractor.invoke(exchange.request).toMutableMap()
        log.debug("inbound traceData = {}", traceData)

        val startTime = System.currentTimeMillis()
        exchange.response.beforeCommit {

            // calculate duration
            val duration = System.currentTimeMillis() - startTime
            traceData[datafill.duration] = duration.toString()
            // traceData..put(datafill.duration, duration.toString())

            // assemble response headers
            responseHeaderCompositor.invoke(traceData, exchange.response)

            Mono.empty()
        }

        return chain.filter(exchange)
    }

}