package com.example.sakartveloguide.domain.util

import com.example.sakartveloguide.data.local.entity.LocationEntity

fun LocationEntity.getDisplayName(lang: String): String = when(lang) {
    "ka" -> nameKa.ifEmpty { nameEn }
    "ru" -> nameRu.ifEmpty { nameEn }
    "tr" -> nameTr.ifEmpty { nameEn }
    "hy" -> nameHy.ifEmpty { nameEn }
    "iw" -> nameIw.ifEmpty { nameEn }
    "ar" -> nameAr.ifEmpty { nameEn }
    else -> nameEn
}

fun LocationEntity.getDisplayDesc(lang: String): String = when(lang) {
    "ka" -> descKa.ifEmpty { descEn }
    "ru" -> descRu.ifEmpty { descEn }
    "tr" -> descTr.ifEmpty { descEn }
    "hy" -> descHy.ifEmpty { descEn }
    "iw" -> descIw.ifEmpty { descEn }
    "ar" -> descAr.ifEmpty { descEn }
    else -> descEn
}