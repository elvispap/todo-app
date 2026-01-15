package com.tradebyte.todoapp

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.library.Architectures
import com.tngtech.archunit.library.Architectures.onionArchitecture

@AnalyzeClasses(
    packagesOf = [ArchitectureTests::class],
    importOptions = [ImportOption.DoNotIncludeTests::class]
)
internal class ArchitectureTests {

    @ArchTest
    @JvmField
    @Suppress("VariableNaming")
    val `the hexagonal architecture is intact`: Architectures.OnionArchitecture =
        onionArchitecture()
            .domainModels(
                "$DOMAIN_PACKAGE.model.."
            )
            .domainServices(
                "$DOMAIN_PACKAGE.service.."
            )
            .adapter(
                "rest",
                "$ADAPTER_PACKAGE.rest.."
            )
            .adapter(
                "rest",
                "$ADAPTER_PACKAGE.rest.."
            )
            .adapter(
                "persistence",
                "$ADAPTER_PACKAGE.persistence.."
            )
            .applicationServices("$APPLICATION_PACKAGE..")

    companion object {
        private const val BASE_PACKAGE = "com.tradebyte.todoapp"
        private const val DOMAIN_PACKAGE = "$BASE_PACKAGE.domain"
        private const val APPLICATION_PACKAGE = "$BASE_PACKAGE.application"
        private const val ADAPTER_PACKAGE = "$BASE_PACKAGE.adapter"
    }
}
