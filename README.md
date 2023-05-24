# Vendor Release API

![Build status](https://github.com/sdkman/vendor-release/actions/workflows/release.yml/badge.svg)
![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/sdkman/vendor-release)

Used by vendors for releasing new candidate versions on SDKMAN!

### Test

    $ sbt test

### Run locally

    $ docker run --rm -d -p="27017:27017" --name=mongo mongo:3.2
    $ docker run \
        --name postgres \
        -p 5432:5432 \
        -e POSTGRES_USER=postgres \
        -e POSTGRES_PASSWORD=postgres \
        -e POSTGRES_DB=sdkman \
        -d postgres
    $ sbt run
