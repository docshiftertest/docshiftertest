# DocShifter-base

FROM eclipse-temurin:21.0.1_12-jre-jammy

LABEL maintainer="DocShifter, support@docshifter.com"

ARG DEPENDENCY

# Changing the default shell for subsequent instructions in exec form to run with bash (instead of sh when not specified)
SHELL ["/bin/bash", "-c"]

RUN groupadd -r -g 999 docshifter && useradd -r -u 999 -g docshifter docshifter

RUN apt-get autoremove && apt-get autoclean -y && apt-get update && apt-get install -y --no-install-recommends \
    libtcnative-1 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# licensing is NOT the persistent licensing folder, rather a "temporary” licensing path where Nalpeiron stores its
# .cache, .lic,... stuff (i.e. the Nalpeiron workdir which is set to that path in all the components), as we cannot
# get 2 separate container instances to act under the same computer ID (especially in Kubernetes, where an instance
# might be scheduled to node X one time and to node Y another time and where sharing the Nalpeiron workdir over an
# NFS is not supported). Therefore we try to return the license each time the container terminates or clean up a
# ghost activation at application startup if the previous instance crashed.
RUN mkdir -p /opt/DocShifter/{beans,licensing} \
    && chown -R docshifter:docshifter /opt/DocShifter

# Create empty marker file that details we're in a container environment
RUN touch /.a602a2cd-ef1c-4c95-a32c-af8a10cc51cf

COPY --chown=docshifter:docshifter target/jars target/classes/license/libShaferFilechck.so target/classes/license/libPassiveFilechck.so target/${DEPENDENCY}-Beans-docker/lib-doc /opt/DocShifter/beans/lib/
