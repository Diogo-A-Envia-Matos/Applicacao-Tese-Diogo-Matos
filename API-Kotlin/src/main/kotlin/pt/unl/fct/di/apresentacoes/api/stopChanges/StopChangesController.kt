package pt.unl.fct.di.apresentacoes.api.stopChanges

import org.springframework.web.bind.annotation.RestController
import pt.unl.fct.di.apresentacoes.api.exceptions.APIConflictException
import pt.unl.fct.di.apresentacoes.api.exceptions.APINotFoundException
import pt.unl.fct.di.apresentacoes.services.*

@RestController
class StopChangesController(val stops: StopChangesService): StopChangesAPI {

    override fun addInitialStopFutureChanges() {
        try {
            stops.addInitialStopFutureChanges()
        } catch (e:StopChangesNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun getStateOfChangesOfRestrictions() : Boolean {
        try {
            return stops.getStateOfChangesOfRestrictions()
        } catch (e:StopChangesNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun stopFutureChangesOfRestrictions() : Boolean {
        try {
            return stops.stopFutureChangesOfRestrictions()
        } catch (e:StopChangesNotFound){
            throw APINotFoundException(e.message)
        }
    }

    override fun allowFutureChangesOfRestrictions() : Boolean {
        try {
            return stops.allowFutureChangesOfRestrictions()
        } catch (e:StopChangesNotFound){
            throw APINotFoundException(e.message)
        }
    }
}