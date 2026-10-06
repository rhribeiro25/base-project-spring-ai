package com.rhribeiro.poc.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.rhribeiro.poc", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {
    @ArchTest
    static final ArchRule domainIsIndependent = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideOutsideOfPackages("..domain..", "java..");

    @ArchTest
    static final ArchRule applicationPointsInward = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideOutsideOfPackages("..application..", "..domain..", "java..");

    @ArchTest
    static final ArchRule adaptersDoNotDependOnInfrastructure = noClasses().that().resideInAPackage("..adapter..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..");
}
