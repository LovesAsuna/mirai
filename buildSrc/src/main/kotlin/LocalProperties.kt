/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.Project
import java.util.*

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

object BuildSrcRootProjectHolder {
    lateinit var value: Project
    var lastUpdateTime: Long = 0
}

val rootProject: Project get() = BuildSrcRootProjectHolder.value

fun <T> projectLazy(action: () -> T): Lazy<T> {
    val projLazy = object : Lazy<T> {
        private lateinit var delegate: Lazy<T>
        private var holdTime: Long = -1

        override val value: T
            get() {
                if (holdTime != BuildSrcRootProjectHolder.lastUpdateTime) {
                    synchronized(this) {
                        if (holdTime != BuildSrcRootProjectHolder.lastUpdateTime) {
                            delegate = lazy(action)
                            holdTime = BuildSrcRootProjectHolder.lastUpdateTime
                        }
                    }
                }
                return delegate.value
            }

        override fun isInitialized(): Boolean {
            if (!::delegate.isInitialized) return false

            if (holdTime == BuildSrcRootProjectHolder.lastUpdateTime) {
                return delegate.isInitialized()
            }
            return false
        }
    }
    return projLazy
}


private lateinit var localProperties: Properties

private fun Project.loadLocalPropertiesIfAbsent() {
    if (::localProperties.isInitialized) return
    localProperties = Properties().apply {
        rootProject.projectDir.resolve("local.properties").takeIf { it.exists() }?.bufferedReader()?.use {
            load(it)
        }
    }
}

fun Project.getLocalProperty(name: String): String? {
    loadLocalPropertiesIfAbsent()
    return localProperties.getProperty(name)
}

fun Project.getLocalProperty(name: String, default: String): String {
    return getLocalProperty(name) ?: default
}

fun Project.getLocalProperty(name: String, default: Int): Int {
    return getLocalProperty(name)?.toInt() ?: default
}

fun Project.getLocalProperty(name: String, default: Boolean): Boolean {
    return getLocalProperty(name)?.toBoolean() ?: default
}

