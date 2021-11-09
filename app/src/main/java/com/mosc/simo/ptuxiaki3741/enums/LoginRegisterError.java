package com.mosc.simo.ptuxiaki3741.enums;

public enum LoginRegisterError {
    NONE,

    UserNameEmptyError,
    UserNameSizeError,
    UserNameInvalidCharacterError,
    UserNameTakenError,
    UserNameWrongError,

    PasswordEmptyError,
    PasswordSizeError,
    PasswordInvalidCharacterError,
    Password2EmptyError,
    PasswordNotMatchError,
    PasswordWrongError,

    EmailEmptyError,
    EmailInvalidCharacterError,
    EmailTakenError,

    PhoneInvalidError,
}
