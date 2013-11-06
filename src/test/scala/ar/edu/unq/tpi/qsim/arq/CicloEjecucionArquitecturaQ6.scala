package ar.edu.unq.tpi.qsim.arq

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import ar.edu.unq.tpi.qsim.parser.Parser
import ar.edu.unq.tpi.qsim.model._
import ar.edu.unq.tpi.qsim.utils._
import ar.edu.unq.tpi.qsim.exeptions.SyntaxErrorException
import scala.collection.mutable.Map

class CicloEjecucionArquitecturaQ6 extends FlatSpec with Matchers {

  def parsers_resultados = new {
    var parser = Parser
    var resultadoQ6 = parser.ensamblarQ6("src/main/resources/programaQ6.qsim")
  }

  def programas = new {
    var instrucciones = List(NOT(R0), AND(new RegistroIndirecto(R6), new Directo(new Inmediato("0006"))), OR(new Indirecto(new Directo(new Inmediato("0023"))), new Inmediato("0300")), AND(R5, new Directo(new Inmediato("0005"))), OR(R6, new Inmediato("0005")))
    var programaQ6 = new Programa(instrucciones)
    var instruccionesinterpretadas = List("9800 ", "4D88 0006", "5600 0023 0300", "4948 0005", "5980 0005")
    var instruccionesdecodificadas = List("NOT R0", "AND [R6], [0x0006]", "OR [[0x0023]], 0x0300","AND R5, [0x0005]", "OR R6, 0x0005")
  }
  //--------------------------------------------TESTS PARSER -----------------------------------------------//

  "Un Parser" should "parsear exitosamente un programa " in {
    var set_parser = parsers_resultados
    var set_programas = programas
    assert(set_parser.resultadoQ6.equals(set_programas.programaQ6))
  }

  it should "tirar un Failure cuando parsea un programa con sintaxis invalida" in {
    var set_parser = parsers_resultados
    var set_programas = programas
    var mensaje_esperado = "Ha ocurrido un error en la linea 1 NOT"
    val exception = intercept[SyntaxErrorException] {

      set_parser.parser.ensamblarQ6("src/main/resources/programaQ6SyntaxError.qsim")
    }
    assert(exception.getMessage().equals(mensaje_esperado))
  }
  //----------------------------------------------TESTS SIMULADOR -----------------------------------------------//

  def simuladores = new {
    var parser = parsers_resultados
    var programa = parser.resultadoQ6
    var registros_actualizar = registros_a_actualizar

    var simulador = new Simulador()
    simulador.inicializarSim()

    var simulador_con_programa = new Simulador()
    simulador_con_programa.inicializarSim()
    simulador_con_programa.cargarProgramaYRegistros(programa, "0000", registros_actualizar.registros)
  }

  def registros_a_actualizar = new {
    var registros = Map[String, W16](("R5", "0010"), ("R0", "0010"), ("R2", "9800"), ("R1", "0009"), ("R7", "0001"))
  }

  "Un Simulador" should "cargar un programa en la memoria desde la posicion que indica pc" in {
    var set_simuladores = simuladores
    var set_registros = registros_a_actualizar
    var set_parser = parsers_resultados
    var pc = "0000"
    var programa = set_parser.resultadoQ6

    set_simuladores.simulador.cargarProgramaYRegistros(programa, pc, set_registros.registros)

    // verificar que pc tiene el valor esperado
    set_simuladores.simulador.cpu.pc.hex should be(pc)
  }
  //-----------------------------------------------------EJECUCION PASO A PASO -----------------------------------------//
  it should "ejecutar el ciclo de instruccion FETCH - DECODE (Paso-a-Paso) al programa que esta cargado en la memoria " in {
    var set_simuladores = simuladores
    var set_parser = parsers_resultados
    var programa = set_parser.resultadoQ6
    var instrucciones = programas
    var count = 0
    do {
      //FETCH
      set_simuladores.simulador_con_programa.fetch()
      assert(instrucciones.instruccionesinterpretadas(count) === set_simuladores.simulador_con_programa.cpu.ir)
      //DECODE
      var decode = set_simuladores.simulador_con_programa.decode()
      assert(instrucciones.instruccionesdecodificadas(count) === decode)

      count += 1
    } while (count < programa.instrucciones.length)
  }

}