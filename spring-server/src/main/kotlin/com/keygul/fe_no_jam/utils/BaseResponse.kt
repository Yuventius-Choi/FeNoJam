package com.keygul.fe_no_jam.utils

import kotlinx.serialization.Serializable

@Serializable
data class BaseResponse<T>(
    val code: Int = 200,
    val message: String = "성공",
    val body: T? = null

) {
    companion object {
        fun <T> ok(body: T? = null): BaseResponse<T> {
            return BaseResponse(
                body = body
            )
        }

        fun <Any> error(message: String = "파라미터가 잘못되었습니다."): BaseResponse<Any> {
            return BaseResponse(code = 400, message = message, body = null)
        }

        fun <Any> error(code: Int, message: String): BaseResponse<Any> {
            return BaseResponse(code = code, message = message, body = null)
        }
    }
}
