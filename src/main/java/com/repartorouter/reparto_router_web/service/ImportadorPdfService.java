package com.repartorouter.reparto_router_web.service;

import com.repartorouter.reparto_router_web.controller.dto.FilaImportadaDTO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImportadorPdfService {

    private static final int COL_ALIAS = 2;
    private static final int COL_DIRECCION = 3;
    private static final int COL_CP = 4;
    private static final int COL_POBLACION = 5;
    private static final int COL_HORA = 7;

    private static final Pattern PATRON_RANGO_HORARIO =
            Pattern.compile("(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})");

    public List<FilaImportadaDTO> extraerFilas(InputStream inputStream) throws IOException {
        List<FilaImportadaDTO> resultado = new ArrayList<>();

        try (PDDocument documento = PDDocument.load(inputStream)) {
            ObjectExtractor extractor = new ObjectExtractor(documento);
            SpreadsheetExtractionAlgorithm algoritmo = new SpreadsheetExtractionAlgorithm();

            for (int numPagina = 1; numPagina <= documento.getNumberOfPages(); numPagina++) {
                Page pagina = extractor.extract(numPagina);
                List<Table> tablas = algoritmo.extract(pagina);

                for (Table tabla : tablas) {
                    List<List<RectangularTextContainer>> filas = tabla.getRows();

                    for (List<RectangularTextContainer> celdas : filas) {

                        if (celdas.size() <= COL_HORA) continue;
                        if (esFilaDeCabecera(celdas)) continue;

                        String alias = limpiar(celdas.get(COL_ALIAS).getText());
                        String direccion = limpiar(celdas.get(COL_DIRECCION).getText());
                        String cp = limpiar(celdas.get(COL_CP).getText());
                        String poblacion = limpiar(celdas.get(COL_POBLACION).getText());
                        String horaTexto = limpiar(celdas.get(COL_HORA).getText());

                        if (alias.isEmpty() || direccion.isEmpty() || cp.isEmpty()) continue;

                        LocalTime[] horario = parsearHorario(horaTexto);
                        boolean detectado = horario != null;
                        LocalTime apertura = detectado ? horario[0] : LocalTime.of(9, 0);
                        LocalTime cierre = detectado ? horario[1] : LocalTime.of(20, 0);

                        resultado.add(new FilaImportadaDTO(alias, direccion, cp, poblacion,
                                apertura, cierre, detectado));
                    }
                }
            }
        }

        return resultado;
    }

    private boolean esFilaDeCabecera(List<RectangularTextContainer> celdas) {
        String alias = limpiar(celdas.get(COL_ALIAS).getText());
        return alias.equalsIgnoreCase("Alias");
    }

    private LocalTime[] parsearHorario(String texto) {
        Matcher m = PATRON_RANGO_HORARIO.matcher(texto);
        if (!m.find()) return null;
        try {
            LocalTime apertura = LocalTime.parse(normalizarHora(m.group(1)));
            LocalTime cierre = LocalTime.parse(normalizarHora(m.group(2)));
            if (cierre.isBefore(apertura) || cierre.equals(apertura)) return null;
            return new LocalTime[]{apertura, cierre};
        } catch (Exception ex) {
            return null;
        }
    }

    private String normalizarHora(String hora) {
        String[] partes = hora.split(":");
        return String.format("%02d:%s", Integer.parseInt(partes[0]), partes[1]);
    }

    private String limpiar(String texto) {
        if (texto == null) return "";
        return texto.replace("\n", " ").replaceAll("\\s+", " ").trim();
    }
}
