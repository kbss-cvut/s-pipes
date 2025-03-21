FROM ontotext/graphdb:10.7.0

# Override parent entrypoint
ENTRYPOINT []

ENV GRAPHDB_HOME=/opt/graphdb/home
ENV GRAPHDB_INSTALL_DIR=/opt/graphdb/dist

WORKDIR ${GRAPHDB_HOME}

# Install libs related to RDF processing
### for arm64
RUN if command -v apt >/dev/null; then \
    apt update && \
    apt install -y python3-rdflib && \
    apt install -y liburi-perl; \
fi

### for amd64
RUN if command -v apk >/dev/null; then \
    apk add py3-rdflib && \
    apk add perl-uri; \
fi

# Copy scripts
COPY bin/* ${GRAPHDB_INSTALL_DIR}/bin/

EXPOSE 7200

# Assuming following input directories:
#     - /repo-config and data -- configuration ttl files to create repositories
#     - /root/graphdb-import -- files that initialize/import data to specific repositories
CMD ${GRAPHDB_INSTALL_DIR}/bin/repo-init.sh /repo-config ${GRAPHDB_HOME} & ${GRAPHDB_INSTALL_DIR}/bin/graphdb -Dgraphdb.home=${GRAPHDB_HOME} -Dgraphdb.logback=${GRAPHDB_INSTALL_DIR}/conf/logback.xml
