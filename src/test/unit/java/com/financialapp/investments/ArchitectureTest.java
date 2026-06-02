package com.financialapp.investments;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.financialapp.investments", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    // --- domain purity (spec §14) ---

    @ArchTest
    static final ArchRule domain_must_not_import_spring =
            noClasses().that().resideInAPackage("..domain..")
                    .should().accessClassesThat().resideInAPackage("org.springframework..")
                    .because("domain layer must be framework-agnostic (spec §14)");

    @ArchTest
    static final ArchRule domain_must_not_import_jpa =
            noClasses().that().resideInAPackage("..domain..")
                    .should().accessClassesThat().resideInAPackage("jakarta.persistence..")
                    .because("domain layer must be persistence-agnostic");

    @ArchTest
    static final ArchRule domain_must_not_import_lombok =
            noClasses().that().resideInAPackage("..domain..")
                    .should().accessClassesThat().resideInAPackage("lombok..")
                    .because("domain layer uses plain Java records, no Lombok");

    @ArchTest
    static final ArchRule domain_must_not_import_kafka =
            noClasses().that().resideInAPackage("..domain..")
                    .should().accessClassesThat().resideInAPackage("org.apache.kafka..")
                    .because("domain layer must not depend on messaging infrastructure");

    // --- application purity (spec §14) ---

    @ArchTest
    static final ArchRule application_must_not_import_jpa =
            noClasses().that().resideInAPackage("..application..")
                    .should().accessClassesThat().resideInAPackage("jakarta.persistence..")
                    .because("application layer must not depend on persistence infrastructure");

    @ArchTest
    static final ArchRule application_must_not_import_kafka =
            noClasses().that().resideInAPackage("..application..")
                    .should().accessClassesThat().resideInAPackage("org.apache.kafka..")
                    .because("application layer must not depend on messaging infrastructure");

    @ArchTest
    static final ArchRule application_must_not_import_spring_kafka =
            noClasses().that().resideInAPackage("..application..")
                    .should().accessClassesThat().resideInAPackage("org.springframework.kafka..")
                    .because("application layer must not depend on messaging infrastructure");

    @ArchTest
    static final ArchRule application_must_not_import_spring_data =
            noClasses().that().resideInAPackage("..application..")
                    .should().accessClassesThat().resideInAPackage("org.springframework.data..")
                    .because("pagination must use the domain PageRequest/PageResult abstraction (spec §14)");

    // --- layering ---

    @ArchTest
    static final ArchRule web_must_not_import_infrastructure_internals =
            noClasses().that().resideInAPackage("..web..")
                    .should().accessClassesThat().resideInAPackage("..infrastructure.persistence..")
                    .because("web layer must depend on use cases, not persistence adapters directly");

    @ArchTest
    static final ArchRule infrastructure_must_not_import_web =
            noClasses().that().resideInAPackage("..infrastructure..")
                    .should().accessClassesThat().resideInAPackage("com.financialapp.investments.web..")
                    .because("infrastructure must not depend on web layer");

    // --- structural placement (spec §16, §26) ---

    @ArchTest
    static final ArchRule use_case_interfaces_reside_in_domain_usecase =
            classes().that().haveSimpleNameEndingWith("UseCase").and().areInterfaces()
                    .should().resideInAPackage("..domain.usecase..")
                    .because("use-case interfaces are domain contracts (spec §16)");

    @ArchTest
    static final ArchRule use_case_impls_reside_in_application =
            classes().that().haveSimpleNameEndingWith("UseCaseImpl")
                    .should().resideInAPackage("..application..impl..")
                    .because("use-case orchestration lives in application/<agg>/impl (spec §16)");

    @ArchTest
    static final ArchRule commands_reside_in_domain_usecase =
            classes().that().haveSimpleNameEndingWith("Command")
                    .should().resideInAPackage("..domain.usecase..")
                    .because("commands are domain use-case inputs (spec §16.2)");

    @ArchTest
    static final ArchRule controllers_reside_in_web =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..web..")
                    .because("controllers live in the web layer (spec §26)");
}
