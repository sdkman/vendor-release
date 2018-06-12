# Vendor Release API

[![Build Status](https://travis-ci.org/sdkman/vendor-release.svg?branch=master)](https://travis-ci.org/sdkman/vendor-release)

Used by Vendors for releasing new Candidate Versions on SDKMAN!

### Run locally

    $ docker run --rm -d -p="27017:27017" --name=mongo mongo:3.2
    $ ./sbtw run

### Test

    $ ./sbtw acc:test

### Release

    $ ./sbtw release

