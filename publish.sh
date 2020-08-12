#!/usr/bin/env bash

sbt +clean +test +macrosJS/publishSigned +macrosJVM/publishSigned +coreJS/publishSigned +coreJVM/publishSigned +xml/publishSigned +hocon/publishSigned +yaml/publishSigned +inputJS/publishSigned +inputJVM/publishSigned +live/publishSigned +all/publishSigned sonatypeRelease