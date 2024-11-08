ifndef RISCV
$(error RISCV is unset)
else
$(info Running with RISCV=$(RISCV))
endif

PREFIX ?= $(RISCV)/
EXTENSION_NAME ?= rerocccluster
SO := lib$(EXTENSION_NAME).so

default: $(SO)

$(SO): rerocc_cluster.cc
	g++ -L $(RISCV)/lib -Wl,-rpath,$(RISCV)/lib -shared -o $@ -std=c++20 -DEXTENSION_NAME=$(EXTENSION_NAME) -I $(RISCV)/include -fPIC -O3 $^

.PHONY: install
install: $(SO)
	cp $(SO) $(RISCV)/lib

clean:
	rm -rf *.o $(SO)
