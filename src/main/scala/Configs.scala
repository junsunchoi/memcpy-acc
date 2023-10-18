package memcpyacc

import chisel3._
import chisel3.util._
import chisel3.{Printable}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheArbiter}
import freechips.rocketchip.util.DecoupledHelper
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.tilelink._

case object QueueDepth extends Field[Int](2)
case object HyperscaleSoCTapeOut extends Field[Boolean](true)
case object CompressAccelPrintfEnable extends Field[Boolean](false)

class WithMemcpyAccel extends Config ((site, here, up) => {
  case MemcpyAccelTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val memcpyacc = LazyModule(new MemcpyAccel(OpcodeSet.custom0)(p))
      memcpyacc
    }
  )
})

class WithCompressAccelPrintf extends Config((site, here, up) => {
  case CompressAccelPrintfEnable => true
})

