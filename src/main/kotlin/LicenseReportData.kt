package io.github.kobanister.license.checker

data class LicenseReportData(
    val licenses: List<LicenseData>
)

data class LicenseData(
    val name: String,
    val url: String?,
    val dependencies: List<String>
)
