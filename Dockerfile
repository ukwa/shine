FROM openjdk:8 AS build-env

ENV         ACTIVATOR_VERSION 1.3.11
ARG         USER_HOME_DIR="/root"

# Add cerificates that ensure download of dependencies works:
RUN         apt-get install -y ca-certificates-java && \
            update-ca-certificates

# Install Typesafe Activator
RUN         cd /tmp && \
            wget -q http://downloads.typesafe.com/typesafe-activator/$ACTIVATOR_VERSION/typesafe-activator-$ACTIVATOR_VERSION.zip && \
            unzip -q typesafe-activator-$ACTIVATOR_VERSION.zip -d /usr/local
RUN         mv /usr/local/activator-dist-$ACTIVATOR_VERSION /usr/local/activator && \
            rm /tmp/typesafe-activator-$ACTIVATOR_VERSION.zip

COPY /shine /shine/shine
COPY .git /shine/.git

WORKDIR /shine/shine

# Patch in the version tag:
RUN git fetch -t && export VERSION=`git describe --tags --always` && sed -i -r 's|version := ".*"|version := "'${VERSION}'"|' build.sbt || exit 0

# Perform a full clean build:
RUN rm -fr target
RUN /usr/local/activator/bin/activator clean stage

EXPOSE 9000

FROM openjdk:8-jre

COPY --from=build-env /shine/shine/target/universal/stage /shine

CMD /shine/bin/shine -Dconfig.file=/shine/conf/application-docker.conf -Dpidfile.path=/dev/null

