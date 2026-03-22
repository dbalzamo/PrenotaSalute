package prenotazione.medica.impegnativa.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import prenotazione.medica.impegnativa.entity.Impegnativa;
import prenotazione.medica.prestazioneSanitaria.entity.PrestazioneSanitaria;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Genera un PDF minimale dell'impegnativa (testo strutturato, ad uso clinico-amministrativo).
 */
@Service
public class ImpegnativaPdfService {

    private static final DateTimeFormatter DATA_IT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());

    public byte[] generaPdf(Impegnativa imp) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Impegnativa elettronica", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Codice NRE: " + nullSafe(imp.getCodiceNRE())));
            document.add(new Paragraph("Data emissione: " + DATA_IT.format(Instant.now())));
            document.add(new Paragraph(" "));

            if (imp.getPaziente() != null) {
                document.add(new Paragraph("Paziente", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(
                        nullSafe(imp.getPaziente().getNome()) + " " + nullSafe(imp.getPaziente().getCognome())));
                document.add(new Paragraph("Codice fiscale: " + nullSafe(imp.getPaziente().getCodiceFiscale())));
                document.add(new Paragraph(" "));
            }

            if (imp.getMedicoCurante() != null) {
                document.add(new Paragraph("Medico curante", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(
                        "Dr./Dr.ssa " + nullSafe(imp.getMedicoCurante().getNome()) + " "
                                + nullSafe(imp.getMedicoCurante().getCognome())));
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Tipo ricetta: " + (imp.getTipoRicetta() != null ? imp.getTipoRicetta().name() : "—")));
            document.add(new Paragraph("Priorità: " + (imp.getPriorità() != null ? imp.getPriorità().name() : "—")));
            document.add(new Paragraph("Regione / riferimento: " + nullSafe(imp.getRegione())));
            document.add(new Paragraph(" "));

            PrestazioneSanitaria ps = imp.getPrestazioneSanitaria();
            if (ps != null) {
                document.add(new Paragraph("Prestazione", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph("Codice prestazione: " + ps.getCodicePrestazione()));
                document.add(new Paragraph("Descrizione: " + nullSafe(ps.getDescrizione())));
                document.add(new Paragraph("Quantità: " + ps.getQuantita()));
                document.add(new Paragraph("Note: " + nullSafe(ps.getNote())));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Errore generazione PDF impegnativa", e);
        }
    }

    private static String nullSafe(String s) {
        return s != null && !s.isBlank() ? s : "—";
    }
}
