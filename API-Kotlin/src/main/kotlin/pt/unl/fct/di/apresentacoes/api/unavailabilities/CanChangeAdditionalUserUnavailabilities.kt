package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanChangeAdditionalUserUnavailabilities.condition)
annotation class CanChangeAdditionalUserUnavailabilities{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.canChangeAdditionalUserUnavailabilities(authentication.principal, #unavailabilityDTO)" +
                    "and" +
                    "@SecurityService.isAdminOrOrganizer(authentication.principal)"
    }
}
