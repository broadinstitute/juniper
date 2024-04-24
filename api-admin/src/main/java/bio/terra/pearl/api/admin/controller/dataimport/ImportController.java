package bio.terra.pearl.api.admin.controller.dataimport;

import bio.terra.pearl.api.admin.api.DataImportApi;
import bio.terra.pearl.api.admin.service.AuthUtilService;
import bio.terra.pearl.api.admin.service.enrollee.EnrolleeImportExtService;
import bio.terra.pearl.core.model.admin.AdminUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Controller
public class ImportController implements DataImportApi {
    private AuthUtilService authUtilService;
    private HttpServletRequest request;
    private EnrolleeImportExtService enrolleeImportExtService;

    public ImportController(
            AuthUtilService authUtilService,
            HttpServletRequest request,
            EnrolleeImportExtService enrolleeImportExtService) {
        this.authUtilService = authUtilService;
        this.request = request;
        this.enrolleeImportExtService = enrolleeImportExtService;
    }

    @Override
    public ResponseEntity<Object> delete(
            String portalShortcode, String studyShortcode, String envName, Integer importId) {
        return DataImportApi.super.delete(portalShortcode, studyShortcode, envName, importId);
    }

    @Override
    public ResponseEntity<Object> get(String portalShortcode, String studyShortcode, String envName) {
        return DataImportApi.super.get(portalShortcode, studyShortcode, envName);
    }

    @Override
    public ResponseEntity<Object> callImport(
            String portalShortcode,
            String studyShortcode,
            String envName,
            String importFileName,
            Object body) {
        AdminUser operator = authUtilService.requireAdminUser(request);
        try {
            InputStream inputStream = new DataInputStream(new FileInputStream(importFileName));
            return ResponseEntity.ok(enrolleeImportExtService.importData(
                            portalShortcode, studyShortcode, envName, inputStream, operator));
        } catch (IOException e) {
            throw new IllegalArgumentException("could not read import data");
        }
    }
}
