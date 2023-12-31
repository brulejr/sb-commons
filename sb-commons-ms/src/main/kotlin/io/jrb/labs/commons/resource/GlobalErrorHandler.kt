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

import com.google.common.base.VerifyException
import io.jrb.labs.commons.service.DuplicateEntityException
import io.jrb.labs.commons.service.InvalidEntityException
import io.jrb.labs.commons.service.PatchInvalidException
import io.jrb.labs.commons.service.EntityNotFoundException
import io.jrb.labs.commons.service.ServiceException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * @author brulejr@gmail.com
 */
@ControllerAdvice
class GlobalErrorHandler {

    @ExceptionHandler(DuplicateEntityException::class)
    fun forumException(exception: DuplicateEntityException) = ErrorResponseEntity.conflict(exception.message)

    @ExceptionHandler(InvalidEntityException::class)
    fun forumException(exception: InvalidEntityException) = ErrorResponseEntity.badRequest(exception.message)

    @ExceptionHandler(PatchInvalidException::class)
    fun forumException(exception: PatchInvalidException) = ErrorResponseEntity.badRequest(exception.message)

    @ExceptionHandler(EntityNotFoundException::class)
    fun forumException(exception: EntityNotFoundException) = ErrorResponseEntity.notFound(exception.message)

    @ExceptionHandler(ServiceException::class)
    fun forumException(exception: ServiceException) = ErrorResponseEntity.serverError(exception.message)

    @ExceptionHandler(VerifyException::class)
    fun forumException(exception: VerifyException) = ErrorResponseEntity.badRequest(exception.message)

}