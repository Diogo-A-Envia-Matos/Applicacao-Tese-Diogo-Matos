package pt.unl.fct.di.apresentacoes.api.stopChanges

import org.springframework.web.bind.annotation.*

@RequestMapping("/api/stopChanges")
interface StopChangesAPI {
    
    @PostMapping("/addInitial")
    fun addInitialStopFutureChanges()

    @GetMapping("")
    fun getStateOfChangesOfRestrictions() : Boolean

    @PostMapping("")
    @CanStopAndAllowFutureChanges
    fun stopFutureChangesOfRestrictions() : Boolean

    @DeleteMapping("")
    @CanStopAndAllowFutureChanges
    fun allowFutureChangesOfRestrictions() : Boolean

}