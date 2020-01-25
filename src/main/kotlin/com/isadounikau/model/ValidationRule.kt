package com.isadounikau.model

data class ValidationRule<K>(
    val key: K,
    val classes: List<ValidationClass>
)

data class ValidationClass(
    val name: String,
    val fields: List<ValidationField>
)

data class ValidationField(
    val name: String,
    val constraints: List<ValidationConstraint>
)

data class ValidationConstraint(
    val type: String,
    val errorMessage: String?,
    val parameters: List<ConstraintParam>?
)

data class ConstraintParam(
    val key: String,
    val value: String
)
