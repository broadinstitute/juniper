package bio.terra.pearl.core.service.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import bio.terra.pearl.core.service.export.formatters.item.ItemFormatter;
import bio.terra.pearl.core.service.export.formatters.module.ModuleFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;


/** generates an excel file with a single sheet containing the participant data */
public class ExcelExporter extends BaseExporter {
    private final static int ROW_ACCESS_WINDOW_SIZE = 200;
    protected final SXSSFWorkbook workbook;

    protected final SXSSFSheet sheet;
    private static final String SHEET_NAME = "Participants";

    public ExcelExporter(List<ModuleFormatter> moduleFormatters, List<Map<String, String>> enrolleeMaps) {
        super(moduleFormatters, enrolleeMaps);
        workbook = new SXSSFWorkbook(ROW_ACCESS_WINDOW_SIZE);
        sheet = workbook.createSheet(getSheetName());
        sheet.trackAllColumnsForAutoSizing();
    }

    public void export(OutputStream os) {
        List<String> columnKeys = getColumnKeys();
        List<String> headerRowValues = getHeaderRow();
        List<String> subHeaderRowValues = getSubHeaderRow();

        writeRowToSheet(headerRowValues, 0);
        writeRowToSheet(subHeaderRowValues, 1);

        IntStream.range(0, enrolleeMaps.size()).forEach(i -> {
            Map<String, String> valueMap = enrolleeMaps.get(i);
            List<String> rowValues = getRowValues(valueMap, columnKeys);
            writeRowToSheet(rowValues, i + 2);
        });
        try {
            writeAndCloseSheet(os);
        } catch (IOException e) {
            throw new RuntimeException("Error writing excel file", e);
        }
    }

    protected void writeAndCloseSheet(OutputStream os) throws IOException {
        try (workbook) {
            workbook.write(os);
            workbook.dispose();
        }
    }

    protected void writeRowToSheet(List<String> rowValues, int rowNum) {
        Row headerRow = sheet.createRow(rowNum);
        IntStream.range(0, rowValues.size()).forEach(i -> {
            headerRow.createCell(i).setCellValue(rowValues.get(i));
        });
    }

    protected String getSheetName() {
        return SHEET_NAME;
    }
}
