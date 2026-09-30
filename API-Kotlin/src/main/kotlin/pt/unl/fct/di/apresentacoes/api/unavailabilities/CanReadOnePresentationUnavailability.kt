package pt.unl.fct.di.apresentacoes.api.unavailabilities

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanReadOnePresentationUnavailability.condition)
annotation class CanReadOnePresentationUnavailability{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)" +
                    "or" +
                    "@SecurityService.isOwnerOfPresentationUnavailability(authentication.principal, #puid)"
    }
}
