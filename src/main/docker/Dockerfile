
#
# This image is intended to be used as the source for the first phase of a multistage build.
#

ARG GRAALVM_TAG

# Set up a build environment to compile the application
FROM ghcr.io/graalvm/graalvm-ce:${GRAALVM_TAG} AS builder

ARG GRAALVM_HOME
ARG ZLIB_VERSION
ARG MUSL_VERSION
ARG MAVEN_VERSION
ARG ZLIB_CHECKSUM
ARG MUSL_CHECKSUM
ARG MAVEN_CHECKSUM
ARG LIBSTDC_VERSION
ARG UPX_VERSION
ARG XZ_VERSION

# Configure Maven build properties
ENV M2_HOME=/opt/maven
ENV MAVEN_HOME=${M2_HOME}
ENV PATH=${MAVEN_HOME}/bin:${PATH}

# Install Maven
WORKDIR ${MAVEN_HOME}
RUN MAVEN_BASE_URL="https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries/" \
    && MAVEN_DOWNLOAD="/tmp/apache-maven.tar.gz" \
    && curl -s -o ${MAVEN_DOWNLOAD} ${MAVEN_BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    # Check the download's SHA512 (the double space in the `echo` is necessary)
    && echo "${MAVEN_CHECKSUM}  ${MAVEN_DOWNLOAD}" | sha512sum -c - \
    && tar -xzf ${MAVEN_DOWNLOAD} -C ${MAVEN_HOME} --strip-components=1 \
    && rm -f $MAVEN_DOWNLOAD

# Install GraalVM's native-image
RUN ${GRAALVM_HOME}/bin/gu install native-image

# Create a location for our static libs
ARG STATIC_LIBS="/staticlibs"
WORKDIR ${STATIC_LIBS}

# Install musl-gcc to compile statically against
WORKDIR /opt/musl
RUN MUSL_DOWNLOAD="/tmp/musl.tar.gz" \
    && curl -s -L -o $MUSL_DOWNLOAD https://musl.libc.org/releases/musl-${MUSL_VERSION}.tar.gz \
    && tar -xvzf $MUSL_DOWNLOAD --strip-components 1 \
    && ./configure --disable-shared --prefix=${STATIC_LIBS} \
    && make install \
    && rm -f $MUSL_DOWNLOAD

# Use the container's libstdc++ (it works as well as using Alpine's)
RUN echo "System's libstdc version: $(ls /usr/lib/gcc/x86_64-redhat-linux/)" \
    && cp /usr/lib/gcc/x86_64-redhat-linux/${LIBSTDC_VERSION}/libstdc++.a ${STATIC_LIBS}/lib/

# Set the musl-gcc build properties
ENV PATH="$PATH:${STATIC_LIBS}/bin"
ENV CC="musl-gcc"

# Install zlib to use when statically compiling against musl
WORKDIR /opt/zlib
RUN ZLIB_DOWNLOAD="/tmp/zlib.tar.gz" \
    && curl -s -L -o $ZLIB_DOWNLOAD https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz \
    && echo "${ZLIB_CHECKSUM}  ${ZLIB_DOWNLOAD}" | sha256sum -c - \
    && tar -xvzf $ZLIB_DOWNLOAD --strip-components 1 \
    && ./configure --static --prefix=${STATIC_LIBS} \
    && make install \
    && rm -f $ZLIB_DOWNLOAD

# Install xz to use when unpacking UPX
RUN XZ_BASE_URL="https://yum.oracle.com/repo/OracleLinux/OL8/3/baseos/base/x86_64/getPackage" \
    && rpm -ivh ${XZ_BASE_URL}/xz-${XZ_VERSION}.el8.x86_64.rpm

# Install UPX for optional compression of the application
WORKDIR /opt/upx
RUN UPX_DOWNLOAD="/tmp/upx.tar.xz" \
    && UPX_BASE_URL="https://github.com/upx/upx/releases/download" \
    && curl -s -L -o $UPX_DOWNLOAD ${UPX_BASE_URL}/v${UPX_VERSION}/upx-${UPX_VERSION}-amd64_linux.tar.xz \
    && tar -xvf $UPX_DOWNLOAD --strip-components 1 \
    && cp upx ${STATIC_LIBS}/bin \
    && rm -f $UPX_DOWNLOAD

# Put GraalVM's cacerts in a predictable place so they can be copied
RUN cp ${GRAALVM_HOME}/lib/security/cacerts /etc/default/cacerts

# Print out a little information about how this image is intended to be used
ENTRYPOINT ["echo", "This image is intended to be used as the Maven builder in a multistage Docker build\n  "]
CMD ["For more details, consult https://github.com/ksclarke/maven-graalvm-musl"]
