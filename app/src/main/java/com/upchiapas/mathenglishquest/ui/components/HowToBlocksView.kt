package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.domain.howto.HowToBlock
import com.upchiapas.mathenglishquest.domain.howto.HowToTextStyle
import com.upchiapas.mathenglishquest.domain.howto.TableRow
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

private val CELL_WIDTH = 26.dp
private val CELL_HEIGHT = 26.dp
private val OP_WIDTH = 18.dp
private val NOTE_ROW_HEIGHT = 16.dp

/** Dibuja un procedimiento: tablas en columna, la casita de la división y los textos que los explican. */
@Composable
fun HowToBlocksView(blocks: List<HowToBlock>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is HowToBlock.Table -> ArithTableView(block)
                is HowToBlock.Text -> HowToTextView(block)
                is HowToBlock.DivisionCasita -> DivisionCasitaView(block)
            }
        }
    }
}

// ---------- Tabla en columna (.arith-table) ----------

@Composable
private fun ArithTableView(table: HowToBlock.Table) {
    val colors = AppTheme.colors
    val red = if (colors.isDark) colors.red else colors.redDeep

    // Con números muy largos la tabla se puede deslizar de lado en vez de cortarse
    Box(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            table.carryRow?.let { row ->
                NoteRow(cells = row, color = red, fontSize = 11.sp, alignment = Alignment.Center)
            }
            table.borrowRow?.let { row ->
                NoteRow(cells = row, color = red, fontSize = 9.5.sp, alignment = Alignment.TopEnd)
            }
            table.rows.forEach { row -> DigitRow(row) }
        }
    }
}

/** Fila de arriba con los acarreos (suma y multiplicación) o lo que se pide prestado (resta). */
@Composable
private fun NoteRow(
    cells: List<String>,
    color: androidx.compose.ui.graphics.Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    alignment: Alignment
) {
    Row {
        Spacer(modifier = Modifier.width(OP_WIDTH))
        cells.forEach { text ->
            Box(
                modifier = Modifier
                    .width(CELL_WIDTH)
                    .height(NOTE_ROW_HEIGHT),
                contentAlignment = alignment
            ) {
                Text(text = text, fontSize = fontSize, fontWeight = FontWeight.ExtraBold, color = color)
            }
        }
    }
}

@Composable
private fun DigitRow(row: TableRow) {
    val colors = AppTheme.colors
    val lineModifier = if (row.hasLine) {
        Modifier
            .drawBehind {
                drawLine(
                    color = colors.ink,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 3.dp.toPx()
                )
            }
            .padding(top = 3.dp)
    } else {
        Modifier
    }

    Row(modifier = lineModifier) {
        Box(
            modifier = Modifier
                .width(OP_WIDTH)
                .height(CELL_HEIGHT),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = row.op,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
        row.cells.forEach { text ->
            Box(
                modifier = Modifier
                    .width(CELL_WIDTH)
                    .height(CELL_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (row.isResult) colors.blueText else colors.ink
                )
            }
        }
    }
}

// ---------- Textos ----------

@Composable
private fun HowToTextView(block: HowToBlock.Text) {
    val colors = AppTheme.colors
    when (block.style) {
        HowToTextStyle.STEP -> Text(
            text = block.text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 18.sp,
            color = colors.ink.copy(alpha = 0.85f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )
        HowToTextStyle.RESULT -> Text(
            text = block.text,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
        HowToTextStyle.NOTE -> Text(
            text = block.text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 17.sp,
            color = colors.mute,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp)
        )
        HowToTextStyle.LABEL -> Text(
            text = block.text,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 17.sp,
            color = colors.mute,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- La casita de la división (como en el cuaderno) ----------

private val CASITA_ROW_HEIGHT = 26.dp
private val CASITA_SIGN_WIDTH = 16.dp
private val CASITA_DIGIT_WIDTH = 22.dp
private val CASITA_DOT_WIDTH = 9.dp

/**
 * Cociente arriba, divisor a la izquierda, dividendo adentro y debajo las restas bajando cifras.
 * Todo está en una cuadrícula de columnas para que cada cifra quede justo debajo de la que le corresponde.
 */
@Composable
private fun DivisionCasitaView(casita: HowToBlock.DivisionCasita) {
    val colors = AppTheme.colors
    val widths = casita.columnIsDot.mapIndexed { index, isDot ->
        when {
            index == 0 -> CASITA_SIGN_WIDTH
            isDot -> CASITA_DOT_WIDTH
            else -> CASITA_DIGIT_WIDTH
        }
    }

    // Con muchas filas o cifras la casita se puede deslizar de lado en vez de cortarse
    Box(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 6.dp)
    ) {
        Row {
            // El divisor, con su raya vertical, a la altura del dividendo
            Column {
                Spacer(modifier = Modifier.height(CASITA_ROW_HEIGHT))
                Box(
                    modifier = Modifier
                        .height(CASITA_ROW_HEIGHT)
                        .drawBehind {
                            drawLine(
                                color = colors.ink,
                                start = Offset(size.width, 0f),
                                end = Offset(size.width, size.height),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        .padding(start = 2.dp, end = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = casita.divisor,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.ink
                    )
                }
            }

            Column {
                CasitaRowView(cells = casita.quotient, widths = widths, color = colors.blueText)
                CasitaRowView(
                    cells = casita.dividend,
                    widths = widths,
                    color = colors.ink,
                    dimFromCol = casita.appendedFromCol,
                    topLine = true
                )
                casita.rows.forEach { row ->
                    CasitaRowView(
                        cells = row.cells,
                        widths = widths,
                        color = colors.ink,
                        underlineFromCol = row.underlineFromCol,
                        underlineToCol = row.underlineToCol
                    )
                }
            }
        }
    }
}

@Composable
private fun CasitaRowView(
    cells: List<String>,
    widths: List<androidx.compose.ui.unit.Dp>,
    color: androidx.compose.ui.graphics.Color,
    dimFromCol: Int = -1,
    topLine: Boolean = false,
    underlineFromCol: Int = -1,
    underlineToCol: Int = -1
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .height(CASITA_ROW_HEIGHT)
            .drawBehind {
                if (topLine) {
                    drawLine(
                        color = colors.ink,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
                if (underlineFromCol >= 0) {
                    val startX = (0 until underlineFromCol).sumOf { widths[it].toPx().toDouble() }.toFloat()
                    val endX = (0..underlineToCol).sumOf { widths[it].toPx().toDouble() }.toFloat()
                    drawLine(
                        color = colors.ink,
                        start = Offset(startX, size.height),
                        end = Offset(endX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
    ) {
        cells.forEachIndexed { index, text ->
            Box(
                modifier = Modifier
                    .width(widths[index])
                    .height(CASITA_ROW_HEIGHT),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    softWrap = false,
                    // Los ceros que se agregaron al dividendo se ven atenuados: no estaban en el número original
                    color = if (dimFromCol in 0..index) colors.mute else color
                )
            }
        }
    }
}
