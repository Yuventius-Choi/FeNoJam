package com.keygul.FeNoJam.data

interface BaseMapper<Domain, Data> {
    fun toDomain(data: Data): Domain
    fun toData(domain: Domain): Data?
}