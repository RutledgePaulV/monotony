FROM ghcr.io/graalvm/graalvm-community:20.0.1-ol9-20230622 AS BUILDER

RUN curl -O https://download.clojure.org/install/linux-install-1.11.1.1347.sh \
    && chmod +x linux-install-1.11.1.1347.sh \
    && ./linux-install-1.11.1.1347.sh

RUN microdnf install git
RUN mkdir -p -m 0600 ~/.ssh
RUN echo "github.com ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIOMqqnkVzrm0SdG6UOoqKLsabgH5C9okWi0dh2l9GKJl" >> ~/.ssh/known_hosts
RUN echo "github.com ecdsa-sha2-nistp256 AAAAE2VjZHNhLXNoYTItbmlzdHAyNTYAAAAIbmlzdHAyNTYAAABBBEmKSENjQEezOmxkZMy7opKgwFB9nkt5YRrYMjNuG5N87uRgg6CLrbo5wAdT/y6v0mKV0U2w0WZ2YB/++Tpockg=" >> ~/.ssh/known_hosts
RUN echo "github.com ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCj7ndNxQowgcQnjshcLrqPEiiphnt+VTTvDP6mHBL9j1aNUkY4Ue1gvwnGLVlOhGeYrnZaMgRK6+PKCUXaDbC7qtbW8gIkhL7aGCsOr/C56SJMy/BCZfxd1nWzAOxSDPgVsmerOBYfNqltV9/hWCqBywINIR+5dIg6JTJ72pcEpEjcYgXkE2YEFXV1JHnsKgbLWNlhScqb2UmyRkQyytRLtL+38TGxkxCflmO+5Z8CSSNY7GidjMIZ7Q4zMjA2n1nGrlTDkzwDCsw+wqFPGQA179cnfGWOWRVruj16z6XyvxvjJwbz0wQZ75XK5tKSb7FNyeIEs4TT4jk+S4dhPeAUC5y+bDYirYgM4GC7uEnztnZyaVWQ7B381AK4Qdrwt51ZqExKbQpTUNn+EjqoTwvqNj4kqx5QUCI0ThS/YkOxJCXmPUWZbhjpCg56i+2aB6CmK2JGhn57K5mj0MNdBXA4/WnwH6XoPWJzK5Nyu2zB3nAZp+S5hpQs+p1vN1/wsjk=" >> ~/.ssh/known_hosts
RUN git config --global url.ssh://git@github.com/.insteadOf https://github.com/

WORKDIR /app
COPY . /app
RUN chmod +x install.sh
RUN --mount=type=ssh ./install.sh
RUN native-image -jar target/monotony.jar \
    --features=InitAtBuildTimeFeature \
    --no-fallback target/monotony \
    --initialize-at-build-time=org.antlr.v4.Tool,org.antlr.v4.tool.AttributeDict,org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.Recognizer,org.antlr.v4.tool.Rule,org.antlr.v4.tool.Grammar
