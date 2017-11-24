# strongbox-spring-boot-starter

> Strongbox is a CLI/GUI and SDK to manage, store and retrieve secrets (access tokens, encryption keys, private certifiactes, etc). Strongbox is a client-side convenience layer on top of AWS KMS, DynamoDB and IAM. It manages the AWS resources for you and configure them in a secure way.

This is a spring-boot starter for [Strongbox](https://schibsted.com/schibsted/strongbox) that aims to make integration with Strongbox as seamless as possible for Spring Boot applications. It provides an easy way to inject secrets into properties before Spring application context is started

## Usage
### Add dependency
Find the [latest released version](https://github.com/schibsted/strongbox/releases) and add the dependency:

Maven:    

    <dependency>
        <groupId>com.schibsted.security</groupId>
        <artifactId>strongbox-spring-boot-starter</artifactId>
        <version>LATEST_VERSION</version>
    </dependency>
Gradle:

    dependencies: {
        compile "com.schibsted.security:strongbox-spring-boot-starter:LATEST_VERSION"
    }
            
You will also need to add the AWS dependencies required by Strongbox:
* `aws-java-sdk-core`
* `aws-java-sdk-dynamodb`
* `aws-java-sdk-kms`
* `aws-java-sdk-iam`
* `aws-java-sdk-sts`

AWS requires you to use the same version of all the libraries. If you are already using the AWS SDK you might want to set them explicitly to a
version greater or equal to the [Strongbox SDK AWS version](https://github.com/schibsted/strongbox/blob/master/build.gradle#L51)

    
### Configuration
Add `bootstrap.yml` to your classpath (e.g.: `src/main/resources/bootstrap.yml`) with the following configuration: `

```YAML
strongbox:
  groupname: my.service # this is the Strongbox Secret Group name 
  enabled: true
``` 
This configuration will inject the latest active version of all secrets as properties. Example: if you have a secret named `api.key`, it will create a Java property called `api.key`, which can be accessed in Spring Boot through `@Value("${api.key}")` annotation. Alternatively, a class with `@ConfigurationProperties("api")` will get the field `String key` populated with `api.key`.
You also have the option to use `@StrongboxValue` as an alias for `@Value`.    


By default, no transformation of Strongbox secret name is done.  However, you can also map strongbox secret names to property names explicitly. This can be useful if you need property names that are outside of the Strongbox naming regex: `^[a-zA-Z][a-zA-Z0-9]*([_\\-.][a-zA-Z0-9]+)*$` 

Example:
```YAML
strongbox:
  groupname: my.service
  enabled: true
  properties:
    -
      strongboxName: "sftpPassword"
      propertyName: "sftp-management.sftp.SERVICE.password"
    -
      strongboxName: "OAuthSignatureSecret"
      propertyName: "auth.domains.[example.com].clientId"

``` 
