# object-validator 
[![Build Status](https://travis-ci.com/Sadovnikov94/object-validator.svg?branch=master)](https://travis-ci.com/Sadovnikov94/object-validator)
[![codecov](https://codecov.io/gh/Sadovnikov94/object-validator/branch/master/graph/badge.svg)](https://codecov.io/gh/Sadovnikov94/object-validator)

Simple Object Validator based on Hibernate validation library

Rules can be build by code or in the JSON format 

* `KeyObject` is any object you want to use as a key for your validation rules 
* `classes` is a classes you want to validate 
* `fields` is a fields you want to validate 
* `constraints` is validation rules 

```
val key = KeyObject(officeId = 57, anotherField = "value")
val rule = DefaultConstraintMapping()
rule.type(TestDTO::class.java)
   .property("id", ElementType.FIELD)
   .constraint(NotEmptyDef().message("errorMessage"))
   .constraint(PatternDef().message("errorMessage").regexp("\\b[A-Z0-9._%-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b"))

validatorFactory.add(key, rule)
validatorFactory.validate(key, TestDTO("non valid"))
```

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
