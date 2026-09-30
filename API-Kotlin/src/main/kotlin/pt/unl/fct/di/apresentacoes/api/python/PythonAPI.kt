package pt.unl.fct.di.apresentacoes.api.python

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestBody
import pt.unl.fct.di.apresentacoes.api.dto.ComputationDTO
import pt.unl.fct.di.apresentacoes.api.dto.TestRestrictionDTO

@RequestMapping("/api/python")
interface PythonAPI {

    @PostMapping("/addInitial")
    fun addInitialComputation()

    @GetMapping("/checkComputation")
    fun checkComputation() : Boolean

    @PostMapping("/changeState")
    @CanExecuteClingo
    fun changeState(@RequestBody state: ComputationDTO) : Boolean

    @PostMapping("/execute")
    @CanExecuteClingo
    fun executeClingo() : String

    @PostMapping("/test")
    @CanExecuteClingo
    fun testRestrictions() : TestRestrictionDTO

}