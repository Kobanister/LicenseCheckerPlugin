package io.github.kobanister.license.checker

import groovy.json.JsonSlurper
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class LicenseCheckerPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.create("checkLicenses") { task ->
            task.group = "license"
            task.dependsOn(project.tasks.getByName("downloadLicenses"))
            task.doLast {
                // Read a JSON file
                val jsonFile = project.file("build/reports/license/license-dependency.json")
                val fileAsJsonObject = JsonSlurper().parseText(jsonFile.readText()) as LicenseReportData

                // Get the "licenses" property as ArrayList from JSON
                val licensesList = fileAsJsonObject.licenses
                println("licenses list: ${licensesList.map { it.name }}")

                // prohibited licenses and their possible names
                val prohibitedLicenses = listOf(
                    "GNU", "GPL", "General Public License", // GPL
                    "LGPL", "Library General Public License", "Lesser General Public License", // LPGL
                    "CDDL", "Common Development and Distribution License", // CDDL
                    "MIT",
                    "EUPL", "European Union Public License" // EUPL
                )

                val listOfLicensingProblems = ArrayList<String>()
                var licenseIndex = 1

                // Find and map the licenses that are not allowed to a string representation
                licensesList.forEach {
                    prohibitedLicenses.forEach { prohibited ->
                        if (it.name.contains(prohibited)) {
                            listOfLicensingProblems.add("$licenseIndex. ${it.name}(${it.url}) in the following dependencies: ${it.dependencies}")
                            licenseIndex++
                        }
                    }
                }

                // If the list with problems is not empty throw an exception to terminate the current task
                if (!listOfLicensingProblems.isEmpty())
                    throw GradleException("Licensing error. The project contains unsupported Licenses:\n${listOfLicensingProblems.joinToString { "\n" }}")
            }
        }
    }
}
