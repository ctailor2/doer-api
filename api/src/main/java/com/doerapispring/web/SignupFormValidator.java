package com.doerapispring.web;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

class SignupFormValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(SignupForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "identifier", "NotEmpty");
        ValidationUtils.rejectIfEmpty(errors, "credentials", "NotEmpty");
    }
}
