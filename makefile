EMPTY=
SPACE=$(EMPTY) $(EMPTY)

SRC_DIR=src
BUILD_DIR=build
LIB_DIR=lib

LIBS=\
	gs-core-1.2-git-last \
	gs-ui-1.2-git-last \
	jts-1.12

FILES=\
	AbstractStrategy \
	AverageDensityStrategy \
	LotPositioningStrategy \
	RandomDensityStrategy \
	RenderingLayer \
	Simulation \
	Test

CP:= $(addprefix $(LIB_DIR)/,$(LIBS))
CP:= $(addsuffix .jar,$(CP))
CP:= $(subst $(SPACE),:,$(CP))

all: compile run

compile: $(addprefix $(SRC_DIR)/,$(addsuffix .java,$(FILES)))
	javac -cp $(CP) -d $(BUILD_DIR) $^

run:
	cd $(BUILD_DIR) && java -cp .:$(subst lib/,../lib/,$(CP)) Test

clean:
	rm $(BUILD_DIR)/*.class
	rm $(SRC_DIR)/*~
