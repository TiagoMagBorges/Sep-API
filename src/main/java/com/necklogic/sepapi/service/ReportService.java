package com.necklogic.sepapi.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.necklogic.sepapi.model.Aluno;
import com.necklogic.sepapi.model.Aula;
import com.necklogic.sepapi.model.ClassGroup;
import com.necklogic.sepapi.repository.AlunoRepository;
import com.necklogic.sepapi.repository.AulaRepository;
import com.necklogic.sepapi.repository.ClassGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font SECTION_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

    private final ClassGroupRepository classGroupRepository;
    private final AlunoRepository alunoRepository;
    private final AulaRepository aulaRepository;

    public byte[] generateClassGroupReport(UUID classGroupId, UUID professorId, LocalDateTime start, LocalDateTime end) {
        validatePeriod(start, end);

        ClassGroup classGroup = classGroupRepository.findByIdAndProfessorId(classGroupId, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Turma não encontrada."));

        List<Aluno> students = alunoRepository
                .findAllByClassGroupIdAndProfessorIdAndArquivadoEmIsNullOrderByNomeAsc(classGroupId, professorId);
        List<Aula> lessons = aulaRepository
                .findAllByClassGroupIdAndProfessorIdAndDataHoraBetweenOrderByDataHoraAsc(classGroupId, professorId, start, end);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, outputStream);

            document.open();
            addHeader(document, classGroup, start, end);
            addStudents(document, students);
            addLessons(document, lessons);
            document.close();

            return outputStream.toByteArray();
        } catch (DocumentException exception) {
            throw new IllegalStateException("Não foi possível gerar o relatório da turma.", exception);
        }
    }

    private void validatePeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe as datas de início e fim.");
        }

        if (start.isAfter(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data inicial deve ser anterior à data final.");
        }
    }

    private void addHeader(Document document, ClassGroup classGroup, LocalDateTime start, LocalDateTime end) throws DocumentException {
        Paragraph title = new Paragraph("Relatório da Turma: " + classGroup.getNome(), TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        Paragraph period = new Paragraph(
                "Período: " + formatDate(start) + " até " + formatDate(end),
                BODY_FONT
        );
        period.setAlignment(Element.ALIGN_CENTER);
        period.setSpacingAfter(20);
        document.add(period);
    }

    private void addStudents(Document document, List<Aluno> students) throws DocumentException {
        Paragraph section = new Paragraph("Alunos vinculados", SECTION_FONT);
        section.setSpacingAfter(8);
        document.add(section);

        if (students.isEmpty()) {
            Paragraph empty = new Paragraph("Nenhum aluno vinculado a esta turma.", BODY_FONT);
            empty.setSpacingAfter(16);
            document.add(empty);
            return;
        }

        for (Aluno student : students) {
            document.add(new Paragraph("- " + student.getNome(), BODY_FONT));
        }

        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(10);
        document.add(spacer);
    }

    private void addLessons(Document document, List<Aula> lessons) throws DocumentException {
        Paragraph section = new Paragraph("Histórico de aulas no período", SECTION_FONT);
        section.setSpacingAfter(8);
        document.add(section);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2F, 1.4F, 4F});
        addHeaderCell(table, "Data/Hora");
        addHeaderCell(table, "Status da Aula");
        addHeaderCell(table, "Log Público de atividades");

        if (lessons.isEmpty()) {
            PdfPCell emptyCell = new PdfPCell(new Phrase("Nenhuma aula encontrada para o período informado.", BODY_FONT));
            emptyCell.setColspan(3);
            emptyCell.setPadding(8);
            table.addCell(emptyCell);
        } else {
            for (Aula lesson : lessons) {
                addBodyCell(table, formatDate(lesson.getDataHora()));
                addBodyCell(table, lesson.getStatus().name());
                addBodyCell(table, resolvePublicLog(lesson));
            }
        }

        document.add(table);
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY_FONT));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String resolvePublicLog(Aula lesson) {
        String log = lesson.getLogPublicoAtividades();
        return log == null || log.isBlank() ? "Sem log público registrado." : log;
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
}
