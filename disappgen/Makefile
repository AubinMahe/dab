-include generated-files.mk

all: $(SRCS)

$(SRCS): ../distributed-application.xsd
	@rm -fr src-gen
	@mkdir -p src-gen
	xjc -d src-gen -p disapp.generator.model -readOnly $<

generated-files.mk: ../distributed-application.xsd
	@mkdir -p src-gen
	xjc -d src-gen -p disapp.generator.model -readOnly $<
	find src-gen/ -type f | awk 'BEGIN { printf( "SRCS =" )} {printf( "\\\n " $$0)}' >$@

clean:
	rm -fr src-gen
	rm generated-files.mk
