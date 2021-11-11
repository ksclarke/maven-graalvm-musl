
#
# This image is intended to be used as the source for the first phase of a multistage build.
#
# See the project page at https://github.com/ksclarke/maven-graalvm-musl for more information
#

ARG GRAALVM_TAG

# Set up a default build environment for our components
FROM ghcr.io/graalvm/graalvm-ce:${GRAALVM_TAG}

ARG ZLIB_VERSION
ARG MUSL_VERSION
ARG MAVEN_VERSION
ARG ZLIB_CHECKSUM
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
RUN gu install native-image

# Create a location for our static libs
ARG MGM_TOOLS="/mgm_tools"
WORKDIR ${MGM_TOOLS}

# Install musl-gcc to compile statically against
WORKDIR /opt/musl
RUN MUSL_DOWNLOAD="/tmp/musl.tar.gz" \
    && curl -s -L -o $MUSL_DOWNLOAD https://musl.libc.org/releases/musl-${MUSL_VERSION}.tar.gz \
    && tar -xvzf $MUSL_DOWNLOAD --strip-components 1 \
    && ./configure --disable-shared --prefix=${MGM_TOOLS} \
    && make install \
    && rm -rf $MUSL_DOWNLOAD /opt/musl

# Use the container's libstdc++ (it works as well as using Alpine's)
RUN echo "System's libstdc version: $(ls /usr/lib/gcc/x86_64-redhat-linux/)" \
    && cp /usr/lib/gcc/x86_64-redhat-linux/${LIBSTDC_VERSION}/libstdc++.a ${MGM_TOOLS}/lib/

# Set the musl-gcc build properties
ENV PATH="$PATH:${MGM_TOOLS}/bin"
ENV CC="musl-gcc"

# Install zlib to use when statically compiling against musl
WORKDIR /opt/zlib
RUN ZLIB_DOWNLOAD="/tmp/zlib.tar.gz" \
    && curl -s -L -o $ZLIB_DOWNLOAD https://zlib.net/zlib-${ZLIB_VERSION}.tar.gz \
    && echo "${ZLIB_CHECKSUM}  ${ZLIB_DOWNLOAD}" | sha256sum -c - \
    && tar -xvzf $ZLIB_DOWNLOAD --strip-components 1 \
    && ./configure --static --prefix=${MGM_TOOLS} \
    && make install \
    && rm -rf $ZLIB_DOWNLOAD /opt/zlib

# Install xz to use when unpacking UPX
RUN XZ_BASE_URL="https://yum.oracle.com/repo/OracleLinux/OL8/3/baseos/base/x86_64/getPackage" \
    && rpm -ivh ${XZ_BASE_URL}/xz-${XZ_VERSION}.el8.x86_64.rpm

# Install UPX for optional compression of the application
WORKDIR /opt/upx
RUN UPX_DOWNLOAD="/tmp/upx.tar.xz" \
    && UPX_BASE_URL="https://github.com/upx/upx/releases/download" \
    && curl -s -L -o $UPX_DOWNLOAD ${UPX_BASE_URL}/v${UPX_VERSION}/upx-${UPX_VERSION}-amd64_linux.tar.xz \
    && tar -xvf $UPX_DOWNLOAD --strip-components 1 \
    && mv upx ${MGM_TOOLS}/bin \
    && rm -rf $UPX_DOWNLOAD /opt/upx

# Put GraalVM's cacerts in a predictable place so they can be copied
RUN cp /etc/pki/java/cacerts /etc/default/cacerts

# We're not going to set a user or home directory at this point
ADD /maven/README.md /README.md

# This container shouldn't be run as a standalone, except in testing
ENTRYPOINT ["tail", "-f", "/dev/null"]
