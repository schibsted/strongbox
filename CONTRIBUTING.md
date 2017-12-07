
# Contributing to Strongbox

Thanks for taking the time to contribute! :+1::tada:

## Code of Conduct
The project is governed by the [Strongbox Code of Conduct](CODE_OF_CONDUCT.md). Please report unacceptable behavior to strongbox@schibsted.com.

## License
Strongbox is released under the [Apache 2.0 License](LICENSE). Any code you submit will be released under the same license,
as per section 5 in the [Apache 2.0 License](LICENSE):
```      
  Unless You explicitly state otherwise,
  any Contribution intentionally submitted for inclusion in the Work
  by You to the Licensor shall be under the terms and conditions of
  this License, without any additional terms or conditions.
  Notwithstanding the above, nothing herein shall supersede or modify
  the terms of any separate license agreement you may have executed
  with Licensor regarding such Contributions.
```

## Practical information
We expect expect submissions to be consistent with existing code in terms of architecture, coding style and testing.

#### Build project and run tests
```
./gradlew build
```

#### Build the CLI
```
./gradlew install
```

#### Run integration tests
***This will allocate resources in AWS**. They should be cleaned up on the same run or garbage collected by a later run.
You should make sure you have your costs under control. KMS keys can for instance not be deleted before after 7 days,
and each run will allocate new keys.*
```
./gradlew integrationTest
```
