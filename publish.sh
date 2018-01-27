#!/usr/bin/env bash

sbt clean +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned sonatypeRelease