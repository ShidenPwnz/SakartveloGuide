package com.example.sakartveloguide.domain.util

import com.example.sakartveloguide.data.local.entity.LocationEntity

fun LocationEntity.getDisplayName(lang: String): String {
    // Logic: If translated name exists, use it. Otherwise use nameEn.
    return when(lang.lowercase()) {
        "ka" -> nameKa.ifEmpty { nameEn }
        "ru" -> nameRu.ifEmpty { nameEn }
        "hy" -> nameHy.ifEmpty { nameEn }
        "iw" -> nameIw.ifEmpty { nameEn }
        "ar" -> nameAr.ifEmpty { nameEn }
        "tr" -> nameTr.ifEmpty { nameEn }
        else -> nameEn
    }
}

fun LocationEntity.getDisplayDesc(lang: String): String {
    return when(lang.lowercase()) {
        "ka" -> descKa.ifEmpty { descEn }
        "ru" -> descRu.ifEmpty { descEn }
        "hy" -> descHy.ifEmpty { descEn }
        "iw" -> descIw.ifEmpty { descEn }
        "ar" -> descAr.ifEmpty { descEn }
        "tr" -> descTr.ifEmpty { descEn }
        else -> descEn
    }
}