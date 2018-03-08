#!/usr/bin/env bash

sbt +clean +test +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned sonatypeRelease