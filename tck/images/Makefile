CC=sdcc
CPP=sdcpp
CPP=cpp
AS=asx8051

include Makefile.local

SDCCFLAGS=-mmcs51 \
          --code-size 0x3c00 \
          --xram-size 0x0200 \
          --xram-loc 0x3c00 \
          -I$(FX2LIBDIR)/include

ifeq ($(FX2LIBDIR),)
$(error FX2LIBDIR has to be defined in Makefile.local)
endif

ifeq ($(DEVICE),)
OPT_D=
else
OPT_D=-d=$(DEVICE)
endif

all: target target/topology.ihx target/bulkint.ihx target/iso.ihx

clean:
	@echo Cleaning...
	@rm -f $(wildcard target/*)

target:
	@mkdir target

list:
	cycfx2prog --list

target/dscr.topology.rel: target/dscr.topology.asm
target/dscr.bulkint.rel: target/dscr.bulkint.asm
target/dscr.iso.rel: target/dscr.iso.asm
target/descriptors.rel: descriptors.asm

target/topology.ihx: target/main.rel target/dscr.topology.rel
target/bulkint.ihx: target/main.rel target/dscr.bulkint.rel
target/iso.ihx: target/main.rel target/dscr.iso.rel

target/firmware.ihx: target/firmware.rel target/descriptors.rel
	@echo LD $@
	$(CC) $(SDCCFLAGS) -o $@ $^ fx2.lib -L$(FX2LIBDIR)/lib

target/dscr.%.rel: target/dscr.%.asm
	@echo AS `basename $<`
	@(cd target; $(AS) -logs `basename $<`)

target/%.rel: %.asm
	@echo AS $<
	$(AS) -logs $<
	@mkdir -p target
	@mv *.rel target/
	@mv *.lst target/
	@mv *.sym target/

#target/main.rel: main.c
#	@echo CC $<
#	@$(CC) $(SDCCFLAGS) -c -o $@ $<

target/%.rel: %.c
	@echo CC $<
	$(CC) $(SDCCFLAGS) -c -o $@ $<

program-%: target/%.ihx
	cycfx2prog $(OPT_D) prg:$< run

target/dscr.topology.asm: dscr.asm
	@echo Creating $<
	@$(CPP) -DTOPOLOGY dscr.asm | grep -v "^#" > $@

target/dscr.bulkint.asm: dscr.asm
	@echo Creating $<
	@$(CPP) -DBULKINT dscr.asm | grep -v "^#" > $@

target/dscr.iso.asm: dscr.asm
	@echo Creating $<
	@$(CPP) -DISO dscr.asm | grep -v "^#" > $@

target/%.ihx: target/main.rel target/dscr.%.rel
	@echo LD $@
	$(CC) $(SDCCFLAGS) -Wl"-b DSCR_AREA=0x3e00" -o $@ $^ fx2.lib -L$(FX2LIBDIR)/lib 
