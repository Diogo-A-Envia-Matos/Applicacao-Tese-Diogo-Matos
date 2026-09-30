package pt.unl.fct.di.apresentacoes.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.ExceptionHandler

import pt.unl.fct.di.apresentacoes.api.exceptions.ComputationInProgress
import pt.unl.fct.di.apresentacoes.api.exceptions.ChangesNotAllowed
import pt.unl.fct.di.apresentacoes.api.exceptions.PresentationUnprocessable

@RestControllerAdvice
class CustomExceptionHandler {
    
    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ExceptionHandler(value = [(ComputationInProgress::class)])
    fun handleComputationInProgress(e: ComputationInProgress) : String {
        val messageCopy: String? = e.message
        if (messageCopy === null) {
            return ""
        } else {
            return messageCopy
        }
    }

    @ResponseStatus(code = HttpStatus.CONFLICT)
    @ExceptionHandler(value = [(ChangesNotAllowed::class)])
    fun handleChangesNotAllowed(e: ChangesNotAllowed) : String {
        val messageCopy: String? = e.message
        if (messageCopy === null) {
            return ""
        } else {
            return messageCopy
        }
    }

    @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(value = [(PresentationUnprocessable::class)])
    fun handleChangesNotAllowed(e: PresentationUnprocessable) : String {
        val messageCopy: String? = e.message
        if (messageCopy === null) {
            return ""
        } else {
            return messageCopy
        }
    }
}