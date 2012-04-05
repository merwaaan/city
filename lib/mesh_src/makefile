SRC_DIR=src
CLASSES_DIR=classes
PACKAGE_PATH=megamu/mesh

all: compile jar

compile: $(SRC_DIR)/$(PACKAGE_PATH)/*.java
	javac -cp original/quickhull3d.1.4.jar -d $(CLASSES_DIR) $(SRC_DIR)/$(PACKAGE_PATH)/*.java

jar: $(CLASSES_DIR)/$(PACKAGE_PATH)/*.class
	cd $(CLASSES_DIR) && jar cvf ../../mesh.jar megamu
