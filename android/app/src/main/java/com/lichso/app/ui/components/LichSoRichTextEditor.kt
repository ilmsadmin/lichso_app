package com.lichso.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lichso.app.ui.theme.LichSoThemeColors
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LichSoRichTextEditor(
    state: RichTextState,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val c = LichSoThemeColors.current

    // Định nghĩa custom styles cho headings và quote
    val h1Style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
    val h2Style = SpanStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold)
    val h3Style = SpanStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold)
    val quoteStyle = SpanStyle(fontStyle = FontStyle.Italic, color = c.textSecondary.copy(alpha = 0.8f))

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = androidx.compose.ui.text.TextStyle(
                fontSize = LichSoTextSize.label,
                fontWeight = FontWeight.SemiBold,
                color = c.textSecondary
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, c.outlineVariant, RoundedCornerShape(LichSoRadius.field))
                .background(c.surfaceContainer, RoundedCornerShape(LichSoRadius.field))
        ) {
            // Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bold
                IconButton(
                    onClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatBold,
                        contentDescription = "In đậm",
                        tint = if (state.currentSpanStyle.fontWeight == FontWeight.Bold) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Italic
                IconButton(
                    onClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatItalic,
                        contentDescription = "In nghiêng",
                        tint = if (state.currentSpanStyle.fontStyle == FontStyle.Italic) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Underline
                IconButton(
                    onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatUnderlined,
                        contentDescription = "Gạch chân",
                        tint = if (state.currentSpanStyle.textDecoration == TextDecoration.Underline) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Strikethrough
                IconButton(
                    onClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatStrikethrough,
                        contentDescription = "Gạch ngang",
                        tint = if (state.currentSpanStyle.textDecoration == TextDecoration.LineThrough) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Heading 1
                IconButton(
                    onClick = { state.toggleSpanStyle(h1Style) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "H1",
                            tint = if (state.currentSpanStyle.fontSize == h1Style.fontSize) c.primary else c.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("1", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.currentSpanStyle.fontSize == h1Style.fontSize) c.primary else c.textSecondary)
                    }
                }

                // Heading 2
                IconButton(
                    onClick = { state.toggleSpanStyle(h2Style) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "H2",
                            tint = if (state.currentSpanStyle.fontSize == h2Style.fontSize) c.primary else c.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("2", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.currentSpanStyle.fontSize == h2Style.fontSize) c.primary else c.textSecondary)
                    }
                }

                // Heading 3
                IconButton(
                    onClick = { state.toggleSpanStyle(h3Style) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatSize,
                            contentDescription = "H3",
                            tint = if (state.currentSpanStyle.fontSize == h3Style.fontSize) c.primary else c.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("3", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (state.currentSpanStyle.fontSize == h3Style.fontSize) c.primary else c.textSecondary)
                    }
                }

                // Unordered List
                IconButton(
                    onClick = { state.toggleUnorderedList() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Danh sách dấu đầu dòng",
                        tint = if (state.isUnorderedList) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Ordered List
                IconButton(
                    onClick = { state.toggleOrderedList() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatListNumbered,
                        contentDescription = "Danh sách số",
                        tint = if (state.isOrderedList) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Quote
                IconButton(
                    onClick = { state.toggleSpanStyle(quoteStyle) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = "Trích dẫn",
                        tint = if (state.currentSpanStyle.fontStyle == FontStyle.Italic && state.currentSpanStyle.color == quoteStyle.color) c.primary else c.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Dòng kẻ ngăn cách
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(c.outlineVariant)
            )

            // Editor
            RichTextEditor(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .defaultMinSize(minHeight = 120.dp),
                placeholder = placeholder?.let { { Text(it) } },
                colors = RichTextEditorDefaults.richTextEditorColors(
                    containerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                )
            )
        }
    }
}
