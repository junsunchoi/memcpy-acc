package memcpyacc

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.{Config, Parameters, Field}
import freechips.rocketchip.tile.{BuildRoCC, OpcodeSet}
import freechips.rocketchip.rocket.{TLBConfig}
import freechips.rocketchip.diplomacy.{LazyModule}

case object MemcpyAccelInsertXbarBetweenMemory extends Field[Boolean](true)
case object MemcpyAccelCmdQueueDepth extends Field[Int](4)
case object MemcpyAccelDataQueueDepth extends Field[Int](16)

class WithMemcpyAccel extends Config ((site, here, up) => {
  case MemcpyAccelTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val memcpyacc = LazyModule(new MemcpyAccel(OpcodeSet.custom0)(p))
      memcpyacc
    }
  )
})
