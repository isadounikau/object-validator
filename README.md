# object-validator 
[![Build Status](https://travis-ci.com/Sadovnikov94/object-validator.svg?branch=master)](https://travis-ci.com/Sadovnikov94/object-validator)
[![codecov](https://codecov.io/gh/Sadovnikov94/object-validator/branch/master/graph/badge.svg)](https://codecov.io/gh/Sadovnikov94/object-validator)

Simple Object Validator based on Hibernate validation library

Representation of a file with rules for validation:
```
{
  "key": {
    "officeId": 57,
    "anotherField": "value"
  },
  "classes": [
    {
      "name": "com.isadovnikov.TestDTO",
      "fields": [
        {
          "name": "id",
          "constraints": [
            {
              "type": "org.hibernate.validator.cfg.defs.NotEmptyDef",
              "errorMessage": "error"
            },
            {
              "type": "org.hibernate.validator.cfg.defs.PatternDef",
              "errorMessage": "error",
              "parameters": [
                {
                  "key": "regexp",
                  "value": "\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b"
                }
              ]
            }
          ]
        }
      ]
    }
  ]
}
```
