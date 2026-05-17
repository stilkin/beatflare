package be.pocito.glyphsense.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import be.pocito.glyphsense.ui.theme.BeatFlareMagenta
import be.pocito.glyphsense.ui.theme.BeatFlareOnSurfaceDim
import java.text.BreakIterator
import java.util.Locale

private val EMOJI_PRESETS = listOf("❤️", "🦄", "⭐", "🔥", "🌊", "🎵", "✨", "💔")

/** Take the first [n] grapheme clusters of a string. Composed emoji stay intact. */
private fun String.takeGraphemes(n: Int): String {
    if (isEmpty() || n <= 0) return ""
    val it = BreakIterator.getCharacterInstance(Locale.ROOT)
    it.setText(this)
    var taken = 0
    var end = it.next()
    while (end != BreakIterator.DONE && taken < n) {
        taken++
        if (taken == n) return substring(0, end)
        end = it.next()
    }
    return this
}

/**
 * Lets the user pick (or clear) a single character/emoji shown centered on the
 * party-mode overlay. Helps a group find each other at a festival.
 */
@Composable
fun EmojiOverlaySettings(
    current: String,
    onChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "Find-me overlay",
            style = MaterialTheme.typography.bodySmall,
            color = BeatFlareOnSurfaceDim,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            EMOJI_PRESETS.forEach { emoji ->
                val isSelected = current == emoji
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) BeatFlareMagenta.copy(alpha = 0.30f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .clickable { onChange(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, fontSize = 20.sp)
                }
            }
        }

        OutlinedTextField(
            value = current,
            // Cap at 4 graphemes (user-perceived chars). Grapheme-aware so a
            // composed family emoji or country flag isn't sliced into broken bytes.
            onValueChange = { onChange(it.takeGraphemes(4)) },
            singleLine = true,
            placeholder = { Text("Custom (emoji or letter)", color = BeatFlareOnSurfaceDim) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
                capitalization = KeyboardCapitalization.Characters,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BeatFlareMagenta,
                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = BeatFlareMagenta,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (current.isNotEmpty()) {
            Text(
                "Tap to clear",
                style = MaterialTheme.typography.labelSmall,
                color = BeatFlareOnSurfaceDim,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .clickable { onChange("") },
            )
        }
    }
}
