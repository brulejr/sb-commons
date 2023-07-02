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

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import java.util.UUID

/**
 * @author brulejr@gmail.com
 */
class TraceabilityRequestHeaderExtractor(
    private val datafill: TraceabilityDatafill
) : (ServerHttpRequest) -> Map<String, String> {

    override fun invoke(httpRequest: ServerHttpRequest): Map<String, String> {
        return buildMap {
            put(datafill.applicationId, datafill.applicationName)
            extract(httpRequest.headers, datafill.transactionId) { k, v -> put(k, v) }
            put(datafill.requestId, UUID.randomUUID().toString())
        }
    }

    private fun extract(headers: HttpHeaders, key: String, action: (String, String) -> Unit) {
        val value = headers[key]?.joinToString(datafill.listSeparator)
        if (value != null) {
            action.invoke(key, value)
        }
    }

}