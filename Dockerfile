FROM node:18-alpine

WORKDIR /app

COPY . .
COPY pom.xml
COPY build.xml

RUN mvn clean compile
RUN mvn exec:java -Dexec.mainClass="infraimageconverter.InfraImageConverter" -Dexec.args="-c 4 -f png -i {INPUT_DIR} -o {OUTPUT_DIR}"
