name: Build and Deploy to Server

on:
  push:
    branches: [ "main" ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'
          cache-dependency-path: |
            build.gradle
            settings.gradle

      - name: Build with Gradle
        run: ./gradlew clean bootJar -x test

      - name: Copy JAR to Server
        uses: appleboy/scp-action@v0.1.7
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          source: "build/libs/*.jar"
          target: "/opt/backend/upload/"
          strip_components: 2

      - name: Restart Systemd Service
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            set -e

            NEW_JAR=$(find /opt/backend/upload -maxdepth 1 -name "*.jar" | head -n 1)

            if [ -z "$NEW_JAR" ]; then
              echo "No JAR file found"
              exit 1
            fi

            sudo systemctl stop api.rspcm.service || true

            sudo rm -f /opt/backend/app.jar
            sudo mv "$NEW_JAR" /opt/backend/app.jar
            sudo rm -rf /opt/backend/upload

            sudo systemctl start api.rspcm.service
            sleep 15
            sudo systemctl status api.rspcm.service --no-pager