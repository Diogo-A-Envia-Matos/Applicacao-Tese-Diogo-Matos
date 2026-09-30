package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanAddAndDeleteUnavailability.condition)
annotation class CanAddAndDeleteUnavailability{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.canAddAndDeleteUnavailability(authentication.principal, #unavailabilityDTO)" +
                    "or" +
                    "@SecurityService.isAdminOrOrganizer(authentication.principal)"
    }
}
