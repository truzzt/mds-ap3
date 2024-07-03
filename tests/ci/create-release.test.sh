#!/bin/bash
act push -s GITHUB_TOKEN="$(gh auth token)" -j release -e tests/ci/push-event.json --container-architecture linux/amd64
