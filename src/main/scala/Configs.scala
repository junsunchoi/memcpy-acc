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

/*
class WithProtoAccel extends Config ((site, here, up) => {
  case ProtoTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val protoacc = LazyModule.apply(new ProtoAccel(OpcodeSet.custom2)(p))
      protoacc
    },
    (p: Parameters) => {
      val protoaccser = LazyModule.apply(new ProtoAccelSerializer(OpcodeSet.custom3)(p))
      protoaccser
    }
  )
})

class WithZstdCompressor extends Config ((site, here, up) => {
  case CompressAccelTLB => Some(TLBConfig(nSets = 4, nWays = 4, nSectors = 1, nSuperpageEntries = 1))
  case ZstdCompressorKey => Some(ZstdCompressorConfig(
    queDepth = 4
    ))
  case HufCompressUnrollCnt => 2
  case HufCompressDicBuilderProcessedStatBytesPerCycle => 2
  case HufCompressDicBuilderProcessedHeaderBytesPerCycle => 4
  case FSECompressDicBuilderProcessedStatBytesPerCycle => 4
  case RemoveSnappyFromMergedAccelerator => true
  case CompressAccelPrintfEnable => true
  case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val zstd_compressor = LazyModule(new ZstdCompressor(OpcodeSet.custom1)(p))
      zstd_compressor
    }
  )
})
*/
class WithCompressAccelPrintf extends Config((site, here, up) => {
  case CompressAccelPrintfEnable => true
})

