package com.keygul.fe_no_jam.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SampleController {
    @GetMapping("/health-check")
    fun healthCheck(): String = "리슨투마핫빗"
}
