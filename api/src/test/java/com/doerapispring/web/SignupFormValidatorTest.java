package com.doerapispring.web;

import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SignupFormValidatorTest {

    private SignupFormValidator signupFormValidator;

    @Before
    public void setUp() throws Exception {
        signupFormValidator = new SignupFormValidator();
    }

    @Test
    public void hasNoErrorsForValidForm() {
        SignupForm signupForm = new SignupForm("someIdentifier", "someCredentials");
        DirectFieldBindingResult errors = new DirectFieldBindingResult(signupForm, "signupForm");

        signupFormValidator.validate(signupForm, errors);

        List<FieldError> fieldErrors = errors.getFieldErrors();
        assertThat(fieldErrors).isEmpty();
    }

    @Test
    public void rejectsMissingIdentifierField() {
        SignupForm signupForm = new SignupForm(null, "someCredentials");
        DirectFieldBindingResult errors = new DirectFieldBindingResult(signupForm, "signupForm");

        signupFormValidator.validate(signupForm, errors);

        FieldError fieldError = errors.getFieldError("identifier");
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getCode()).isEqualTo("NotEmpty");
    }

    @Test
    public void rejectsEmptyIdentifierField() {
        SignupForm signupForm = new SignupForm("", "someCredentials");
        DirectFieldBindingResult errors = new DirectFieldBindingResult(signupForm, "signupForm");

        signupFormValidator.validate(signupForm, errors);

        FieldError fieldError = errors.getFieldError("identifier");
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getCode()).isEqualTo("NotEmpty");
    }

    @Test
    public void rejectsMissingCredentialsField() {
        SignupForm signupForm = new SignupForm("someIdentifier", null);
        DirectFieldBindingResult errors = new DirectFieldBindingResult(signupForm, "signupForm");

        signupFormValidator.validate(signupForm, errors);

        FieldError fieldError = errors.getFieldError("credentials");
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getCode()).isEqualTo("NotEmpty");
    }

    @Test
    public void rejectsEmptyCredentialsField() {
        SignupForm signupForm = new SignupForm("someIdentifier", "");
        DirectFieldBindingResult errors = new DirectFieldBindingResult(signupForm, "signupForm");

        signupFormValidator.validate(signupForm, errors);

        FieldError fieldError = errors.getFieldError("credentials");
        assertThat(fieldError).isNotNull();
        assertThat(fieldError.getCode()).isEqualTo("NotEmpty");
    }
}