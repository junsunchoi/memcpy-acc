#include "rerocc_cluster.h"

#include <riscv/mmu.h>
#include <riscv/rerocc.h>

rerocc_cluster_t::rerocc_cluster_t() {
  memcpy_state = {0, 0, 0, 0, 0};
}

reg_t rerocc_cluster_t::memcpy(rocc_insn_t insn, reg_t xs1, reg_t UNUSED xs2){
  switch(insn.funct){
    case 0: //FENCE
      break;
    case 1: //Get input source info
      memcpy_state.ip = xs1;
      memcpy_state.isize = xs2;
      memcpy_state.size_processed = 0;
      break;
    case 2: //Get output address info
      //printf("src=0x%lx sz=0x%lx dst=0x%lx cmgflagptr=0x%lx\n", memcpy_state.ip, memcpy_state.isize, xs1, xs2);
      memcpy_state.op = xs1;
      memcpy_state.cmpflag = xs2;
      while (memcpy_state.size_processed < memcpy_state.isize) {
        uint8_t temp = p->get_mmu()->load<uint8_t>(memcpy_state.ip + memcpy_state.size_processed);
        p->get_mmu()->store<uint8_t>(memcpy_state.op + memcpy_state.size_processed, temp);
        memcpy_state.size_processed += (memcpy_state.isize - memcpy_state.size_processed >= 1 ? 1 : memcpy_state.isize - memcpy_state.size_processed);
      }
      break;
    case 3: //Check completion
      p->get_mmu()->store<uint64_t>(memcpy_state.cmpflag, memcpy_state.isize == memcpy_state.size_processed ? 1 : 0);
      //printf("m[cmpflg]=0x%lx\n", memcpy_state.isize == memcpy_state.size_processed ? 1 : 0);
      return 1; // dummy
      break;
    case 4: //Custom function added to check the output.
      return p->get_mmu()->load<uint64_t>(memcpy_state.op);
      break;
    default:
      illegal_instruction();
      break;
  }
  return memcpy_state.isize;
}

reg_t rerocc_cluster_t::dispatch(uint8_t accid, rocc_insn_t insn, reg_t xs1, reg_t xs2) {
  switch (accid) {
    default:
      return memcpy(insn, xs1, xs2);
  }
}

define_rerocc_funcs(rerocc_cluster_t, xstr(EXTENSION_NAME), dispatch)

std::vector<insn_desc_t> rerocc_cluster_t::get_instructions()
{
  std::vector<insn_desc_t> insns;
  push_rerocc_insns(insns);
  return insns;
}

std::vector<disasm_insn_t*> rerocc_cluster_t::get_disasms()
{
  std::vector<disasm_insn_t*> insns;
  return insns;
}

// THIS MUST MATCH EXTENSION_NAME
REGISTER_EXTENSION(rerocccluster, []() { return new rerocc_cluster_t; })
