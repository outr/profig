#!/usr/bin/env bash

sbt +clean +test +coreJS/publishSigned +coreJVM/publishSigned +xml/publishSigned +hocon/publishSigned +yaml/publishSigned +all/publishSigned sonatypeRelease