EMPTY=
SPACE=$(EMPTY) $(EMPTY)

SRC_DIR=src
BUILD_DIR=build
LIB_DIR=lib

LIBS=\
	geoapi-2.2-M1 \
	gs-algo-1.2-git-last \
	gs-core-1.2-git-last \
	gs-ui-1.2-git-last \
	gt-api-2.5.1 \
	gt-main-2.5.1 \
	gt-metadata-2.5.1 \
	gt-epsg-hsql-2.5.1 \
	gt-referencing-2.5.1 \
	gt-shapefile-2.5.1 \
	jsr-275-1.0-beta-2 \
	jts-1.12

FILES=\
	AbstractStrategy \
	AverageDensityStrategy \
	BackgroundLayer \
	CityOps \
	CrossroadPivot \
	Density \
	DiscreteDensityStrategy \
	LotConstructionStrategy \
	LotOps \
	LotPositioningStrategy \
	MouseManager \
	RandomDensityStrategy \
	RoadDevelopmentStrategy \
	RoadOps \
	ShapeFileLoader \
	Simulation \
	Test

CP:= $(addprefix $(LIB_DIR)/,$(LIBS))
CP:= $(addsuffix .jar,$(CP))
CP:= $(subst $(SPACE),:,$(CP))

OPTIONS=

all: compile run

compile: $(addprefix $(SRC_DIR)/,$(addsuffix .java,$(FILES)))
	javac -cp $(CP) -d $(BUILD_DIR) $(OPTIONS) $^

run:
	cd $(BUILD_DIR) && java -cp .:$(subst lib/,../lib/,$(CP)) Test

javadoc: $(addprefix $(SRC_DIR)/,$(addsuffix .java,$(FILES)))
	javadoc -private -d javadoc src/*.java

clean:
	rm $(BUILD_DIR)/*.class
	rm $(SRC_DIR)/*~
