#!/usr/bin/env bash

sbt +clean +test docs/mdoc +publishSigned sonatypeBundleRelease