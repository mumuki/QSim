package ar.edu.unq.tpi.qsim.arq

/**
* Copyright 2014 Tatiana Molinari.
* Copyright 2014 Susana Rosito
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import ar.edu.unq.tpi.qsim.parser.Parser
import ar.edu.unq.tpi.qsim.model._
import ar.edu.unq.tpi.qsim.utils._
import ar.edu.unq.tpi.qsim.exeptions.SyntaxErrorException
import scala.collection.mutable.Map


class CicloEjecucionArquitecturaQ5 extends FlatSpec with Matchers {

  def parsers_resultados = new {
    var path  = new PathTest()
    var programa_valido = path.getContenido("Q5", 1)
    var programa_invalido = path.getContenido("Q5", 2)
    var parser = Parser
    var resultadoQ5 = parser.ensamblarQ5(programa_valido)
  }

  def programas = new {
    var instrucciones = List(MOV(R1, new Indirecto(new Directo(new Inmediato("0010")))), CMP(new RegistroIndirecto(R2), new Inmediato("0000")), MOV(new Indirecto(new Directo(new Inmediato("0010"))), R2) )
    var programaQ5 = new Programa(instrucciones)
    var instruccionesinterpretadas = List("1858 0010", "6C80 0000", "1622 0010 ")
    var instruccionesdecodificadas = List("MOV R1, [[0x0010]]", "CMP [R2], 0x0000","MOV [[0x0010]], R2" )
  } 
  //--------------------------------------------TESTS PARSER -----------------------------------------------//

  "Un Parser" should "parsear exitosamente un programa " in {
    var set_parser = parsers_resultados
    var set_programas = programas
    assert(set_parser.resultadoQ5.equals(set_programas.programaQ5))
  }

  it should "tirar un Failure cuando parsea un programa con sintaxis invalida" in {
    var set_parser = parsers_resultados
    var set_programas = programas
    var mensaje_esperado = "Ha ocurrido un error en la linea 3 : MOV [[0x0011] , "
    val exception = intercept[SyntaxErrorException] {
      
      set_parser.parser.ensamblarQ5(set_parser.programa_invalido)
    }
    assert(exception.getMessage().equals(mensaje_esperado))
  }
  //----------------------------------------------TESTS SIMULADOR -----------------------------------------------//

    def simuladores = new {
      var parser = parsers_resultados
      var programa = parser.resultadoQ5
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
      var programa = set_parser.resultadoQ5
  
      set_simuladores.simulador.cargarProgramaYRegistros(programa, pc, set_registros.registros)
  
      // verificar que pc tiene el valor esperado
      set_simuladores.simulador.cpu.pc.hex should be(pc)
    }

  //-----------------------------------------------------EJECUCION PASO A PASO -----------------------------------------//
  it should "ejecutar el ciclo de instruccion FETCH - DECODE (Paso-a-Paso) al programa que esta cargado en la memoria " in {
    var set_simuladores = simuladores
    var set_parser = parsers_resultados
    var programa = set_parser.resultadoQ5
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
