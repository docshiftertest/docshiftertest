# DocShifter-base
#
# VERSION       6.2.4.1

# using the IBM Semeru openj9 image
FROM ibm-semeru-runtimes:open-11-jre

LABEL maintainer="DocShifter, support@docshifter.com"

ARG DEPENDENCY

RUN groupadd -r -g 999 docshifter && useradd -r -u 999 -g docshifter docshifter

RUN apt-get update && apt-get install -y --no-install-recommends \
    libtcnative-1 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /opt/DocShifter/beans \
    && chown -R docshifter:docshifter /opt/DocShifter

COPY --chown=docshifter:docshifter target/jars target/classes/license/libnalpjava.so target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
COPY --chown=docshifter:docshifter target/classes/license/DSLicenseCode.txt target/classes/license/DSLicenseActivationRequest.txt target/classes/license/DSLicenseActivationAnswer.txt target/classes/license/docShifterFileCheck.dll target/classes/license/docShifterFileCheck.so /opt/DocShifter/licensing/
