# elephant

[![gradle-version](https://img.shields.io/badge/gradle-5.5.1-brightgreen)](https://img.shields.io/badge/gradle-5.5.1-brightgreen)

Import twitter lists from json formatted file to twitter account. 

### Usage
clone project and set required properties in elephant.properties file, and then execute: 

```groovy
gradle run
```

#### elephant.properties

```properties
elephant-consumer-key=
elephant-consumer-secret=
elephant-access-token=
elephant-access-token-secret=
import-file=
```

#### example import-file

```json
[
  {
    "name": "twitter-list",
    "description": "",
    "members": [
      {
        "id": 1,
        "screenName": "a"
      },
      {
        "id": 2,
        "screenName": "b"
      },
      {
        "id": 3,
        "screenName": "c"
      }
    ]
  }
]
```