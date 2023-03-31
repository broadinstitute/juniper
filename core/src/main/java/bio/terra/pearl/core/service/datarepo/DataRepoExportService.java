package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import org.springframework.stereotype.Service;

@Service
public class DataRepoExportService {

    DataRepoClient dataRepoClient;

    public DataRepoExportService(DataRepoClient dataRepoClient) {
        this.dataRepoClient = dataRepoClient;
    }

    public boolean getServiceStatus() {
        try {
            return dataRepoClient.getServiceStatus().isOk();
        } catch (ApiException e) {
            return false;
        }
    }

    public String makeDatasetName(String deploymentZone, String studyName, String environmentName) {
        if(deploymentZone.equalsIgnoreCase("prod"))
            return String.format("d2p_%s_%s", studyName, environmentName);
        else
            return String.format("d2p_%s_%s_%s", deploymentZone, studyName, environmentName);
    }

}
