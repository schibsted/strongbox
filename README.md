<p align="center">
  <a href="https://github.com/schibsted/strongbox">
    <img src="https://raw.githubusercontent.com/schibsted/strongbox/images/strongbox-logo.png?sanitize=true">
  </a>
</p>
<p align="center">
  <a title="Stronbox Travis Build" href="https://travis-ci.org/schibsted/strongbox">
    <img src="https://api.travis-ci.org/schibsted/strongbox.svg?branch=master">
  </a>
  <a title="Slack Status" href="https://slackin-bjmwohmllu.now.sh">
    <img src="https://slackin-bjmwohmllu.now.sh/badge.svg">
  </a>
</p>

Strongbox is a CLI/GUI and SDK to manage, store, and retrieve secrets (access tokens, encryption keys, private certificates, etc). Strongbox is a client-side convenience layer on top of AWS KMS, DynamoDB and IAM. It manages the AWS resources for you and configure them in a secure way.

Strongbox has been used in production since mid-2016 and is now used extensively within Schibsted. 

<img src="https://raw.githubusercontent.com/schibsted/strongbox/images/strongbox-integrations.png">

## Wiki
Please head over to the [Wiki for more detailed documentation](https://github.com/schibsted/strongbox/wiki).

## Getting Started

### Prerequisites
Strongbox relies on AWS therefore you need:
* An AWS account
* Setup `~/.aws/credentials` either
  * Manually, or
  * By installing the [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/installing.html#install-bundle-other-os), and then use the command `aws configure`

### Install
[Strongbox can be installed in several ways](https://github.com/schibsted/strongbox/wiki/Install-the-CLI)

### MFA and Proxy
Strongbox supports both using [MFA](https://github.com/schibsted/strongbox/wiki/MFA) and using a [Proxy](https://github.com/schibsted/strongbox/wiki/Proxy).

### Manage

#### GUI
The GUI can be launched via the CLI using:
```
$ strongbox --region eu-west-1 gui
```
You can then perform the same steps to create a Secrets Group and a Secret as shown with the CLI below.
<img src="https://raw.githubusercontent.com/schibsted/strongbox/images/strongbox-gui.png">

#### CLI
Create a Secrets Group (this will allocate the underlying AWS resources)
```
$ strongbox --region eu-west-1 group create team.project
```

Create a Secret (will result in a prompt to enter the secret confidentially, it can also be piped in):
```
$ strongbox --region eu-west-1 secret create --group team.project --name MySecret --value-from-stdin
```

*You can omit `--region` if you have specified a region in `~/.aws/credentials`. If you specify `--profile`, it will get the region associated with that profile, otherwise it will get the region of the default profile.*

Optional: Add a readonly IAM Principal (e.g. the role associated with the Instance Profile of your EC2 instances)
```
$ strongbox --region eu-west-1 group attach-readonly --group team.project --type role <some-iam-role>
```

### Fetch Secret

#### CLI
```
$ strongbox --region eu-west-1 secret get --group team.project --name MySecret
```
For more examples please see [the Wiki](https://github.com/schibsted/strongbox/wiki/Fetch-Secrets-With-the-CLI).

#### Java SDK
The following is from the [example repo](https://github.com/schibsted/strongbox-examples/tree/master/sdk). Add the Gradle dependency (make sure you have added the jcenter repository)
```
compile 'com.schibsted.security:strongbox-sdk:0.2.4'
```

Then retrieve the Secret using
```
SimpleSecretsGroup secretsGroup = new DefaultSimpleSecretsGroup(new SecretsGroupIdentifier(Region.EU_WEST_1, "team.project"));
Optional<String> secret = secretsGroup.getStringSecret("MySecret");
```
#### Archaius (Experimental)
Please see the [example repo](https://github.com/schibsted/strongbox-examples/tree/master/archaius).

#### Spring Boot Starter

See [spring-boot-starter/README](https://github.com/schibsted/strongbox/spring-boot-starter)

## Development status
Strongbox is in active development, and will soon allow external contributions.

## LICENSE

Copyright (c) 2016 Schibsted Products & Technology AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
