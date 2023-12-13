package memcpyacc

import chisel3._
import chisel3.util._

import org.chipsalliance.cde.config.{Parameters}
import roccaccutils.logger._
import roccaccutils.memutils._
import roccaccutils.memstreamer._

class Xerox(val logger: Logger = DefaultLogger)(implicit val p: Parameters, val hp: L2MemHelperParams) extends MemStreamer {
  lazy val io = IO(new MemStreamerBundle)

  io.mem_stream.req.bits := io.mem_stream.output_bytes_avail
  store_data_queue.io.enq.bits.chunk_size_bytes := io.mem_stream.output_bytes_avail
  store_data_queue.io.enq.bits.chunk_data := io.mem_stream.output_data
  store_data_queue.io.enq.bits.is_final_chunk := io.mem_stream.output_last_chunk

  // a bit counterintuitive (mem_stream.ready means mem_stream.req.bits bytes are ready to read out fron mem_stream.output_data)
  store_data_queue.io.enq.valid := io.mem_stream.req.ready
  io.mem_stream.req.valid := store_data_queue.io.enq.ready
}
