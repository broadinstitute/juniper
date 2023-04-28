package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.QuestionChoice;
import bio.terra.pearl.core.service.export.formatters.ExportFormatUtils;
import bio.terra.pearl.core.service.export.formatters.SurveyFormatter;
import bio.terra.pearl.core.service.export.instance.ItemExportInfo;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;

/**
 * Writes a data dictionary file based on the given configs.
 * columns include variable name, type, description, and options.
 * Built in excel for allowing more precise formatting control -- tsv/markdown/rtf caused study staff too many problems
 * with cell formats
 */
public class DataDictionaryExcelExporter extends ExcelExporter {
    private static final int ROW_ACCESS_WINDOW_SIZE = 200;
    private int currentRowNum = -1;
    private static final String SHEET_NAME = "Data dictionary";
    private static final String FILE_NAME = "DataDictionary.xlsx";
    private static final String SPLIT_OPTIONS_OPT_TEXT = "0 - not selected; 1 - selected";

    private final CellStyle wrapStyle;
    private final CellStyle boldStyle;
    private final CellStyle boldUnderlineStyle;

    private static final int VARIABLE_NAME_COL_NUMBER = 0;
    private static final int DATATYPE_COL_NUMBER = 1;
    private static final int QUESTION_TYPE_COL_NUMBER = 2;
    private static final int DESCRIPTION_COL_NUMBER = 3;
    private static final int OPTIONS_COL_NUMBER = 4;

    private SurveyFormatter surveyFormatter;


    /**
     * initializes the dictionary and internal spreadsheet
     */
    public DataDictionaryExcelExporter(List<ModuleExportInfo> moduleInfos, ObjectMapper objectMapper) {
        super(moduleInfos, null);
        wrapStyle = workbook.createCellStyle();
        wrapStyle.setWrapText(true);
        boldStyle = workbook.createCellStyle();
        Font boldFont = workbook.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);

        boldUnderlineStyle = workbook.createCellStyle();
        Font boldUnderlineFont = workbook.createFont();
        boldUnderlineFont.setBold(true);
        boldUnderlineFont.setUnderline(Font.U_SINGLE);
        boldUnderlineStyle.setFont(boldUnderlineFont);

        surveyFormatter = new SurveyFormatter(objectMapper);
    }

    /** writes the dictionary */
    @Override
    public void export(OutputStream os) throws IOException {
        sheet.setColumnWidth(VARIABLE_NAME_COL_NUMBER, 40 * 256);
        sheet.setColumnWidth(DATATYPE_COL_NUMBER, 10 * 256);
        sheet.setColumnWidth(QUESTION_TYPE_COL_NUMBER, 12 * 256);
        sheet.setColumnWidth(DESCRIPTION_COL_NUMBER, 60 * 256);
        sheet.setColumnWidth(OPTIONS_COL_NUMBER, 40 * 256);

        for (ModuleExportInfo moduleInfo : moduleExportInfos) {
            addModuleHeaderRows(moduleInfo);
            for (ItemExportInfo itemInfo : moduleInfo.getItems()) {
                addItemRows(moduleInfo, itemInfo);
            }
        }
        writeAndCloseSheet(os);
    }


    protected void addModuleHeaderRows(ModuleExportInfo moduleInfo) {
        // two blank rows
        addRowToSheet();
        addRowToSheet();

        SXSSFRow moduleNameRow = addRowToSheet();
        moduleNameRow.setRowStyle(boldStyle);
        moduleNameRow.createCell(VARIABLE_NAME_COL_NUMBER).setCellValue(moduleInfo.getDisplayName().toUpperCase());

        if (moduleInfo.getMaxNumRepeats() > 1) {
            addModuleRepeatDescription(moduleInfo.getModuleName(), moduleNameRow, moduleInfo);
        }
        sheet.addMergedRegion(new CellRangeAddress(currentRowNum, currentRowNum, DATATYPE_COL_NUMBER, OPTIONS_COL_NUMBER));

        SXSSFRow columnHeaders = addRowToSheet("Variable Name", "Data type",
                "Question type", "Description", "Options");
        columnHeaders.setRowStyle(boldUnderlineStyle);
    }

    private void addModuleRepeatDescription(String moduleName, SXSSFRow moduleNameRow, ModuleExportInfo moduleInfo) {
        String repeatString = "Up to " + moduleInfo.getMaxNumRepeats() +
                " entries for this module exist for each participant.\n";
        repeatString += "Entries are indicated in reverse chronological order.\n";
        repeatString += "The most recent entry has no suffix, the next-most-recent is suffixed with _2," +
                " the next-most-recent is suffixed with _3, etc...\n ";
        repeatString += "e.g. " + moduleName + ".[QUESTION] is the most recent completion, and " + moduleName +
                "_2.QUESTION is the next-most recent completion.";
        moduleNameRow.createCell(DATATYPE_COL_NUMBER).setCellValue(repeatString);
    }

    protected void addItemRows(ModuleExportInfo moduleInfo, ItemExportInfo itemInfo) {
        if (itemInfo.getPropertyAccessor() != null) {
            addBeanPropertyRow(moduleInfo, itemInfo);
        } else {
            addSurveyQuestionRows(moduleInfo, itemInfo);
        }
    }

    protected void addBeanPropertyRow(ModuleExportInfo moduleInfo, ItemExportInfo itemInfo) {
        String questionType = itemInfo.getQuestionType();
        if (questionType == null) {
            questionType = "";
        }
        String dataType = itemInfo.getDataType().toString().toLowerCase();

        String descriptionText = ExportFormatUtils.camelToWordCase(itemInfo.getBaseColumnKey());

        if (itemInfo.getMaxNumRepeats() > 1) {
            descriptionText += "\n May have up to " + itemInfo.getMaxNumRepeats() +
                    " response variables, denoted with _2, _3, etc. for each response after the first.";
        }
        addRowToSheet(itemInfo.getBaseColumnKey(), dataType, questionType, descriptionText, "");
    }

    protected void addSurveyQuestionRows(ModuleExportInfo moduleInfo, ItemExportInfo itemInfo) {
        String questionType = itemInfo.getQuestionType();
        if (questionType == null) {
            questionType = "";
        }
        if (questionType.equals("html")) {
            // for now, exclude descriptions from the export
            return;
        }
        String dataType = itemInfo.getDataType().toString().toLowerCase();

        String descriptionText = itemInfo.getQuestionText();

        if (itemInfo.getMaxNumRepeats() > 1) {
            descriptionText += "\n May have up to " + itemInfo.getMaxNumRepeats() +
                    " response variables, denoted with _2, _3, etc. for each response after the first.";
        }
        String optionText = "";
        if (!itemInfo.getChoices().isEmpty() && !itemInfo.isSplitOptionsIntoColumns()) {
            if (isLargeNumericDropdown(itemInfo.getChoices())) {
                optionText = renderNumericDropdownText(itemInfo.getChoices());
            } else {
                optionText = renderChoicesText(itemInfo.getChoices());
            }
        }
        String header = surveyFormatter.getColumnHeader(moduleInfo, itemInfo, false, null);
        addRowToSheet(header, dataType, questionType, descriptionText, optionText);

        if (!itemInfo.getChoices().isEmpty() && itemInfo.isSplitOptionsIntoColumns()) {
            for (QuestionChoice choice : itemInfo.getChoices()) {
                String choiceHeader = surveyFormatter.getColumnHeader(moduleInfo, itemInfo, false, choice);
                addRowToSheet(choiceHeader, null, null, choice.text(), SPLIT_OPTIONS_OPT_TEXT);
            }
        }
        if (itemInfo.isHasOtherDescription()) {
            String otherHeader = surveyFormatter.getColumnHeader(moduleInfo, itemInfo, true, null);
            addRowToSheet(otherHeader, "text", "TEXT", "additional detail", null);
        }
    }

    /** fast and hacky way to avoid listing 100 numeric options in numeric dropdowns.
     * eventually, we'll want this to be part of the question definition
     * */
    public boolean isLargeNumericDropdown(List<QuestionChoice> choices) {
        if (choices.size() < 20) {
            return false;
        }
        // only check the first 10 (skipping the first),
        // it still counts as a number dropdown if it has an N/A or preferNotToAnswer at the end or beginning
        for (QuestionChoice choice : choices.subList(1,10)) {
            if (!NumberUtils.isParsable(choice.stableId())) {
                return false;
            }
        }
        return true;
    }

    public String renderChoicesText(List<QuestionChoice> choices) {
        return choices.stream().map(opt ->
                opt.stableId() + " - " + opt.text()
        ).collect(Collectors.joining("\n"));
    }

    public String renderNumericDropdownText(List<QuestionChoice> choices) {
        // only show the first three and last three
        List<String> choiceStrings = choices.subList(0, 3).stream().map(choice -> renderSingleChoice(choice))
                .collect(Collectors.toList());
        choiceStrings.add(" ... ");
        choiceStrings.addAll(
                choices.subList(choices.size() - 3, choices.size()).stream().map(choice -> renderSingleChoice(choice)).toList()
        );
        return choiceStrings.stream().collect(Collectors.joining("\n"));
    }

    protected String renderSingleChoice(QuestionChoice choice) {
        return choice.stableId() + " - " + choice.text();
    }

    protected SXSSFRow addRowToSheet(String variableName, String dataType, String questionType, String description, String options) {
        SXSSFRow newRow = addRowToSheet();
        addCellToRow(newRow, VARIABLE_NAME_COL_NUMBER, variableName != null ? variableName : StringUtils.EMPTY);
        addCellToRow(newRow, DATATYPE_COL_NUMBER, dataType != null ? dataType : StringUtils.EMPTY);
        addCellToRow(newRow, QUESTION_TYPE_COL_NUMBER, questionType != null ? questionType : StringUtils.EMPTY);
        addCellToRow(newRow, DESCRIPTION_COL_NUMBER, description != null ? description : StringUtils.EMPTY);
        addCellToRow(newRow, OPTIONS_COL_NUMBER, options != null ? options : StringUtils.EMPTY);
        return newRow;
    }

    protected SXSSFRow addRowToSheet() {
        currentRowNum++;
        return sheet.createRow(currentRowNum);
    }

    protected SXSSFCell addCellToRow(SXSSFRow row, int colNum, String value) {
        SXSSFCell cell = row.createCell(colNum);
        cell.setCellValue(value);
        cell.setCellStyle(wrapStyle);
        return cell;
    }

    protected String getSheetName() {
        return SHEET_NAME;
    }
}
