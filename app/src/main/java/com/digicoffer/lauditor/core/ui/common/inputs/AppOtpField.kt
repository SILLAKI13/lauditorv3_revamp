package com.digicoffer.lauditor.core.ui.common.inputs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.placeCursorAtEnd
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

private const val ZERO_WIDTH = "\u200B"

// ─────────────────────────────────────────────────────────────────────────────
// OTPInputView  — drop-in replacement
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun OTPInputView(
    otp: String,
    onOtpChange: (String) -> Unit,
    length: Int = 6,
    modifier: Modifier = Modifier
) {
    // One TextFieldValue per box so each carries its own cursor position.
    // Pre-fill from `otp` on first composition; keeps external resets working.
    val fields = remember(length) {
        mutableStateListOf<TextFieldValue>().apply {
            repeat(length) { i ->
                val ch = otp.getOrNull(i)?.toString() ?: ""
                add(TextFieldValue(
                    text      = ch.ifEmpty { ZERO_WIDTH },
                    selection = TextRange(if (ch.isEmpty()) 1 else ch.length)
                ))
            }
        }
    }

    val focusRequesters = remember(length) { List(length) { FocusRequester() } }
    var focusedIndex    by remember { mutableIntStateOf(0) }

    // ── Sync external `otp` → fields (handles paste/clear from parent) ────────
    LaunchedEffect(otp) {
        val cleaned = otp.filter { it.isDigit() }
        if (cleaned.length == length) {
            // Full external paste (e.g. SMS autofill)
            cleaned.forEachIndexed { i, ch ->
                fields[i] = TextFieldValue(
                    text      = ch.toString(),
                    selection = TextRange(1)
                )
            }
            focusedIndex = length   // dismiss focus (no box index = length)
        } else if (cleaned.isEmpty()) {
            // External clear
            fields.indices.forEach { i ->
                fields[i] = TextFieldValue(text = ZERO_WIDTH, selection = TextRange(1))
            }
            focusedIndex = 0
        }
    }

    // ── Auto-focus first box on appear ────────────────────────────────────────
    LaunchedEffect(Unit) {
        delay(200)
        runCatching { focusRequesters[0].requestFocus() }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val gap = 8.dp
        val totalGaps = gap * (length - 1)
        val computedWidth = ((maxWidth - totalGaps) / length).coerceIn(36.dp, 52.dp)
        val computedHeight = (computedWidth * 1.15f).coerceIn(44.dp, 58.dp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(length) { index ->
                OTPBoxField(
                    fieldValue     = fields[index],
                    isFocused      = focusedIndex == index,
                    focusRequester = focusRequesters[index],
                    boxWidth       = computedWidth,
                    boxHeight      = computedHeight,
                    onFocusChange  = { focused -> if (focused) focusedIndex = index },

                    onValueChange = { newTfv ->
                        handleBoxInput(
                            newTfv          = newTfv,
                            index           = index,
                            length          = length,
                            fields          = fields,
                            focusRequesters = focusRequesters,
                            onFocusMove     = { target ->
                                focusedIndex = target.coerceIn(0, length - 1)
                                runCatching { focusRequesters[target.coerceIn(0, length - 1)].requestFocus() }
                            },
                            onOtpChange     = onOtpChange
                        )
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Input handler  — mirrors iOS handleInput(_:at:)
// ─────────────────────────────────────────────────────────────────────────────
private fun handleBoxInput(
    newTfv          : TextFieldValue,
    index           : Int,
    length          : Int,
    fields          : MutableList<TextFieldValue>,
    focusRequesters : List<FocusRequester>,
    onFocusMove     : (Int) -> Unit,
    onOtpChange     : (String) -> Unit
) {
    val raw     = newTfv.text
    val cleaned = raw.replace(ZERO_WIDTH, "").filter { it.isDigit() }

    // ── Full paste ────────────────────────────────────────────────────────────
    if (cleaned.length == length) {
        cleaned.forEachIndexed { i, ch ->
            fields[i] = TextFieldValue(text = ch.toString(), selection = TextRange(1))
        }
        onFocusMove(length - 1)
        emitOtp(fields, length, onOtpChange)
        return
    }

    // ── Partial paste (fills from current index forward) ─────────────────────
    if (cleaned.length > 1) {
        cleaned.forEachIndexed { offset, ch ->
            val target = index + offset
            if (target < length) {
                fields[target] = TextFieldValue(text = ch.toString(), selection = TextRange(1))
            }
        }
        onFocusMove((index + cleaned.length).coerceAtMost(length - 1))
        emitOtp(fields, length, onOtpChange)
        return
    }

    // ── Delete (backspace cleared the digit) ─────────────────────────────────
    if (cleaned.isEmpty()) {
        val wasAlreadyEmpty = fields[index].text.replace(ZERO_WIDTH, "").isEmpty()
        fields[index] = TextFieldValue(text = ZERO_WIDTH, selection = TextRange(1))
        emitOtp(fields, length, onOtpChange)
        if (wasAlreadyEmpty && index > 0) {
            // Box was already blank → move left (same as iOS onBackspace)
            fields[index - 1] = TextFieldValue(text = ZERO_WIDTH, selection = TextRange(1))
            onFocusMove(index - 1)
            emitOtp(fields, length, onOtpChange)
        }
        return
    }

    // ── Normal single-digit input ─────────────────────────────────────────────
    // `cleaned` is exactly 1 char here.
    // Take only the LAST digit to handle the case where the invisible char
    // is still in the string alongside the new digit.
    val digit = cleaned.last().toString()
    fields[index] = TextFieldValue(text = digit, selection = TextRange(1))
    emitOtp(fields, length, onOtpChange)
    if (index < length - 1) {
        onFocusMove(index + 1)
    }
}

// Converts the field list back to the clean OTP string expected by the parent.
private fun emitOtp(
    fields      : List<TextFieldValue>,
    length      : Int,
    onOtpChange : (String) -> Unit
) {
    val value = fields
        .take(length)
        .joinToString("") { it.text.replace(ZERO_WIDTH, "") }
    onOtpChange(value)
}

private class NoopInteractionSource : InteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
}

// ─────────────────────────────────────────────────────────────────────────────
// OTPBoxField  — one box = one hidden BasicTextField + visual overlay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun OTPBoxField(
    fieldValue     : TextFieldValue,
    isFocused      : Boolean,
    focusRequester : FocusRequester,
    boxWidth       : Dp,
    boxHeight      : Dp,
    onFocusChange  : (Boolean) -> Unit,
    onValueChange  : (TextFieldValue) -> Unit
) {
    val display = fieldValue.text.replace(ZERO_WIDTH, "")

    // ✅ Convert TextFieldValue to TextFieldState for new API
    val textFieldState = rememberTextFieldState(fieldValue.text)

    // ✅ Sync incoming fieldValue → state
    LaunchedEffect(fieldValue.text) {
        if (textFieldState.text.toString() != fieldValue.text) {
            textFieldState.edit {
                replace(0, length, fieldValue.text)
                placeCursorAtEnd()
            }
        }
    }

    // ✅ Sync state changes → parent onValueChange
    LaunchedEffect(textFieldState.text) {
        val newText = textFieldState.text.toString()
        if (newText != fieldValue.text) {
            onValueChange(TextFieldValue(text = newText, selection = TextRange(newText.length)))
        }
    }

    Box(
        modifier = Modifier
            .width(boxWidth)
            .height(boxHeight)
            .border(
                width = if (isFocused) 2.dp else 1.5.dp,
                color = if (isFocused) Color(0xFF0A63B0) else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { focusRequester.requestFocus() },
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            state          = textFieldState,
            modifier       = Modifier
                .matchParentSize()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChange(it.isFocused) },
            textStyle      = TextStyle(color = Color.Transparent, fontSize = 1.sp),
            cursorBrush    = SolidColor(Color.Transparent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

            // ✅ Disables selection handle / context menu
            interactionSource = remember { MutableInteractionSource() },

            decorator = {
                Box(Modifier.fillMaxSize())
            }
        )

        // ── Visual layer ──────────────────────────────────────────────────────
        if (display.isNotEmpty()) {
            Text(
                text       = display,
                fontSize   = 22.sp,
                color      = Color.Black
            )
        } else if (isFocused) {
            BlinkingCursor()
        }
    }
}

@Composable
fun BlinkingCursor() {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500L)
            visible = !visible
        }
    }

    Box(
        modifier = Modifier
            .width(2.dp)
            .height(24.dp)
            .background(
                color = Color(0xFF0A63B0),
                shape = RoundedCornerShape(1.dp)
            )
            .alpha(if (visible) 1f else 0f)
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// AppOtpField — canonical wrapper for OTPInputView
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AppOtpField(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    length: Int = 6,
    enabled: Boolean = true
) {
    OTPInputView(
        otp = otpValue,
        onOtpChange = onOtpChange,
        length = length,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "AppOtpField Preview")
@Composable
fun AppOtpFieldPreview() {
    AppOtpField(
        otpValue = "123456",
        onOtpChange = {}
    )
}
