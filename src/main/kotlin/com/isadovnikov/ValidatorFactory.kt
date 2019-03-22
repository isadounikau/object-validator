package com.isadovnikov

import com.isadovnikov.model.ValidationClass
import com.isadovnikov.model.ValidationConstraint
import com.isadovnikov.model.ValidationField
import com.isadovnikov.model.ValidationRule
import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintDef
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext
import org.hibernate.validator.cfg.defs.PatternDef
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping
import java.lang.RuntimeException
import java.lang.annotation.ElementType
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator

class ValidatorFactory<K>(
    validationRules: List<ValidationRule<K>>
) {

    private var validators: Map<K, Validator> = mapOf()

    init {
        validationRules.map {
            it.key to it.classes
        }.forEach { pair ->
            val constraintMapping = DefaultConstraintMapping()
            pair.second.forEach {
                addClass(constraintMapping, it)
            }
            add(pair.first, constraintMapping)
        }
    }

    fun <T> validate(key: K, data: T): Set<ConstraintViolation<T>> {
        return getValidator(key).validate(data)
    }

    fun add(key: K, constraintMapping: DefaultConstraintMapping) {
        val validator = Validation
            .byProvider<HibernateValidatorConfiguration, HibernateValidator>(HibernateValidator::class.java).configure()
            .addMapping(constraintMapping).buildValidatorFactory()
            .validator

        validators = validators.plus(key to validator)
    }

    private fun addClass(constraintMapping: ConstraintMapping, validationClass: ValidationClass) {
        val clazz = Class.forName(validationClass.name)
        val type = constraintMapping.type(clazz)
        validationClass.fields.forEach { addField(type, it) }
    }

    private fun getValidator(key: K): Validator = validators[key] ?: throw RuntimeException()

    private fun addField(
        type: TypeConstraintMappingContext<out Any>,
        field: ValidationField
    ) {
        val property = type.property(field.name, ElementType.FIELD)
        field.constraints.forEach { addConstraint(property, it) }
    }

    private fun addConstraint(
        property: PropertyConstraintMappingContext,
        constraint: ValidationConstraint
    ) {
        val constraintClass = Class.forName(constraint.type).newInstance() as ConstraintDef<*, *>
        when (constraintClass) {
            is PatternDef -> {
                constraint.parameters?.firstOrNull {
                    it.key == "regex"
                }.also {
                    constraintClass.regexp(it?.value)
                }
            }
        }
        constraintClass.message(constraint.errorMessage)
        property.constraint(constraintClass)
    }

    fun getConditions(key: K) {
        //TODO implement get Conditions
    }
}
