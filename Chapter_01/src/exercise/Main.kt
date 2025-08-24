package exercise

fun String.capitalizeFirst(): String {
    return this.lowercase()
        .replaceFirstChar {
            it.uppercase()
        }
}

fun cap(lst: List<String?>): List<String> {
    val result = mutableListOf<String>()
    for (str in lst) {
        if (str.isNullOrEmpty()) {
            println("Skipped")
        } else {
            result.addAll(str.split(' ').filter {
                it.isNotBlank()
            }.map {
                it.capitalizeFirst()
            })
        }
    }
    return result
}

fun main() {
    val initialList = listOf("hellO wOrlD", null, "fRom", null, "kOtlin", "")
    println(cap(initialList))
}