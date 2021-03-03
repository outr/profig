#!/usr/bin/env bash

sbt +clean +test +coreJS/publishSigned +coreJVM/publishSigned sonatypeRelease