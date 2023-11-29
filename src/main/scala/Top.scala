package memcpyacc

import chisel3._

import org.chipsalliance.cde.config.{Parameters, Field}
import freechips.rocketchip.tile._
import freechips.rocketchip.rocket.{TLBConfig}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.subsystem.{SystemBusKey}
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.tilelink._
import roccaccutils.memutils._
import roccaccutils.memstreamer._

case object MemcpyAccelTLB extends Field[Option[TLBConfig]](None)

class MemcpyAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends MemStreamerRoCC(
    opcodes = opcodes) {

  override lazy val module = new MemcpyAccelImp(this)

  require(p(SystemBusKey).beatBytes == 32, "Only tested on 32B SBUS width")

  lazy val tlbConfig = p(MemcpyAccelTLB).get
  lazy val xbarBetweenMem = p(MemcpyAccelInsertXbarBetweenMemory)
  lazy val logger = MemcpyAccelLogger
}

class MemcpyAccelImp(outer: MemcpyAccel)(implicit p: Parameters)
  extends MemStreamerRoCCImp(outer) {

  lazy val metadataQueueDepth = p(MemcpyAccelCmdQueueDepth)
  lazy val dataQueueDepth = p(MemcpyAccelDataQueueDepth)

  lazy val cmd_router = Module(new MemcpyAccelCommandRouter(metadataQueueDepth))
  lazy val streamer = Module(new Xerox(outer.logger))
}
