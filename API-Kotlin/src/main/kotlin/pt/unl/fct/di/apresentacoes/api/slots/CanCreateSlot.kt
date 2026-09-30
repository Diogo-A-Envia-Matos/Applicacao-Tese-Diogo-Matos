package pt.unl.fct.di.apresentacoes.api.slots

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanCreateSlot.condition)
annotation class CanCreateSlot{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)"
    }
}
