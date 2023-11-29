package memcpyacc

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.{Parameters}
import roccaccutils.logger._
import roccaccutils.memutils._
import roccaccutils.memstreamer._

class Xerox(val logger: Logger = DefaultLogger)(implicit val p: Parameters, val hp: L2MemHelperParams) extends MemStreamer {
  lazy val io = IO(new MemStreamerBundle)
  store_data_queue.io.enq <> load_data_queue.io.deq
}
