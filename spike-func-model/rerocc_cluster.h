#ifndef _REROCC_CLUSTER_H
#define _REROCC_CLUSTER_H

#include <vector>
#include <riscv/decode.h>
#include <riscv/rocc.h>

typedef struct {
  reg_t ip;
  reg_t isize;
  reg_t size_processed;
  reg_t op;
  reg_t cmpflag;
  reg_t counter;
} memcpy_state_t;

#define str(s) #s
#define xstr(s) str(s)
#define nstr(s) s

class rerocc_cluster_t : public extension_t {
public:
  const char* name() { return xstr(EXTENSION_NAME); }
  rerocc_cluster_t();

  reg_t dispatch(uint8_t accid, rocc_insn_t insn, reg_t xs1, reg_t xs2);

  virtual std::vector<insn_desc_t> get_instructions();
  virtual std::vector<disasm_insn_t*> get_disasms();

private:
  reg_t memcpy(rocc_insn_t insn, reg_t xs1, reg_t xs2);
  memcpy_state_t memcpy_state;
};

#endif
