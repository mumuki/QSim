package ar.edu.unq.tpi.qsim.ArqQ1

import scala.collection.mutable.ArrayBuffer

import org.scalatest._

import ar.edu.unq.tpi.qsim.model._
import ar.edu.unq.tpi.qsim.utils._

class CicloDeEjecucionArquitecturaQ1 extends FlatSpec with Matchers {

  def simuladorInicializado =
    new {
      var simulador = new Simulador()
      simulador.inicializarSim()
    }

  def simuladorConProgramaCargado =
    new {
      var programacreado = programaCreado
      var simuladorinit = simuladorInicializado
      simuladorinit.simulador.inicializarSim()
      simuladorinit.simulador.cargarProgramaYRegistros(programacreado.programa, "0000", programacreado.registrosParaActualizar)
      var simulador = simuladorinit.simulador
    }

  def programaCreado =
    new {
      var instrucciones = new ArrayBuffer[Instruccion]()
      instrucciones.append(ADD(R0, new Inmediato("0002")), MUL(R4, new Inmediato("0001")),
        SUB(R5, new Inmediato("000A")), MOV(R5, new Inmediato("0005")),
        MOV(R2, R3), ADD(R1, R7))

      var programa = new Programa(instrucciones)
      var registrosParaActualizar = Map[String, W16](("R5", "0010"), ("R0", "0010"), ("R2", "9800"),
        ("R1", "0009"), ("R7", "0001"))
    }

  "Un Simulador" should "inicializar la cpu y la memoria cuando se crea" in {
    var ci = simuladorInicializado
    ci.simulador.cpu should be(CPU())
    ci.simulador.memoria should be(Memoria(25))
  }

  it should "cargar un programa en la memoria desde la posicion que indica pc y actualizar los registros de cpu" in {
    var f = simuladorInicializado
    var pc = programaCreado
    f.simulador.cargarProgramaYRegistros(pc.programa, "0000", pc.registrosParaActualizar)
    assert(f.simulador.cpu.pc.equals(new W16("0000")))

    println("-------------Verificar registros actualizados-----------------")

    var map = pc.registrosParaActualizar

    for {
      key ← map.keys
      value = map(key)
    } yield {
      f.simulador.cpu.registro(key) match {
        case Some(registro) ⇒ {
          println("valor del registro " + registro.valor)
          println("valor a actualizar " + value.toString)
          assert(registro.valor.equals(value))
        }
        case _ ⇒
      }
    }

  }

  it should "ejecutar un programa que se encuentra en memoria " in {
    var spc = simuladorConProgramaCargado
    var pcAnteriorAEjecucion = spc.simulador.cpu.pc
    spc.simulador.ejecucion()
    assert(pcAnteriorAEjecucion.equals(new W16("000A")))
  }
}
