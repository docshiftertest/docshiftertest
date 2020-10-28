# DocShifter-base
#
# VERSION       6.2.4.1

# using the openjdk11-openj9 image
FROM adoptopenjdk:11-jre-openj9

LABEL maintainer="DocShifter, support@docshifter.com"

ARG DEPENDENCY

RUN groupadd -r docshifter && useradd -r -g docshifter docshifter

COPY target/jars target/classes/license/libnalpjava.so target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
COPY target/classes/license/DSLicenseCode.txt target/classes/license/DSLicenseActivationRequest.txt target/classes/license/DSLicenseActivationAnswer.txt target/classes/license/docShifterFileCheck.dll target/classes/license/docShifterFileCheck.so /opt/DocShifter/licensing/