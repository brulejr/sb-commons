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
package io.jrb.labs.commons.resource

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.time.Instant

/**
 * @author brulejr@gmail.com
 */
data class ErrorResponse(
    val status: HttpStatus,
    val code: Int,
    val message: String?,
    val timestamp: Instant,
    val bindingErrors: List<String>
) {

    constructor(status: HttpStatus, message: String?, bindingErrors: List<String>) :
            this(status, status.value(), message, Instant.now(), bindingErrors)

    constructor(status: HttpStatus, message: String?) :
            this(status, status.value(), message, Instant.now(), ArrayList<String>())

}

class ErrorResponseEntity(body: ErrorResponse) : ResponseEntity<ErrorResponse>(body, body.status) {

    companion object {

        fun conflict(message: String?) =
            ErrorResponseEntity(ErrorResponse(HttpStatus.CONFLICT, message))

        fun badRequest(message: String?) =
            ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message))

        fun badRequest(message: String?, bindingErrors: List<String>) =
            ErrorResponseEntity(ErrorResponse(HttpStatus.BAD_REQUEST, message, bindingErrors))

        fun notFound(message: String?) =
            ErrorResponseEntity(ErrorResponse(HttpStatus.NOT_FOUND, message))

        fun serverError(message: String?) =
            ErrorResponseEntity(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message))

    }

}