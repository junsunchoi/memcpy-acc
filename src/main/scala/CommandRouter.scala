package memcpyacc

import chisel3._

import org.chipsalliance.cde.config.{Parameters}
import roccaccutils.memutils._
import roccaccutils.memstreamer._

class MemcpyAccelCommandRouter(val cmdQueueDepth: Int)(implicit val p: Parameters) extends MemStreamerCommandRouter {
  lazy val io = IO(new MemStreamerCmdBundle)
  io.rocc_in.ready := streaming_fire
}
