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
package io.jrb.labs.commons.test

import kotlin.reflect.full.declaredMemberProperties
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito.mock
import java.util.regex.Pattern
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

typealias BeanPropertyMap = Map<String, Any>
typealias BeanPropertyMapOverrides = Map<String, (map: BeanPropertyMap) -> Any?>

interface BeanTestUtils : TestUtils {

    fun createBeanMapForTest(
        klass: KClass<*>,
        propsToIgnore: List<String> = listOf(),
        overrides: BeanPropertyMapOverrides = mapOf()
    ): BeanPropertyMap {
        val map: BeanPropertyMap = klass.declaredMemberProperties.filter { !propsToIgnore.contains(it.name) }.associate {
            it.name to randomTypeOrMock(it.returnType)
        }
        return map.entries.associate {
            val key = it.key
            val value = if (overrides.containsKey(key)) overrides[key]?.let { x -> x(map) } else it.value
            key to value!!
        }
    }

    fun <T : Any > createBeanFromMap(klass: KClass<T>, map: BeanPropertyMap): T {
        val cons = klass.primaryConstructor!!
        return cons.callBy(cons.parameters.associateBy({ it }, { map[it.name] }))
    }

    fun randomTypeOrMock(type: KType): Any {
        return when (val typeString = type.toString().dropLastWhile { it == '?' }) {
            "kotlin.Boolean" -> randomBoolean()
            "kotlin.Int" -> randomInt()
            "kotlin.String" -> randomString()
            "java.util.regex.Pattern" -> Pattern.compile(randomString())
            else -> mock(Class.forName(typeString))
        }
    }

    fun <T> validateBean(bean: T, map: BeanPropertyMap) {
        val beanProps = bean!!::class.declaredMemberProperties
        map.keys.forEach {
            val beanProp = beanProps.stream().filter { x -> x.name == it }.findFirst().get()
            assertThat(beanProp.getter.call(bean)).isEqualTo(map[it])
        }
    }

}