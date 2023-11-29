package memcpyacc

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.{Parameters, Field}
import midas.targetutils.{SynthesizePrintf}
import roccaccutils._
import roccaccutils.logger._

case object MemcpyAccelPrintfSynth extends Field[Boolean](true)

object MemcpyAccelLogger extends Logger {
  // just print info msgs
  def logInfoImplPrintWrapper(printf: chisel3.printf.Printf)(implicit p: Parameters = Parameters.empty): chisel3.printf.Printf = {
    printf
  }

  // optionally synthesize critical msgs
  def logCriticalImplPrintWrapper(printf: chisel3.printf.Printf)(implicit p: Parameters = Parameters.empty): chisel3.printf.Printf = {
    if (p(MemcpyAccelPrintfSynth)) {
      SynthesizePrintf(printf)
    } else {
      printf
    }
  }
}
