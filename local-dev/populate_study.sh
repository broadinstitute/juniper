#!/usr/bin/env bash

# populate the study with a given folder name

curl -X POST localhost:8080/api/populate/study/$1
