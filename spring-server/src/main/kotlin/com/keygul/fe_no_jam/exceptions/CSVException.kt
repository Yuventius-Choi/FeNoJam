package com.keygul.fe_no_jam.exceptions

class CSVException(
    message: String,
    cause: Throwable? = null
): RuntimeException(message, cause)
