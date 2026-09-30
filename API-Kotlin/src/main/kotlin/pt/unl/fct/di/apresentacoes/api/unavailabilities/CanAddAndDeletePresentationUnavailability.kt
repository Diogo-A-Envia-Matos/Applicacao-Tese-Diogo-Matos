package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanAddAndDeletePresentationUnavailability.condition)
annotation class CanAddAndDeletePresentationUnavailability{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.canAddAndDeletePresentationUnavailability(authentication.principal,#presentationUnavailabilityDTO)" +
                    "or" +
                    "@SecurityService.isAdminOrOrganizer(authentication.principal)"
    }
}
