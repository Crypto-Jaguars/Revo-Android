package com.example.fideicomisoapproverring.theme.icons

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcAbout
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcArrowTopRight
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcCalendarChecked
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcCategories
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcFaq
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcGroup
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcSettings
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcUpwardTrend
import com.example.fideicomisoapproverring.theme.icons.ringcore.IcWallet
import kotlin.collections.List as ____KtList

object RingCore

private var __AllIcons: ____KtList<ImageVector>? = null

val RingCore.AllIcons: ____KtList<ImageVector>
    get() {
        if (__AllIcons != null) {
            return __AllIcons!!
        }
        __AllIcons = listOf(IcAbout, IcArrowTopRight, IcCategories, IcFaq, IcGroup, IcSettings, IcUpwardTrend, IcWallet, IcCalendarChecked)
        return __AllIcons!!
    }
