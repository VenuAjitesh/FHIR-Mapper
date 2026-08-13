/* (C) 2026 */
package in.nha.abdm.fhir.mapper.rest.dto.resources;

import in.nha.abdm.fhir.mapper.Utils;
import in.nha.abdm.fhir.mapper.rest.common.constants.ResourceProfileIdentifier;
import in.nha.abdm.fhir.mapper.rest.requests.helpers.MedicalDeviceResource;
import java.text.ParseException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.Device;
import org.hl7.fhir.r4.model.DeviceUseStatement;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.stereotype.Component;

@Component
public class MakeDeviceUseStatementResource {

  public DeviceUseStatement getDeviceUseStatement(
      Patient patient, Device device, MedicalDeviceResource resource) throws ParseException {
    DeviceUseStatement deviceUseStatement = new DeviceUseStatement();
    deviceUseStatement.setId(UUID.randomUUID().toString());
    deviceUseStatement.setMeta(createMeta());
    deviceUseStatement.setSubject(Utils.buildReference(patient.getId()));
    deviceUseStatement.setDevice(Utils.buildReference(device.getId()));
    setStatus(deviceUseStatement, resource.getStatus());
    setRecordedOn(deviceUseStatement, resource.getRecordedDate());
    setTiming(deviceUseStatement, resource.getRecordedDate());

    if (StringUtils.isNotBlank(resource.getNote())) {
      deviceUseStatement.addNote(new Annotation().setText(resource.getNote()));
    }

    Utils.setNarrative(deviceUseStatement, "Device in use: " + resource.getDeviceName());
    return deviceUseStatement;
  }

  private Meta createMeta() throws ParseException {
    return new Meta()
        .setLastUpdatedElement(Utils.getCurrentTimeStamp())
        .addProfile(ResourceProfileIdentifier.PROFILE_IN_PS_DEVICE_USE_STATEMENT);
  }

  private void setStatus(DeviceUseStatement deviceUseStatement, String status) {
    if (StringUtils.isNotBlank(status)) {
      deviceUseStatement.setStatus(DeviceUseStatement.DeviceUseStatementStatus.fromCode(status));
    }
  }

  private void setRecordedOn(DeviceUseStatement deviceUseStatement, String recordedDate)
      throws ParseException {
    if (StringUtils.isNotBlank(recordedDate)) {
      deviceUseStatement.setRecordedOnElement(Utils.getFormattedDateTime(recordedDate));
    }
  }

  private void setTiming(DeviceUseStatement deviceUseStatement, String recordedDate)
      throws ParseException {
    if (StringUtils.isNotBlank(recordedDate)) {
      deviceUseStatement.setTiming(Utils.getFormattedDateTime(recordedDate));
    }
  }
}
