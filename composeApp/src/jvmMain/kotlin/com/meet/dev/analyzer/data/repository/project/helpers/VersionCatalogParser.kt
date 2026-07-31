package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.*
import com.meet.dev.analyzer.utility.crash_report.AppLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.akuleshov7.ktoml.Toml
import com.akuleshov7.ktoml.TomlInputConfig
import com.meet.dev.analyzer.utility.crash_report.AppLogger.tagName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.decodeFromString
import java.io.File

class VersionCatalogParser {
    private val tag = tagName(javaClass = javaClass)

    suspend fun findVersionCatalog(
        versionCatalogFileInfo: VersionCatalogFileInfo?
    ): VersionCatalog? = withContext(Dispatchers.IO) {
        if (versionCatalogFileInfo == null) return@withContext null

        @Serializable
        data class VersionPartial(
            val version: String? = null, @SerialName("ref") val ref: String? = null
        )

        @Serializable
        data class PluginPartial(
            val id: String? = null, val version: VersionPartial? = null
        )

        @Serializable
        data class LibraryPartial(
            val group: String? = null,
            @SerialName("name") val libName: String? = null,
            val module: String? = null,
            val version: VersionPartial? = null
        )

        @Serializable
        data class VersionCatalogPartial(
            val versions: Map<String, String> = emptyMap(),
            val libraries: Map<String, LibraryPartial> = emptyMap(),
            val plugins: Map<String, PluginPartial> = emptyMap()
        )

        fun parseBundlesFromToml(toml: String): Map<String, List<String>> {
            val bundles = mutableMapOf<String, MutableList<String>>()
            var currentKey: String? = null
            var insideArray = false
            val buffer = mutableListOf<String>()

            for (line in toml.lines()) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

                if (!insideArray) {
                    val match = Regex("""^([\w-]+)\s*=\s*\[""").find(trimmed)
                    if (match != null) {
                        currentKey = match.groupValues[1]
                        bundles[currentKey] = mutableListOf()
                        insideArray = true
                        buffer.clear()
                        buffer += trimmed

                        // handle inline case where [ ... ] on the same line
                        if (trimmed.contains("]")) {
                            val values =
                                trimmed.substringAfter("[").substringBeforeLast("]").split(",")
                                    .mapNotNull { value ->
                                        value.trim().removeSurrounding("\"")
                                            .takeIf { it.isNotEmpty() }
                                    }
                            bundles[currentKey]?.addAll(values)
                            insideArray = false
                            currentKey = null
                            buffer.clear()
                        }
                        continue
                    }
                } else {
                    buffer += trimmed
                    if (trimmed.contains("]")) {
                        val joined = buffer.joinToString(" ")
                        val values =
                            joined.substringAfter("[").substringBeforeLast("]").split(",")
                                .mapNotNull { value ->
                                    value.trim().removeSurrounding("\"").takeIf { it.isNotEmpty() }
                                }
                        bundles[currentKey!!]?.addAll(values)
                        insideArray = false
                        currentKey = null
                        buffer.clear()
                    }
                }
            }

            return bundles
        }

        val tomlText = versionCatalogFileInfo.content

        val partial = Toml(
            inputConfig = TomlInputConfig(ignoreUnknownNames = true)
        ).decodeFromString<VersionCatalogPartial>(tomlText)

        val bundleMap = parseBundlesFromToml(versionCatalogFileInfo.content)
        val versionMap = partial.versions

        val versionCatalog = VersionCatalog(
            versions = versionMap.map { (k, v) ->
                Version(name = k, version = v)
            },

            libraries = partial.libraries.map { (k, v) ->
                val resolvedVersion = when {
                    v.version?.version != null -> v.version.version
                    v.version?.ref != null -> versionMap[v.version.ref]
                    else -> null
                }
                val notation = when {
                    v.module != null -> v.module
                    v.group != null && v.libName != null -> "${v.group}:${v.libName}"
                    else -> k
                }
                val (group, libName) = when {
                    v.module != null -> {
                        val parts = v.module.split(":")
                        parts[0] to parts[1]
                    }

                    v.group != null && v.libName != null -> v.group to v.libName
                    else -> null to k // fallback
                }
                Library(
                    name = k,
                    group = group,
                    libName = libName,
                    version = resolvedVersion,
                    id = notation
                )
            },

            plugins = partial.plugins.map { (k, v) ->
                val resolvedVersion = when {
                    v.version?.version != null -> v.version.version
                    v.version?.ref != null -> versionMap[v.version.ref]
                    else -> null
                }
                Plugin(
                    name = k,
                    id = v.id!!,
                    version = resolvedVersion,
                    module = "",
                    configuration = "versionCatalog",
                    group = ""
                )
            },

            bundles = bundleMap.map { (k, v) ->
                Bundle(name = k, artifacts = v)
            }
        )
        AppLogger.d(tag = tag) { "Found version catalog." }
        AppLogger.i(tag = tag) {
            """
                Version Catalog:
                Versions: ${versionCatalog.versions.size}
                Libraries: ${versionCatalog.libraries.size}
                Plugins: ${versionCatalog.plugins.size}
                Bundles: ${versionCatalog.bundles.size}
            """.trimIndent()
        }
        AppLogger.i(tag = tag) { "Version:" }
        versionCatalog.versions.forEach {
            AppLogger.i(tag = tag) {
                "Name: ${it.name} Version: ${it.version}"
            }
        }
        AppLogger.i(tag = tag) { "Library:" }
        versionCatalog.libraries.forEach {
            AppLogger.i(tag = tag) {
                "Name: ${it.name} Group: ${it.group} LibName: ${it.libName} Version: ${it.version} id: ${it.id}"
            }
        }
        AppLogger.i(tag = tag) { "Plugin:" }
        versionCatalog.plugins.forEach {
            AppLogger.i(tag = tag) {
                "Name: ${it.name} Id: ${it.id} Version: ${it.version} Module: ${it.module}"
            }
        }
        AppLogger.i(tag = tag) { "Bundle:" }
        versionCatalog.bundles.forEach {
            AppLogger.i(tag = tag) {
                "Name: ${it.name} Artifacts: ${it.artifacts}"
            }
        }
        versionCatalog
    }
}
