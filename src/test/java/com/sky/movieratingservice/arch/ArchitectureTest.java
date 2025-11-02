package com.sky.movieratingservice.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

public class ArchitectureTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.sky.movieratingservice");

    @Test
    void controllersShouldOnlyAccessServiceInterfacesStartingWithI() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(RestController.class).or().areAnnotatedWith(Controller.class)
                .should().accessClassesThat().areInterfaces()
                .andShould().accessClassesThat().haveSimpleNameStartingWith("I")
                .andShould().accessClassesThat().resideInAPackage("..service..")
                .because("Controllers should access service interfaces starting with I");
        rule.check(importedClasses);
    }

    @Test
    void controllerShouldNotAccessServiceImlementationDirectly() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(RestController.class)
                .should().accessClassesThat()
                .areNotInterfaces()
                .andShould().accessClassesThat()
                .resideInAPackage("..service..")
                .because("Controllers should not access service implementations directly");
        rule.check(importedClasses);
    }

    @Test
    void serviceImplementationsShouldBeAnnotatedWithService() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().haveSimpleNameNotStartingWith("I")
                .should().beAnnotatedWith(Service.class)
                        .because("Service implementations should not be annotated with Service");

        rule.check(importedClasses);
    }

    @Test
    void repositoriesShouldOnlyBeAccessedByServices() {
        ArchRule rule = classes()
                .that().resideInAPackage("..repository..")
                .should().onlyBeAccessed().byAnyPackage("..service..", "..mapper..", "..security..")
                .because("Repositories should only be accessed by services, mappers, or security classes");

        rule.check(importedClasses);
    }

    @Test
    void servicesShouldBeInterfacesStartingWithI() {
        ArchRule rule = classes()
                .that().resideInAPackage("..service..")
                .and().areInterfaces()
                .should().haveSimpleNameStartingWith("I")
                .because("Services should access service interfaces starting with I");

        rule.check(importedClasses);
    }

    @Test
    void controllerShouldFollowNamingConvention() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(RestController.class)
                .should().haveSimpleNameEndingWith("Controller")
                .because("Controllers should follow the naming convention ending with 'Controller'");

        rule.check(importedClasses);
    }
}

