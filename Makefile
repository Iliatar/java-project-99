run-dist:
	./build/install/app/bin/app

build-dist:
	./gradlew clean
	./gradlew build
	./gradlew installDist

build-run: build-dist run-dist

build:
	./gradlew clean build

report:
	./gradlew jacocoTestReport

.PHONY: build

setup:
	./gradlew wrapper --gradle-version 8.6

install:
	./gradlew installDist

start:
	./gradlew bootRun --args='--spring.profiles.active=dev'
