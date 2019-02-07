# DocShifter-base
#
# VERSION       6.2.1

# using the OpenJDK image
FROM openjdk:11-jre

LABEL maintainer="DocShifter, support@docshifter.com"

ARG DEPENDENCY

COPY target/jars target/classes/license/libnalpjava.so target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
COPY target/classes/license/DSLicenseCode.txt target/classes/license/DSLicenseActivationRequest.txt target/classes/license/DSLicenseActivationAnswer.txt target/classes/license/docShifterFileCheck.dll target/classes/license/docShifterFileCheck.so /opt/DocShifter/licensing/