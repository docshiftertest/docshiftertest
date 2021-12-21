# DocShifter-base

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

COPY --chown=docshifter:docshifter target/jars target/classes/license/libShaferFilechck.so target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
