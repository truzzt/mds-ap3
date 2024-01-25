#!/bin/bash
act release -s GITHUB_TOKEN="$(gh auth token)" -j build-and-publish-edc-extension -e tests/ci/release-event.json --container-architecture linux/amd64
