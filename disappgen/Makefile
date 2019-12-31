-include generated-files.mk

SCHEMAS =\
 ../distributed-application-common.xsd\
 ../distributed-application.xsd\
 ../distributed-application-automaton.xsd\
 ../distributed-application-generation.xsd

all: $(SRCS)

$(SRCS): $(SCHEMAS)
	@rm -fr src-gen
	@mkdir -p src-gen
	xjc -d src-gen -p disapp.generator.model    -readOnly ../distributed-application.xsd
	xjc -d src-gen -p disapp.generator.genmodel -readOnly ../distributed-application-generation.xsd

generated-files.mk: $(SCHEMAS)
	@mkdir -p src-gen
	xjc -d src-gen -p disapp.generator.model    -readOnly ../distributed-application.xsd
	xjc -d src-gen -p disapp.generator.genmodel -readOnly ../distributed-application-generation.xsd
	find src-gen/ -type f | awk 'BEGIN { printf( "SRCS =" )} {printf( "\\\n " $$0)}' >$@
