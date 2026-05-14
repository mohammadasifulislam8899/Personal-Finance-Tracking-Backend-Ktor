package com.xentoryx.finance_tracker.export

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.xentoryx.finance_tracker.domain.model.Transaction
import com.xentoryx.finance_tracker.domain.model.TransactionType
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PdfExportService {

    // ── Colors ────────────────────────────────────────────────────────────
    private val primaryColor   = DeviceRgb(79, 70, 229)    // Indigo
    private val incomeColor    = DeviceRgb(34, 197, 94)    // Green
    private val expenseColor   = DeviceRgb(239, 68, 68)    // Red
    private val headerBg       = DeviceRgb(243, 244, 246)  // Light gray
    private val borderColor    = DeviceRgb(229, 231, 235)  // Gray-200
    private val textMuted      = DeviceRgb(107, 114, 128)  // Gray-500

    private val dateFormatter  = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

    // ── Main export function ──────────────────────────────────────────────
    fun generateTransactionReport(
        userName: String,
        currencyCode: String,
        transactions: List<Transaction>,
        categoryNames: Map<java.util.UUID, String>,
        accountNames: Map<java.util.UUID, String>,
        from: LocalDate,
        to: LocalDate
    ): ByteArray {

        val out    = ByteArrayOutputStream()
        val writer = PdfWriter(out)
        val pdf    = PdfDocument(writer)
        val doc    = Document(pdf, PageSize.A4)

        doc.setMargins(36f, 36f, 36f, 36f)

        val boldFont   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val regularFont = PdfFontFactory.createFont(StandardFonts.HELVETICA)

        // ── Header ────────────────────────────────────────────────────────
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .setWidth(UnitValue.createPercentValue(100f))
            .setBorder(Border.NO_BORDER)

        headerTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .add(Paragraph("Finance Tracker")
                    .setFont(boldFont).setFontSize(22f)
                    .setFontColor(primaryColor))
                .add(Paragraph("Transaction Report")
                    .setFont(regularFont).setFontSize(11f)
                    .setFontColor(textMuted))
        )

        headerTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(Paragraph(userName)
                    .setFont(boldFont).setFontSize(12f))
                .add(Paragraph("${from.format(dateFormatter)} – ${to.format(dateFormatter)}")
                    .setFont(regularFont).setFontSize(10f)
                    .setFontColor(textMuted))
                .add(Paragraph("Generated: ${LocalDate.now().format(dateFormatter)}")
                    .setFont(regularFont).setFontSize(9f)
                    .setFontColor(textMuted))
        )

        doc.add(headerTable)
        doc.add(Paragraph("\n"))

        // ── Divider ───────────────────────────────────────────────────────
        doc.add(Table(1)
            .setWidth(UnitValue.createPercentValue(100f))
            .addCell(Cell().setHeight(2f)
                .setBackgroundColor(primaryColor)
                .setBorder(Border.NO_BORDER))
        )
        doc.add(Paragraph("\n"))

        // ── Summary Cards ─────────────────────────────────────────────────
        val totalIncome  = transactions
            .filter { it.type == TransactionType.INCOME }
            .fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .fold(BigDecimal.ZERO) { acc, t -> acc + t.amount }

        val net = totalIncome - totalExpense

        val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(33f, 33f, 34f)))
            .setWidth(UnitValue.createPercentValue(100f))

        summaryTable.addCell(summaryCard("Total Income", formatAmount(totalIncome, currencyCode), incomeColor, boldFont, regularFont))
        summaryTable.addCell(summaryCard("Total Expense", formatAmount(totalExpense, currencyCode), expenseColor, boldFont, regularFont))
        summaryTable.addCell(summaryCard(
            "Net Balance",
            formatAmount(net, currencyCode),
            if (net >= BigDecimal.ZERO) incomeColor else expenseColor,
            boldFont,
            regularFont
        ))

        doc.add(summaryTable)
        doc.add(Paragraph("\n"))

        // ── Transaction count ─────────────────────────────────────────────
        doc.add(Paragraph("Transactions (${transactions.size})")
            .setFont(boldFont).setFontSize(13f)
            .setFontColor(primaryColor)
        )
        doc.add(Paragraph("\n").setFontSize(4f))

        // ── Transaction Table ─────────────────────────────────────────────
        if (transactions.isEmpty()) {
            doc.add(Paragraph("No transactions found for the selected period.")
                .setFont(regularFont).setFontSize(11f)
                .setFontColor(textMuted)
                .setTextAlignment(TextAlignment.CENTER)
            )
        } else {
            val cols = floatArrayOf(14f, 28f, 18f, 20f, 20f)
            val txTable = Table(UnitValue.createPercentArray(cols))
                .setWidth(UnitValue.createPercentValue(100f))

            // Table header
            listOf("Date", "Category / Note", "Account", "Type", "Amount").forEach { label ->
                txTable.addHeaderCell(
                    Cell().setBackgroundColor(headerBg)
                        .setBorderTop(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(Border.NO_BORDER)
                        .setBorderBottom(SolidBorder(borderColor, 1f))
                        .add(Paragraph(label)
                            .setFont(boldFont).setFontSize(9f)
                            .setFontColor(textMuted))
                )
            }

            // Table rows
            transactions.sortedByDescending { it.transactionDate }.forEach { tx ->
                val categoryName = categoryNames[tx.categoryId] ?: "Unknown"
                val accountName  = accountNames[tx.accountId]   ?: "Unknown"
                val isIncome     = tx.type == TransactionType.INCOME
                val amountColor  = if (isIncome) incomeColor else expenseColor
                val amountPrefix = if (isIncome) "+" else "-"

                txTable.addCell(dataCell(tx.transactionDate.format(dateFormatter), regularFont, 9f))
                txTable.addCell(
                    Cell().setBorderTop(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(Border.NO_BORDER)
                        .setBorderBottom(SolidBorder(borderColor, 0.5f))
                        .add(Paragraph(categoryName).setFont(boldFont).setFontSize(9f))
                        .add(Paragraph(tx.note ?: "").setFont(regularFont).setFontSize(8f).setFontColor(textMuted))
                )
                txTable.addCell(dataCell(accountName, regularFont, 9f))
                txTable.addCell(
                    Cell().setBorderTop(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(Border.NO_BORDER)
                        .setBorderBottom(SolidBorder(borderColor, 0.5f))
                        .add(Paragraph(tx.type.name)
                            .setFont(boldFont).setFontSize(8f)
                            .setFontColor(if (isIncome) incomeColor else expenseColor))
                )
                txTable.addCell(
                    Cell().setBorderTop(Border.NO_BORDER)
                        .setBorderLeft(Border.NO_BORDER)
                        .setBorderRight(Border.NO_BORDER)
                        .setBorderBottom(SolidBorder(borderColor, 0.5f))
                        .setTextAlignment(TextAlignment.RIGHT)
                        .add(Paragraph("$amountPrefix ${formatAmount(tx.amount, currencyCode)}")
                            .setFont(boldFont).setFontSize(9f)
                            .setFontColor(amountColor))
                )
            }

            doc.add(txTable)
        }

        // ── Footer ────────────────────────────────────────────────────────
        doc.add(Paragraph("\n"))
        doc.add(Table(1)
            .setWidth(UnitValue.createPercentValue(100f))
            .addCell(Cell().setHeight(1f)
                .setBackgroundColor(borderColor)
                .setBorder(Border.NO_BORDER))
        )
        doc.add(
            Paragraph("This report was automatically generated by Finance Tracker · ${LocalDate.now().format(dateFormatter)}")
                .setFont(regularFont).setFontSize(8f)
                .setFontColor(textMuted)
                .setTextAlignment(TextAlignment.CENTER)
        )

        doc.close()
        return out.toByteArray()
    }

    // ── Helper: Summary card cell ─────────────────────────────────────────
    private fun summaryCard(
        label: String,
        value: String,
        valueColor: DeviceRgb,
        boldFont: com.itextpdf.kernel.font.PdfFont,
        regularFont: com.itextpdf.kernel.font.PdfFont
    ): Cell {
        return Cell()
            .setBackgroundColor(headerBg)
            .setBorder(SolidBorder(borderColor, 1f))
            .setPadding(10f)
            .add(Paragraph(label)
                .setFont(regularFont).setFontSize(9f)
                .setFontColor(textMuted))
            .add(Paragraph(value)
                .setFont(boldFont).setFontSize(14f)
                .setFontColor(valueColor))
    }

    // ── Helper: Data cell ─────────────────────────────────────────────────
    private fun dataCell(text: String, font: com.itextpdf.kernel.font.PdfFont, size: Float): Cell {
        return Cell()
            .setBorderTop(Border.NO_BORDER)
            .setBorderLeft(Border.NO_BORDER)
            .setBorderRight(Border.NO_BORDER)
            .setBorderBottom(SolidBorder(borderColor, 0.5f))
            .add(Paragraph(text).setFont(font).setFontSize(size))
    }

    // ── Helper: Format amount ─────────────────────────────────────────────
    private fun formatAmount(amount: BigDecimal, currency: String): String {
        return "$currency ${amount.setScale(2, RoundingMode.HALF_UP)}"
    }
}