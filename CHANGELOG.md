# Changelog

[The released artifacts can be found in Bintray](https://bintray.com/strongbox)

## 0.3.2 (2021-1-7)
 - Support newer versions of Java by including jaxb-api

## 0.2.13 (2017-12-4)
- Archaius derived property support
- Automated homebrew release
- Automated landing page release
- Minor fixes

## 0.2.7 (2017-11-24)

- Spring Boot Starter integration
- Prefix JARs with `strongbox`
- Improve error handling when credential resolving fails
- Change license to Apache 2.0

## 0.2.4 (2017-11-7)

- Improve help messages when credential resolving fails
- Prefix JARs with 'strongbox'

## 0.2.3 (2017-10-19)

- Interpret `#` at the beginning of credentials/config file line as a comment.

## 0.2.2 (2017-10-18)
- `strongbox --version` gives the version of Strongbox
- Stacktraces are no longer printed by default for exceptions, you can ebable it with `strongbox --stacktrace <command>`
- Improved error messages in the CLI

## 0.2.1 (2017-10-03)

- Added `getAll{String,Binary}Secrets()` to the `SimpleSecretsGroup` in the Java SDK

## 0.2.0 (2017-10-03)

- Initial public release
