# DocShifter-base
#
# VERSION       6.1.1

# using the OpenJDK image
FROM openjdk:10

MAINTAINER DocByte support@docbyte.com

#Make folder for DocShifter
RUN ["mkdir","-p","/opt/DocShifter/beans/lib"]
RUN ["mkdir","-p","/opt/DocShifter/licensing/"]

ARG DEPENDENCY

COPY target/jars target/classes/license/libnalpjava.so target/classes/license/nalpjava.dll target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
COPY target/classes/application.properties /opt/DocShifter/beans/
COPY target/classes/license/DSLicenseCode.txt target/classes/license/docShifterFileCheck.dll target/classes/license/docShifterFileCheck.so target/classes/license/DSLicenseActivationAnswer.txt target/classes/license/DSLicenseActivationRequest.txt /opt/DocShifter/licensing/