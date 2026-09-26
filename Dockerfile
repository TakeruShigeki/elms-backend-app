# ── Stage 1: ビルド ──
# JDKでGradleビルドを実行してJARファイルを作る
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 依存関係のキャッシュ（Gradleファイルが変わらなければ再ダウンロードしない）
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon -q

# ソースコードをコピーしてビルド
COPY src/ src/
RUN ./gradlew build -x test --no-daemon

# ── Stage 2: 実行 ──
# JREだけの軽量イメージでアプリを動かす
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 非rootユーザーで動かす（侵害時の影響を限定）
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/uploads && chown spring:spring /app/uploads

COPY --from=builder --chown=spring:spring /app/build/libs/*.jar app.jar

USER spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
