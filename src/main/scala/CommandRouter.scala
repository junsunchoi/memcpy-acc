package memcpyacc

import Chisel._
import chisel3.{Printable}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig}
import freechips.rocketchip.util.DecoupledHelper
import freechips.rocketchip.rocket.constants.MemoryOpConstants

class CommandRouter(val cmd_que_depth: Int)(implicit p: Parameters) 
  extends Module {

  val io = IO(new Bundle{
    val rocc_in = Flipped(Decoupled(new RoCCCommand))
    val rocc_out = Decoupled(new RoCCResponse)

    val dmem_status_out = Valid(new RoCCCommand)
    val sfence_out = Output(Bool())

    val src_info = Decoupled(new StreamInfo) //to memloader
    val dest_info = Decoupled(new DstInfo) //to writer unit
    val bufs_completed = Input(UInt(64.W)) //from writer unit
    val no_writes_inflight = Input(Bool()) //from writer unit
    val num_bytes = Decoupled(UInt(32.W)) //to Xerox
  })
  val FUNCT_SFENCE                        = 0.U
  val FUNCT_SRC_INFO                      = 1.U
  val FUNCT_DEST_INFO                     = 2.U
  val FUNCT_CHECK_COMPLETION              = 3.U

  val cur_funct = io.rocc_in.bits.inst.funct
  val cur_rs1 = io.rocc_in.bits.rs1
  val cur_rs2 = io.rocc_in.bits.rs2

  // io.sfence_out and io.dmem_status_out
  val sfence_fire = DecoupledHelper(
    io.rocc_in.valid,
    cur_funct === FUNCT_SFENCE
  )
  io.sfence_out := sfence_fire.fire

  io.dmem_status_out.bits <> io.rocc_in.bits
  io.dmem_status_out.valid <> io.rocc_in.fire

  // Memloader interface
  val src_info_queue = Module(new Queue(new StreamInfo, 4))
  src_info_queue.io.enq.bits.ip := cur_rs1
  src_info_queue.io.enq.bits.isize := cur_rs2
  val src_info_fire = DecoupledHelper(
    io.rocc_in.valid,
    cur_funct === FUNCT_SRC_INFO,
    src_info_queue.io.enq.ready
  )
  src_info_queue.io.enq.valid := src_info_fire.fire(src_info_queue.io.enq.ready)
  io.src_info <> src_info_queue.io.deq
  io.num_bytes.bits := src_info_queue.io.deq.bits.isize
  io.num_bytes.valid := src_info_queue.io.deq.fire

  // Memwriter interface
  val dest_info_queue = Module(new Queue(new DstInfo, 4))
  dest_info_queue.io.enq.bits.op := cur_rs1
  dest_info_queue.io.enq.bits.cmpflag := cur_rs2
  val dest_info_fire = DecoupledHelper(
    io.rocc_in.valid,
    cur_funct === FUNCT_DEST_INFO,
    dest_info_queue.io.enq.ready
  )
  dest_info_queue.io.enq.valid := dest_info_fire.fire(dest_info_queue.io.enq.ready)
  io.dest_info <> dest_info_queue.io.deq

  // Completion check
  val track_dispatched_src_infos = RegInit(0.U(32.W))
  val bufs_completed_when_start = RegInit(0.U(32.W))
  when (io.rocc_in.fire) {
    when (io.rocc_in.bits.inst.funct === FUNCT_SRC_INFO) {
    track_dispatched_src_infos := track_dispatched_src_infos + 1.U
    bufs_completed_when_start := io.bufs_completed
    }
  }
  when(io.rocc_in.fire && cur_funct ===FUNCT_CHECK_COMPLETION){
    track_dispatched_src_infos := 0.U
    bufs_completed_when_start := io.bufs_completed
  }
  val check_completion_fire = DecoupledHelper(
    io.rocc_in.valid,
    cur_funct === FUNCT_CHECK_COMPLETION,
    io.no_writes_inflight,
    track_dispatched_src_infos =/= 0.U,
    io.bufs_completed - bufs_completed_when_start === track_dispatched_src_infos,
    io.rocc_out.ready
  )

  // rocc_in and rocc_out
  io.rocc_out.valid := check_completion_fire.fire(io.rocc_out.ready)
  io.rocc_out.bits.data := track_dispatched_src_infos
  io.rocc_out.bits.rd := io.rocc_in.bits.inst.rd
  io.rocc_in.ready := sfence_fire.fire(io.rocc_in.valid) ||
    src_info_fire.fire(io.rocc_in.valid) ||
    dest_info_fire.fire(io.rocc_in.valid) ||
    check_completion_fire.fire(io.rocc_in.valid)
}
