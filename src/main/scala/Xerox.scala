package memcpyacc

import Chisel._
import chisel3.{Printable, VecInit, dontTouch}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig}
import freechips.rocketchip.util.DecoupledHelper
import freechips.rocketchip.rocket.constants.MemoryOpConstants

class VecToWire32B extends Module{
  val io = IO(new Bundle{
    val input = Input(Vec(32, UInt(8.W)))
    val output = Output(UInt(256.W))
  })
  io.output := Cat(
    io.input(31),
    io.input(30),
    io.input(29),
    io.input(28),
    io.input(27),
    io.input(26),
    io.input(25),
    io.input(24),
    io.input(23),
    io.input(22),
    io.input(21),
    io.input(20),
    io.input(19),
    io.input(18),
    io.input(17),
    io.input(16),
    io.input(15),
    io.input(14),
    io.input(13),
    io.input(12),
    io.input(11),
    io.input(10),
    io.input(9),
    io.input(8),
    io.input(7),
    io.input(6),
    io.input(5),
    io.input(4),
    io.input(3),
    io.input(2),
    io.input(1),
    io.input(0)
  )
  /* Reverse version
  io.output := Cat(
    io.input(0),
    io.input(1),
    io.input(2),
    io.input(3),
    io.input(4),
    io.input(5),
    io.input(6),
    io.input(7),
    io.input(8),
    io.input(9),
    io.input(10),
    io.input(11),
    io.input(12),
    io.input(13),
    io.input(14),
    io.input(15),
    io.input(16),
    io.input(17),
    io.input(18),
    io.input(19),
    io.input(20),
    io.input(21),
    io.input(22),
    io.input(23),
    io.input(24),
    io.input(25),
    io.input(26),
    io.input(27),
    io.input(28),
    io.input(29),
    io.input(30),
    io.input(31)
  )
  */
}

// Receives the L2 bandwidth in bits as a parameter
class Xerox(l2bw: Int)(implicit p: Parameters) extends Module{
  val io = IO(new Bundle{
    val num_bytes = Decoupled(UInt(32.W)).flip //from CommandRouter
    val mem_stream = (new MemLoaderConsumerBundle).flip //from MemLoader
    val memwrites_in = Decoupled(new WriterBundle) //to MemWriter
  })
  // 1. Receive data from the memloader to data_queue
  /* Slice data by the L2 bandwidth (assuming 32 bytes).
  ** Different L2 bandwidth will require to change
  ** the memwriter module in Top.scala
  ** and the LiteralChunk bundle in Common.scala. */

  //TODO: Make sure this remaining_bytes thing works correctly with multiple requests
  val num_bytes_queue = Module(new Queue(UInt(32.W), 5))
  num_bytes_queue.io.enq <> io.num_bytes
  val consumed_bytes = RegInit(0.U(32.W))
  val remaining_bytes = num_bytes_queue.io.deq.bits - consumed_bytes

  //TODO: reset registers(data received, consumed_bytes)
  val data_queue = Module(new Queue(new LiteralChunk, 5)) 
  val data_received = RegInit(0.U(32.W))
  when(data_queue.io.enq.ready && data_queue.io.enq.valid){
    data_received := data_received + 1.U
  }
  dontTouch(data_received)
  val chunk_size = Mux(remaining_bytes < (l2bw/8).U,
    remaining_bytes % (l2bw/8).U,
    (l2bw/8).U)
  data_queue.io.enq.bits.chunk_data := io.mem_stream.output_data
  data_queue.io.enq.bits.chunk_size_bytes := chunk_size
  data_queue.io.enq.bits.is_final_chunk := (remaining_bytes < (l2bw/8).U)
  val fire_read = DecoupledHelper(
    io.mem_stream.output_valid,
    data_queue.io.enq.ready,
    chunk_size <= io.mem_stream.available_output_bytes,
    num_bytes_queue.io.deq.valid
  )
  data_queue.io.enq.valid := fire_read.fire(data_queue.io.enq.ready)
  io.mem_stream.output_ready := fire_read.fire(io.mem_stream.output_valid)
  io.mem_stream.user_consumed_bytes := Mux(num_bytes_queue.io.deq.valid,
    chunk_size,
    0.U)
  when(fire_read.fire()){
    when(data_queue.io.enq.bits.is_final_chunk){
      consumed_bytes := 0.U
    }.otherwise{
      consumed_bytes := consumed_bytes + chunk_size
    }
  }
  num_bytes_queue.io.deq.ready := fire_read.fire(num_bytes_queue.io.deq.valid) &&
    data_queue.io.enq.bits.is_final_chunk

  // 2. Write the data via Memwriter (Reverse the data) 
  val chunk_size_wire = Wire(UInt(6.W))
  chunk_size_wire := data_queue.io.deq.bits.chunk_size_bytes
  val chunk_data_vec = Wire(Vec(l2bw/8, UInt(8.W)))
  val reverse_data = Wire(UInt(l2bw.W))
  for(i <- 0 to l2bw/8-1){
    chunk_data_vec(chunk_size_wire-1.U-i.U) := data_queue.io.deq.bits.chunk_data(8*(i+1)-1, 8*i)
  }
  val remapper = Module(new VecToWire32B)
  val remap_vec = Wire(UInt(l2bw.W))
  remapper.io.input := chunk_data_vec
  remap_vec := remapper.io.output
  val fire_write = DecoupledHelper(
    data_queue.io.deq.valid,
    io.memwrites_in.ready
  )
  io.memwrites_in.bits.data := remap_vec 
  io.memwrites_in.bits.validbytes := chunk_size_wire
  io.memwrites_in.bits.end_of_message := data_queue.io.deq.bits.is_final_chunk
  io.memwrites_in.valid := fire_write.fire(io.memwrites_in.ready)
  data_queue.io.deq.ready := fire_write.fire(data_queue.io.deq.valid)
}
