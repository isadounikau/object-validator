package com.isadovnikov

import com.google.gson.Gson
import org.junit.Test
import com.isadovnikov.model.ValidationRule


class ValidatorFactoryTest {

    private val validatorFactory: ValidatorFactory<String>

    init {
        val fileText = this::class.java.classLoader.getResource("test-rules-list.json").readText()
        val rule = Gson().fromJson<ValidationRule<String>>(fileText, ValidationRule::class.java)
        validatorFactory = ValidatorFactory(listOf(rule))
    }

    @Test
    fun validate() {
        val key = "key"
        val test = TestDTO("", "")

        val result = validatorFactory.validate(key, test)
        println(result)
    }
}

data class TestDTO(
    val id: String,
    val name: String
)
