package pt.unl.fct.di.apresentacoes.api.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

private const val COMPUTATION_IN_PROGRESS = "There is a computation in progress."
private const val CHANGES_NOT_ALLOWED = "Currently, changes are not allowed to be performed."

@ResponseStatus(code = HttpStatus.NOT_FOUND)
class APINotFoundException(message: String? = "NOT FOUND") : Exception(message)

@ResponseStatus(code = HttpStatus.CONFLICT)
class APIConflictException(message: String? = "CONFLICT") : Exception(message)

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
class APIUnprocessableEntityException(message: String? = "UNPROCESSABLE ENTITY") : Exception(message)

@ResponseStatus(code = HttpStatus.CONFLICT)
class ComputationInProgress(message: String? = COMPUTATION_IN_PROGRESS) : RuntimeException(message)

@ResponseStatus(code = HttpStatus.CONFLICT)
class ChangesNotAllowed(message: String? = CHANGES_NOT_ALLOWED) : RuntimeException(message)

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
class PresentationUnprocessable(message: String): RuntimeException(message)