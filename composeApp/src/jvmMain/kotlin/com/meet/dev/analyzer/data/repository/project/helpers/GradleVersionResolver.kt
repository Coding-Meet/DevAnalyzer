package com.meet.dev.analyzer.data.repository.project.helpers

import com.meet.dev.analyzer.data.models.project.ModuleBuildFileInfo

class GradleVersionResolver {
    private val extVars = mutableMapOf<String, String>()

    fun collectExtVariables(moduleBuildFileInfos: List<ModuleBuildFileInfo>) {
        val extRegex = Regex("""ext\.([a-zA-Z0-9_]+)\s*=\s*['"]([^'"]+)['"]""")
        val extBlockRegex = Regex("""ext\s*\{([^}]+)\}""", RegexOption.DOT_MATCHES_ALL)
        val varAssignRegex = Regex("""([a-zA-Z0-9_]+)\s*=\s*['"]([^'"]+)['"]""")

        moduleBuildFileInfos.forEach { fileInfo ->
            // Simple ext.var = 'val'
            extRegex.findAll(fileInfo.content).forEach { match ->
                extVars[match.groupValues[1]] = match.groupValues[2]
            }

            // ext { ... } block
            extBlockRegex.findAll(fileInfo.content).forEach { blockMatch ->
                val blockContent = blockMatch.groupValues[1]
                varAssignRegex.findAll(blockContent).forEach { match ->
                    extVars[match.groupValues[1]] = match.groupValues[2]
                }
            }
        }
    }

    fun resolve(version: String?): String? {
        if (version == null) return null
        
        // Handle $variable or ${variable}
        if (version.contains("$")) {
            var resolved = version
            val varRegex = Regex("""\$([a-zA-Z0-9_]+)|\$\{([a-zA-Z0-9_]+)\}""")
            varRegex.findAll(version).forEach { match ->
                val varName = match.groupValues[1].ifEmpty { match.groupValues[2] }
                extVars[varName]?.let {
                    resolved = resolved.replace(match.value, it)
                }
            }
            return resolved
        }
        
        // Handle plain variable name if it's exactly a variable (common in Groovy)
        return extVars[version] ?: version
    }
}
