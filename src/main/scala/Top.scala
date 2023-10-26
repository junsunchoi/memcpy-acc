package memcpyacc

import Chisel._
import chisel3.{Printable, DontCare}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheArbiter}
import freechips.rocketchip.util.DecoupledHelper
import freechips.rocketchip.rocket.constants.MemoryOpConstants
import freechips.rocketchip.tilelink._

class MemcpyAccel(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(
    opcodes = opcodes, nPTWPorts = 2) {
  override lazy val module = new MemcpyAccelImp(this)

  val queue_depth = p(QueueDepth)

  val tapeout = p(HyperscaleSoCTapeOut)
  val roccTLNode = if (tapeout) atlNode else tlNode

  val l2_memloader =     LazyModule(new L2MemHelper("[memloader]", numOutstandingReqs=32))
  roccTLNode := TLBuffer.chainNode(1) := l2_memloader.masterNode

  val l2_memwriter =     LazyModule(new L2MemHelper("[memwriter]", numOutstandingReqs=32))
  roccTLNode := TLBuffer.chainNode(1) := l2_memwriter.masterNode
}
class MemcpyAccelImp(outer: MemcpyAccel)(implicit p: Parameters)
  extends LazyRoCCModuleImp(outer) with MemoryOpConstants {

  io.mem.req.valid := false.B
  io.mem.s1_kill := false.B
  io.mem.s2_kill := false.B
  io.mem.keep_clock_enabled := true.B
  io.interrupt := false.B
  io.busy := false.B
  io.fpu_resp.ready := true.B
  io.fpu_req.valid := false.B
  io.fpu_req.bits := DontCare

  ////////////////////////////////////////
  ///// Don't touch above this line! /////
  ////////////////////////////////////////

  val queue_depth = p(QueueDepth)

  val cmd_router = Module(new CommandRouter(queue_depth))
  cmd_router.io.rocc_in <> io.cmd
  io.resp <> cmd_router.io.rocc_out

  val memloader = Module(new MemLoader(memLoaderQueDepth=queue_depth))
  outer.l2_memloader.module.io.userif <> memloader.io.l2helperUser
  memloader.io.src_info <> cmd_router.io.src_info

  val memwriter = Module(new MemWriter32(cmd_que_depth=queue_depth))
  outer.l2_memwriter.module.io.userif <> memwriter.io.l2io

  val xerox = Module(new Xerox(256))
  xerox.io.mem_stream <> memloader.io.consumer
  memwriter.io.memwrites_in <> xerox.io.memwrites_in
  memwriter.io.decompress_dest_info <> cmd_router.io.dest_info
  cmd_router.io.bufs_completed := memwriter.io.bufs_completed
  cmd_router.io.no_writes_inflight := memwriter.io.no_writes_inflight

 // Boilerplate code for l2 mem helper
  outer.l2_memloader.module.io.sfence <> cmd_router.io.sfence_out
  outer.l2_memloader.module.io.status.valid := cmd_router.io.dmem_status_out.valid
  outer.l2_memloader.module.io.status.bits := cmd_router.io.dmem_status_out.bits.status
  io.ptw(0) <> outer.l2_memloader.module.io.ptw

  outer.l2_memwriter.module.io.sfence <> cmd_router.io.sfence_out
  outer.l2_memwriter.module.io.status.valid := cmd_router.io.dmem_status_out.valid
  outer.l2_memwriter.module.io.status.bits := cmd_router.io.dmem_status_out.bits.status
  io.ptw(1) <> outer.l2_memwriter.module.io.ptw
}
