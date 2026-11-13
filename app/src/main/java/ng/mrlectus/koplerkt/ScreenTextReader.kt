package ng.mrlectus.koplerkt

import android.view.accessibility.AccessibilityNodeInfo

object ScreenTextReader {

    private const val MAX_CHARS = 6_000
    private val whitespace = Regex("\\s+")

    fun read(root: AccessibilityNodeInfo?, ownPackage: String): String {
        if (root == null) return ""

        val lines = mutableListOf<String>()
        root.packageName?.toString()
            ?.takeIf { it.isNotBlank() && it != ownPackage }
            ?.let { lines += "App: $it" }
        collect(root, lines)
        return lines.joinToString("\n").take(MAX_CHARS)
    }

    private fun collect(node: AccessibilityNodeInfo, lines: MutableList<String>) {
        if (!node.isVisibleToUser) return

        labelFor(node)?.let { label ->
            if (label != lines.lastOrNull()) lines += label
        }

        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collect(child, lines)
        }
    }

    private fun labelFor(node: AccessibilityNodeInfo): String? {
        if (node.isPassword) return "[password field — content hidden]"

        if (node.isEditable) {
            val hint = node.hintText?.toString()?.trim()?.replace(whitespace, " ")
            return if (hint.isNullOrBlank()) {
                "[text field — content hidden]"
            } else {
                "[text field — content hidden] ($hint)"
            }
        }

        val text = node.text?.toString()?.trim()?.replace(whitespace, " ")
        val description = node.contentDescription?.toString()?.trim()?.replace(whitespace, " ")
        val label = when {
            !text.isNullOrEmpty() -> text
            !description.isNullOrEmpty() -> description
            else -> return null
        }

        val className = node.className?.toString().orEmpty()
        val role = when {
            className.contains("Button") -> "button"
            className.contains("CheckBox") || className.contains("Switch") -> "toggle"
            className.contains("Tab") -> "tab"
            else -> null
        }
        return if (role == null) label else "[$role] $label"
    }
}
