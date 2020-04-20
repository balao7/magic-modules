package io.labs.dotanuki.magicmodules.tests.internal

import io.labs.dotanuki.magicmodules.internal.MagicModulesError
import io.labs.dotanuki.magicmodules.internal.ModuleNamesWriter
import io.labs.dotanuki.magicmodules.internal.model.CanonicalModuleName
import io.labs.dotanuki.magicmodules.internal.model.GradleModuleInclude
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

internal class ModuleNamesWriterTests {

    @get:Rule val tempFolder = TemporaryFolder()

    @Test fun `should not write on non-directory file`() {
        val target = tempFolder.newFile()

        val coordinates = mapOf(
            CanonicalModuleName("LIBRARY") to GradleModuleInclude(":library"),
            CanonicalModuleName("APP") to GradleModuleInclude(":app")
        )

        val filename = "Libraries"

        val execution = { ModuleNamesWriter.write(target, filename, coordinates) }

        val expected = MagicModulesError.CantWriteConstantsFile
        assertThatThrownBy(execution).isEqualTo(expected)
    }

    @Test fun `should not write when no names are provided`() {
        val target = tempFolder.newFolder()
        val coordinates = emptyMap<CanonicalModuleName, GradleModuleInclude>()
        val filename = "Modules"

        val execution = { ModuleNamesWriter.write(target, filename, coordinates) }

        val expected = MagicModulesError.CantAcceptModulesNames
        assertThatThrownBy(execution).isEqualTo(expected)
    }

    @Test fun `should write names as constants and also each name inside a list`() {
        val target = tempFolder.newFolder()

        val coordinates = mapOf(
            CanonicalModuleName("CORE") to GradleModuleInclude(":core"),
            CanonicalModuleName("COMMON") to GradleModuleInclude(":common")
        )

        val filename = "Modules"

        ModuleNamesWriter.write(target, filename, coordinates)

        val writtenCode = target.resolve("$filename.kt").readText()
        val expectedCode = """
            // Generated by MagicModules plugin. Please do not edit
            import kotlin.String
            import kotlin.collections.List
            
            object Modules {
                const val CORE: String = ":core"

                const val COMMON: String = ":common"

                val allAvailable: List<String> = 
                        listOf(
                            "CORE",
                            "COMMON"
                        )
            }
            
            """.trimIndent()

        assertThat(writtenCode).isEqualTo(expectedCode)
    }
}