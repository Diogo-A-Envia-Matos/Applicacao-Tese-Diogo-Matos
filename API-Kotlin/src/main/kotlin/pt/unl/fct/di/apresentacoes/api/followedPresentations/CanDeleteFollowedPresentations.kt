package pt.unl.fct.di.apresentacoes.api.followedPresentations

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanDeleteFollowedPresentations.condition)
annotation class CanDeleteFollowedPresentations{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)" +
                    "or" +
                    "@securityService.isOwnerOfFollowedPresentations(authentication.principal,#id)"
    }
}
