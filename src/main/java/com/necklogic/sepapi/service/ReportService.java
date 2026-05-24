package com.necklogic.sepapi.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.necklogic.sepapi.model.Finance;
import com.necklogic.sepapi.model.Lesson;
import com.necklogic.sepapi.model.Student;
import com.necklogic.sepapi.model.enums.PaymentStatus;
import com.necklogic.sepapi.model.enums.LessonStatus;
import com.necklogic.sepapi.repository.FinanceRepository;
import com.necklogic.sepapi.repository.LessonRepository;
import com.necklogic.sepapi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StudentRepository studentRepository;
    private final LessonRepository lessonRepository;
    private final FinanceRepository financeRepository;

    public byte[] generateStudentReport(UUID studentId, UUID professorId, LocalDateTime start, LocalDateTime end) {
        Student student = studentRepository.findByIdAndProfessorId(studentId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Lesson> lessons = lessonRepository.findAllByStudentIdAndDateTimeBetweenOrderByDateTimeAsc(studentId, start, end);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            buildReportHeader(document, student, start, end);
            buildMetricsSection(document, lessons);
            buildLessonsTable(document, lessons);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating PDF");
        }
    }

    private void buildReportHeader(Document document, Student student, LocalDateTime start, LocalDateTime end) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Relatório de Acompanhamento", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        document.add(new Paragraph("Aluno: " + student.getName(), infoFont));
        document.add(new Paragraph("Disciplina: " + student.getSubject(), infoFont));
        document.add(new Paragraph("Período: " + start.format(formatter) + " a " + end.format(formatter), infoFont));
        document.add(new Paragraph("\n"));
    }

    private void buildMetricsSection(Document document, List<Lesson> lessons) throws DocumentException {
        long total = lessons.size();
        long attended = lessons.stream().filter(l -> l.getStatus() == LessonStatus.COMPLETED).count();
        long missed = lessons.stream().filter(l -> l.getStatus() == LessonStatus.CANCELED).count();

        Font metricsFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        document.add(new Paragraph(String.format("Aulas Agendadas: %d | Realizadas: %d | Faltas: %d", total, attended, missed), metricsFont));
        document.add(new Paragraph("\n"));
    }

    private void buildLessonsTable(Document document, List<Lesson> lessons) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 1.5f, 5f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        table.addCell(new PdfPCell(new Phrase("Data", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Status", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Log da Aula", headerFont)));

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        for (Lesson lesson : lessons) {
            table.addCell(new PdfPCell(new Phrase(lesson.getDateTime().format(formatter), cellFont)));

            String statusTranslation = translateStatus(lesson.getStatus());
            table.addCell(new PdfPCell(new Phrase(statusTranslation, cellFont)));

            String log = lesson.getPublicLog() != null && !lesson.getPublicLog().isBlank() ? lesson.getPublicLog() : "-";
            table.addCell(new PdfPCell(new Phrase(log, cellFont)));
        }

        document.add(table);
    }

    private String translateStatus(LessonStatus status) {
        return switch (status) {
            case SCHEDULED -> "Agendada";
            case COMPLETED -> "Concluída";
            case CANCELED -> "Cancelada/Falta";
        };
    }

    public byte[] generateFinanceReport(UUID professorId, LocalDate start, LocalDate end) {
        
        List<Finance> finances = financeRepository.findAllByProfessorIdAndDueDateBetweenOrderByDueDateDesc(professorId, start, end);

        String professorIdentifier = professorId.toString().substring(0, 8) + "...";

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            buildFinanceHeader(document, professorIdentifier, start, end);
            buildFinanceMetrics(document, finances);
            buildFinanceTable(document, finances);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error generating Finance PDF");
        }
    }

    private void buildFinanceHeader(Document document, String professorName, LocalDate start, LocalDate end) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("Relatório Financeiro", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        document.add(new Paragraph("Professor (ID): " + professorName, infoFont));
        document.add(new Paragraph("Período: " + start.format(formatter) + " a " + end.format(formatter), infoFont));
        document.add(new Paragraph("\n"));
    }

    private void buildFinanceMetrics(Document document, List<Finance> finances) throws DocumentException {
        BigDecimal totalPaid = BigDecimal.ZERO;
        BigDecimal totalOverdue = BigDecimal.ZERO;

        for (Finance finance : finances) {
            if (finance.getStatus() == PaymentStatus.PAID) {
                totalPaid = totalPaid.add(finance.getAmount());
            } else if (finance.getStatus() == PaymentStatus.OVERDUE) {
                totalOverdue = totalOverdue.add(finance.getAmount());
            }
        }

        Font metricsFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        document.add(new Paragraph(String.format("Total Recebido (PAID): R$ %s | Inadimplência (OVERDUE): R$ %s", 
                totalPaid.toString(), totalOverdue.toString()), metricsFont));
        document.add(new Paragraph("\n"));
    }

    private void buildFinanceTable(Document document, List<Finance> finances) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2f, 4f, 2f, 2f});

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        table.addCell(new PdfPCell(new Phrase("Vencimento", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Descrição", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Valor (R$)", headerFont)));
        table.addCell(new PdfPCell(new Phrase("Status", headerFont)));

        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Finance finance : finances) {
            table.addCell(new PdfPCell(new Phrase(finance.getDueDate().format(formatter), cellFont)));

            String description = finance.getDescription() != null ? finance.getDescription() : "-";
            table.addCell(new PdfPCell(new Phrase(description, cellFont)));
            
            table.addCell(new PdfPCell(new Phrase(finance.getAmount().toString(), cellFont)));
            table.addCell(new PdfPCell(new Phrase(finance.getStatus().name(), cellFont)));
        }

        document.add(table);
    }
}