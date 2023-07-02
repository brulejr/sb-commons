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
package io.jrb.labs.commons.service

import io.jrb.labs.commons.model.Entity
import io.jrb.labs.commons.repository.EntityRepository
import org.springframework.dao.DataIntegrityViolationException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * @author brulejr@gmail.com
 */
class CrudServiceUtilsImpl<E: Entity>(
    override val entityClass: Class<E>,
    override val repository: EntityRepository<E>,
    override val entityType: String = entityClass.simpleName
) : CrudServiceUtils<E> {

    override fun createEntity(entity: E, fnModify: (E) -> Mono<E>): Mono<E> {
        return repository.save(entity)
            .onErrorResume(DataIntegrityViolationException::class.java) {
                Mono.error(DuplicateEntityException(entityType, entity.guid))
            }
            .flatMap(fnModify)
            .onErrorResume(handleServiceError("Unexpected error when creating $entityType"))
    }

    override fun deleteEntity(guid: UUID): Mono<Void> {
        return repository.findByGuid(guid)
            .switchIfEmpty(Mono.error(EntityNotFoundException(entityType, guid)))
            .flatMap { repository.delete(it) }
            .onErrorResume(handleServiceError("Unexpected error when deleting $entityType"))
    }

    override fun findEntityByGuid(guid: UUID, fnModify: (E) -> Mono<E>): Mono<E> {
        return repository.findByGuid(guid)
            .switchIfEmpty(Mono.error(EntityNotFoundException(entityType, guid)))
            .flatMap(fnModify)
            .onErrorResume(handleServiceError("Unexpected error when finding $entityType"))
    }

    override fun listEntities(fnRetrieve: () -> Flux<E>,  fnModify: (E) -> Mono<E>): Flux<E> {
        return fnRetrieve()
            .flatMap(fnModify)
            .onErrorResume(handleServiceError("Unexpected error when retrieving $entityType"))
    }

    override fun updateEntity(guid: UUID, fnEntity: (E) -> E, fnModify: (E) -> Mono<E>): Mono<E> {
        return repository.findByGuid(guid)
            .switchIfEmpty(Mono.error(EntityNotFoundException(entityType, guid)))
            .map(fnEntity)
            .flatMap { repository.save(it) }
            .flatMap(fnModify)
            .onErrorResume(handleServiceError("Unexpected error when updating $entityType"))
    }

}
