package com.isadounikau

import com.isadounikau.model.*
import org.hibernate.validator.HibernateValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.cfg.ConstraintDef
import org.hibernate.validator.cfg.ConstraintMapping
import org.hibernate.validator.cfg.context.PropertyConstraintMappingContext
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorManager
import org.hibernate.validator.internal.metadata.core.ConstraintHelper
import org.hibernate.validator.internal.metadata.core.MetaConstraint
import org.hibernate.validator.internal.metadata.raw.ConstrainedElement
import org.hibernate.validator.internal.util.TypeResolutionHelper
import java.lang.RuntimeException
import java.lang.annotation.ElementType
import javax.validation.ConstraintViolation
import javax.validation.Validation
import javax.validation.Validator

class ValidatorFactoryImpl<K>(
    validationRules: List<ValidationRule<K>>
) : ValidatorFactory<K> {

    private var validators: Map<K, Validator> = mapOf()
    private var constraintMappings: Map<K, DefaultConstraintMapping> = mapOf()

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

    override fun <T> validate(key: K, data: T): Set<ConstraintViolation<T>> {
        return getValidator(key).validate(data)
    }

    override fun add(key: K, constraintMapping: DefaultConstraintMapping) {
        val validator = Validation
            .byProvider<HibernateValidatorConfiguration, HibernateValidator>(HibernateValidator::class.java).configure()
            .addMapping(constraintMapping).buildValidatorFactory()
            .validator

        constraintMappings = constraintMappings.plus(key to constraintMapping)
        validators = validators.plus(key to validator)
    }

    private fun addClass(constraintMapping: ConstraintMapping, validationClass: ValidationClass) {
        val clazz = Class.forName(validationClass.name)
        val type = constraintMapping.type(clazz)
        validationClass.fields.forEach { addField(type, it) }
    }

    override fun getConstraints(key: K): ValidationRule<K> {
        val constraintMapping = constraintMappings[key] ?: throw RuntimeException()
        val beans =
            constraintMapping.getBeanConfigurations(constraintHelper, typeResolutionHelper, valueExtractorManager)

        val classes = beans.map {
            ValidationClass(
                it.beanClass.name,
                it.constrainedElements.filterNot { ConstrainedElement.ConstrainedElementKind.TYPE == it.kind }
                    .map {
                        this.getField(
                            it
                        )
                    }
            )
        }

        return ValidationRule(key, classes)

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
        constraint.errorMessage?.also {
            constraintClass.message(it)
        }
        constraint.parameters?.forEach {
            val method = constraintClass.javaClass.getMethod(it.key, String::class.java)
            method.invoke(constraintClass, it.value)
        }
        property.constraint(constraintClass)
    }

    private fun getField(it: ConstrainedElement) = ValidationField(
        name = it.constraints.firstOrNull()?.location?.member?.name ?: "",
        constraints = it.constraints.map { this.getConstraint(it) }
    )


    private fun getConstraint(constraint: MetaConstraint<*>): ValidationConstraint {
        val attributes = constraint.descriptor.attributes
        val parameters = attributes.entries
            .filter { it.value is String }
            .map { ConstraintParam(it.key, it.value as String) }

        return ValidationConstraint(
            constraint.descriptor.annotationType.name,
            constraint.descriptor.messageTemplate,
            parameters
        )
    }

    companion object {
        private var constraintHelper = ConstraintHelper()
        private var typeResolutionHelper = TypeResolutionHelper()
        private var valueExtractorManager = ValueExtractorManager(setOf())
    }
}

interface ValidatorFactory<K> {

    fun <T> validate(key: K, data: T): Set<ConstraintViolation<T>>

    fun add(key: K, constraintMapping: DefaultConstraintMapping)

    fun getConstraints(key: K): ValidationRule<K>
}

