package com.mosc.simo.ptuxiaki3741.util;

import com.mosc.simo.ptuxiaki3741.enums.LoginRegisterError;
import com.mosc.simo.ptuxiaki3741.models.entities.User;
import com.mosc.simo.ptuxiaki3741.values.AppValues;

import org.apache.commons.validator.routines.EmailValidator;

public final class UserUtil {
    private UserUtil(){}
    public static LoginRegisterError checkData(String username, String password) {
        if(username.isEmpty()) return LoginRegisterError.UserNameEmptyError;
        if(password.isEmpty()) return LoginRegisterError.PasswordEmptyError;
        return LoginRegisterError.NONE;
    }
    public static LoginRegisterError checkData(
            String username, String password, String password2, String email, String email2, String phone
    ) {
        if(username.length()< AppValues.minUserNameFieldSize || username.length()>AppValues.maxUserNameFieldSize)
            return LoginRegisterError.UserNameSizeError;
        if(!username.matches("[a-zA-Z0-9]+"))
            return LoginRegisterError.UserNameInvalidCharacterError;

        if(password.length()<AppValues.minPasswordFieldSize|| password.length()>AppValues.maxPasswordFieldSize)
            return LoginRegisterError.PasswordSizeError;
        if(!password.matches("[a-zA-Z0-9]+"))
            return LoginRegisterError.PasswordInvalidCharacterError;
        if(password2.isEmpty())
            return LoginRegisterError.Password2EmptyError;
        if(!password.equals(password2))
            return LoginRegisterError.PasswordNotMatchError;

        if(email.isEmpty())
            return LoginRegisterError.EmailEmptyError;
        if(!EmailValidator.getInstance().isValid(email))
            return LoginRegisterError.EmailInvalidCharacterError;
        if(email2.isEmpty())
            return LoginRegisterError.Email2EmptyError;
        if(!email.equals(email2))
            return LoginRegisterError.EmailNotMatchError;

        if(isPhoneInvalid(phone))
            return LoginRegisterError.PhoneInvalidError;

        return LoginRegisterError.NONE;
    }
    public static LoginRegisterError checkUserData(User user) {
        if(user.getEmail().isEmpty())
            return LoginRegisterError.EmailEmptyError;
        if(!EmailValidator.getInstance().isValid(user.getEmail()))
            return LoginRegisterError.EmailInvalidCharacterError;

        if(isPhoneInvalid(user.getPhone()))
            return LoginRegisterError.PhoneInvalidError;

        return LoginRegisterError.NONE;
    }
    private static boolean isPhoneInvalid(String phone) {
        if(phone.isEmpty()) return false;
        return !phone.matches(AppValues.phoneRegex);
    }
}
