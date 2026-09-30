package pt.unl.fct.di.apresentacoes.api.presentations

import org.intellij.lang.annotations.Language
import org.springframework.security.access.prepost.PreAuthorize
import java.lang.annotation.Inherited

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@MustBeDocumented
@PreAuthorize(CanAddAndDeleteOptional1.condition)
annotation class CanAddAndDeleteOptional1{
    companion object{
        @Language("SpEL")
        const val condition: String =
            "@SecurityService.isAdminOrOrganizer(authentication.principal)" +
                    "or" +
                    "@SecurityService.isAdviserOrArguerOfPresentation(authentication.principal,#pid)"
    }
}
