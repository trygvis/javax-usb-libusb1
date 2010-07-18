include Makefile.local

CPP=sdcpp
CPP=cpp

ifeq ($(FX2LIBDIR),)
$(error FX2LIBDIR has to be defined in Makefile.local)
endif

ifeq ($(DEVICE),)
OPT_D=
else
OPT_D=-d=$(DEVICE)
endif

all: topology bulkint iso

clean:
	rm -f $(wildcard *.rel *.lst *.sym *.*.asm)
	$(MAKE) -f Makefile.topology clean

list:
	cycfx2prog --list

dscr.topology.asm: dscr.asm
	$(CPP) -DTOPOLOGY dscr.asm | grep -v "^#" > dscr.topology.asm

dscr.bulkint.asm: dscr.asm
	$(CPP) -DBULKINT dscr.asm | grep -v "^#" > dscr.bulkint.asm

dscr.iso.asm: dscr.asm
	$(CPP) -DISO dscr.asm | grep -v "^#" > dscr.iso.asm

program-%:
	$(MAKE) $(patsubst program-%,%,$@)
	cycfx2prog $(OPT_D) prg:build/$(patsubst program-%,%,$@).ihx run

topology: dscr.topology.asm
	$(MAKE) -f Makefile.topology

bulkint: dscr.bulkint.asm
	$(MAKE) -f Makefile.bulkint

iso: dscr.iso.asm
	$(MAKE) -f Makefile.iso
