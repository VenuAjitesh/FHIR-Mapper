# ABDM FHIR Mapper - FHIR (HL7® FHIR® Standard) R4
- The purpose of this implementation guide is to provide essential and minimum health record artefacts that can be captured and created as per ABDM Health Data Interchange Specifications 1.0.
- This module is built using **hapi fhir library** for generating the fhir resources. 
- For better understanding of FHIR, check [here](https://nrces.in/ndhm/fhir/r4/index.html)

### 1. System Requirements and Installations:
There are two ways to get fhir-mapper application running on your system:
#### 1. Using docker (Preferred): This is an easy way to get wrapper up and running.
Install docker and docker-compose: You can install docker desktop from [here](https://www.docker.com/products/docker-desktop/) to get both.

System Requirements:
- For Mac, check [here](https://docs.docker.com/desktop/install/mac-install/)
- For Windows, check [here](https://docs.docker.com/desktop/install/windows-install/)
- For Linux, check [here](https://docs.docker.com/desktop/install/linux-install/)

Using docker-compose-fhir.yaml to bring up the server.

#### 2. If you are facing issues with installing or running docker, then you can install individual components:
- Install jdk 17. Instructions can be found [here](https://www3.cs.stonybrook.edu/~amione/CSE114_Course/materials/resources/InstallingJava17.pdf)
- Install gradle from [here](https://gradle.org/install/)

System Requirements:
- For Java17, you can check [here](https://www.oracle.com/java/technologies/javase/products-doc-jdk17certconfig.html) for compatible system configurations.
- Gradle version >= 8.5 should be fine.

Recommended RAM: Systems with more than 8 GB RAM
### Bringing up the application
- If you have installed docker and docker compose then you can bring the application using: `docker compose -f docker-compose-fhir.yaml up --build`
* If you have chosen to install Java and gradle components, then here is how you can bring the service up:
  - Go to root of this repository and start fhir-mapper by running `gradle bootrun`
- Using any of the above approaches the server will be running on port `8085`

### ABDM PROFILES (HI-Types)
- The HI type is primarily defined based on the data being collected. However the type of interaction should also be considered.
- If the data which is collected is not defined in the structured field for any HI-type you can create a pdf and attach to the particular bundle in the documents.

| Name                   | Definition                                                                                                                                                |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| DiagnosticReportRecord | The Clinical Artifact represents diagnostic reports including Radiology and Laboratory reports that can be shared across the health ecosystem.              |
| DischargeSummaryRecord | Clinical document used to represent the discharge summary record for ABDM HDE data set.                                                                     |
| HealthDocumentRecord   | The Clinical Artifact represents the unstructured historical health records as a single or multiple Health Record Documents generally uploaded by the patients through the Health Locker and can be shared across the health ecosystem.  |
| ImmunizationRecord     | The Clinical Artifact represents the Immunization records with any additional documents such as vaccine certificate, the next immunization recommendations, etc. This can be further shared across the health ecosystem.               |
| OPConsultRecord        | The Clinical Artifact represents the outpatient visit consultation note which may include clinical information on any OP examinations, procedures along with medication administered, and advice that can be shared across the health ecosystem. |
| PrescriptionRecord     | The Clinical Artifact represents the medication advice to the patient in compliance with the Pharmacy Council of India (PCI) guidelines, which can be shared across the health ecosystem.                                                |
| WellnessRecord         | The Clinical Artifact represents regular wellness information of patients typically through the Patient Health Record (PHR) application covering clinical information such as vitals, physical examination, general wellness, women wellness, etc., that can be shared across the health ecosystem.                            |
| InvoiceRecord          | The billing artifact represents the invoice details such as pharmacy invoice, consultation invoice etc. along with the support for scanned documents attached for the patient which can be shared across the health ecosystem.                                                                                                                                                                                                                                                                                                             |

---
### APIs for generating the FHIR bundle
- For Swagger-ui, check [here](https://venuajitesh.github.io/FHIR-Mapper/)

<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 📄 DiagnosticReportRecord </span></summary>

- for the DiagnosticReportRecord bundle you need to 
   * `POST` Request `/v1/bundle/diagnostic-report`

    ``` 
    {
      "bundleType": "DiagnosticReportRecord", 
      "careContextReference": "visist 21-03-2024", 
      "authoredOn": "2006-04-22", 
      "patient": { 
        "name": "Venu Ajitesh", 
        "patientReference": "ajitesh6x", 
        "gender": "male",
        "birthDate": "2001-04-27"
      },
      "practitioners": [{  
        "name": "Dr.Venu Ajitesh", 
        "practitionerId": "Predator"
      }],
      "organisation": {
        "facilityName": "Predator_HIP", 
        "facilityId": "Predator_HIP"
      },
      "encounter": "Ambula",
      "diagnostics": [{
        "serviceName": "BloodTest", 
        "serviceCategory": "Hematography", 
        "result": [{ 
          "observation": "Height", 
          "result": "Normal",
          "valueQuantity": {
            "unit": "CM",
            "value": 170
          }
        }],
        "conclusion": "Normal", 
        "presentedForm": {
          "contentType": "application/pdf",
          "data": "Base64 data of the pdf"
        }
      }],
      "documents": [{
        "type": "diagnosticReport",  
        "contentType": "application/pdf",  
        "data": "Base64 data of the pdf"  
      }]
    } ```
</details> 

<details>
  <summary> <span style="font-weight:bold; font-size:16px;"> 🏥 DischargeSummaryRecord </span></summary>

- For the DischargeSummaryRecord bundle you need to
  * `POST` Request `/v1/bundle/discharge-summary`

  ```
  {
    "bundleType":"DischargeSummaryRecord", //mandatory
    "careContextReference":"visist 21-03-2024", //mandatory
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{ //mandatory
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "chiefComplaints":[
        {
            "complaint":"Sugar", //mandatory
            "recordedDate":"2024-05-20", //mandatory
            "dateRange":{
                "from":"2018-04-27",
                "to":"2018-05-26"
            }
        }
    ],
    "physicalExaminations":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
                    "unit": "CM",
                    "value": 170
            }
        }
    ],
    "allergies":[
        "Walnuts"
    ],
    "medicalHistories":[
        {
            "complaint":"Sugar", //mandatory
            "recordedDate":"2024-05-20", //mandatory
            "dateRange":{
                "from":"2018-04-27",
                "to":"2018-05-26"
            }
        }
    ],
    "familyHistories":[
        {
            "relationship":"Friend", //mandatory
            "observation":"Toxic" //mandatory
        }
    ],
    "authoredOn":"2024-02-03", //mandatory
    "medications":[
        {
          "medicine":"Aspirin 75 mg oral tablet", //mandatory
          "dosage":"1-0-1", //mandatory
          "timing":"2-5-d",
          "route":"Oral",
          "method":"swallow",
          "additionalInstructions":"Take them after food",
          "reason": "fever"
      }
      ,{
          "medicine":"Disprin",  //mandatory
          "dosage":"0-0-1", //mandatory
          "timing":"1-2-d",
          "route":"Syrup",
          "method":"drink",
          "additionalInstructions":"Take them before food",
          "reason": "Cough"
      }
    ],
    "diagnostics":[{
        "serviceName":"BloodTest", //mandatory
        "serviceCategory":"Hematography", //mandatory
        "result":[{ 
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
                    "unit": "CM",
                    "value": 170
            }
        }],
        "conclusion":"Normal", //mandatory
        "presentedForm":{
            "contentType":"application/pdf",
            "data":"Base64 data of the pdf"
          }
    }],
    
    "procedures":[
        {
            "date":"2001-04-20", //mandatory
            "status":"INPROGRESS", //mandatory
            "procedureReason":"Severe", //mandatory
            "outcome":"Healthy",
            "procedureName":"Operation" //mandatory
        }
    ],

    "documents":[{
        "type":"Discharge record", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
    }]
  }
  ```
  </details>

<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 📑 HealthDocumentRecord </span></summary>

- for the HealthDocumentRecord bundle you need to
  * `POST` Request `/v1/bundle/health-document`
  ```
  {
    "bundleType":"HealthDocumentRecord", //mandatory
    "careContextReference":"visist 21-03-2024", //mandatory
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "authoredOn":"2001-04-27", //mandatory
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "encounter":"",
    "documents":[{
        "type":"health-document", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
    }]
  }
  ```
  </details>
<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 💉 ImmunizationRecord </span></summary>

- for the ImmunizationRecord bundle you need to
  * `POST` Request `/v1/bundle/immunization`
  ```
  {
    "bundleType":"ImmunizationRecord", //mandatory
    "careContextReference":"visit-{{$isoTimestamp}}", //mandatory
    "authoredOn":"2022-02-14", //mandatory
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "immunizations":[{ //mandatory
        "date":"2024-06-15", //mandatory
        "vaccineName":"Covaxin", //mandatory
        "lotNumber":"IN00004",
        "manufacturer":"NHA",
        "doseNumber":"3"
    }],
    "documents":[{
        "type":"immunization", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
      }]
  }
  ```
  </details>
<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 👨‍⚕️ OPConsultRecord </span></summary>

- for the OPConsultRecord bundle you need to
  * `POST` Request `/v1/bundle/op-consultation`
  ```
  {
    "bundleType":"OPConsultRecord", //mandatory
    "careContextReference":"visist 21-03-2025", //mandatory
    "visitDate":"2000-06-23", //mandatory
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "chiefComplaints":[
        {
            "complaint":"Sugar", //mandatory
            "recordedDate":"2024-05-20", //mandatory
            "dateRange":{
                "from":"2018-04-27",
                "to":"2018-05-26"
            }
        }
    ],
    "physicalExaminations":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "allergies":[
        "Walnuts"
    ],
    "medicalHistories":[
        {
            "complaint":"Sugar", //mandatory
            "recordedDate":"2024-05-20", //mandatory
            "dateRange":{
                "from":"2018-04-27",
                "to":"2018-05-26"
            }
        }
    ],
    "familyHistories":[
        {
            "relationship":"Friend", //mandatory
            "observation":"Toxic" //mandatory
        }
    ],
    "serviceRequests":[
        {
            "status":"ACTIVE", //mandatory
            "details":"X-RAY", //mandatory
            "specimen":"Jhonsons"
        }
    ],
    "medications":[
        {
            "medicine":"Aspirin 75 mg oral tablet", //mandatory
            "dosage":"1-0-1", //mandatory
            "timing":"2-5-d",
            "route":"Oral",
            "method":"swallow",
            "additionalInstructions":"Take them after food",
            "reason": "fever"
        }
        ,{
            "medicine":"Disprin", //mandatory
            "dosage":"0-0-1", //mandatory
            "timing":"1-2-d",
            "route":"Syrup",
            "method":"drink",
            "additionalInstructions":"Take them before food",
            "reason": "Cough"
        }
    ],
    "followups":[
        {
            "serviceType":"OPConsultation", //mandatory
            "appointmentTime":"2024-05-20", //mandatory
            "reason":"General" //mandatory
        }
    ],
    "procedures":[
          {
              "date":"2001-04-20", //mandatory
              "status":"INPROGRESS", //mandatory
              "procedureReason":"Severe", //mandatory
              "outcome":"Healthy",
              "procedureName":"Operation" //mandatory
          }
      ],
    "referrals":[
        {
            "status":"ACTIVE", //mandatory
            "details":"Y-RAY", //mandatory
            "specimen":"Rock" //mandatory
        }
    ],
    "otherObservations":[
        {
            "observation":"weight", //mandatory
            "result":"Over weight", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "KG",
					"value": 90
				}
        }
    ],
    "documents":[{
        "type":"OP record", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
      }]
  }
  ```
    </details>
<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 💊 PrescriptionRecord </span></summary>

- for the PrescriptionRecord bundle you need to
  * `POST` Request `/v1/bundle/prescription`
  ```
  {
    "bundleType":"PrescriptionRecord", //mandatory
    "careContextReference":"visit-{{$isoTimestamp}}", //mandatory
    "authoredOn":"2001-05-22", //mandatory
    "encounter":"",
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "prescriptions":[
        {
            "medicine":"Aspirin 75 mg oral tablet", //mandatory
            "dosage":"1-0-1", //mandatory
            "timing":"2-5-d",
            "route":"Oral",
            "method":"swallow",
            "additionalInstructions":"Take them after food",
            "reason": "fever"
        }
        ,{
            "medicine":"Disprin", //mandatory
            "dosage":"0-0-1", //mandatory
            "timing":"1-2-d",
            "route":"Syrup",
            "method":"drink",
            "additionalInstructions":"Take them before food",
            "reason": "Cough"
        }
    ],
    "documents":[{
        "type":"Prescription", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
      }]
  }
  ```
  </details>
<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 🌿 WellnessRecord </span></summary>

- for the WellnessRecord bundle you need to
  * `POST` Request `/v1/bundle/wellness-record`
  ```
  {
    "bundleType":"WellnessRecord", //mandatory
    "careContextReference":"visist 21-03-2025", //mandatory
    "authoredOn":"2024-02-11", //mandatory
    "patient":{ //mandatory
        "name":"Venu Ajitesh", //mandatory
        "patientReference":"ajitesh6x", //mandatory
        "gender":"male",
        "birthDate":"2001-04-27"
    },
    "practitioners":[{ //mandatory
        "name":"Dr.Venu Ajitesh", //mandatory
        "practitionerId":"Predator"
    }],
    "organisation":{
        "facilityName":"Predator_HIP", //mandatory
        "facilityId":"Predator_HIP"
    },
    "vitalSigns":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "bodyMeasurements":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "physicalActivities":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "generalAssessments":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],"womanHealths":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "lifeStyles":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "otherObservations":[
        {
            "observation":"Height", //mandatory
            "result":"Normal", //you can pass either result or valueQuantity not both
            "valueQuantity": {
					"unit": "CM",
					"value": 170
			}
        }
    ],
    "documents":[{
        "type":"Wellness record", //mandatory
        "contentType":"application/pdf", //mandatory
        "data":"Base64 data of the pdf" //mandatory
      }]
  }
  ```
  </details>
<details>
  <summary><span style="font-weight:bold; font-size:16px;"> 🧾 InvoiceRecord </span></summary>

  - for the InvoiceRecord bundle you need to
      * `POST` Request `/v1/bundle/invoice`
    ```
    {
      "bundleType": "Invoice",
      "careContextReference": "visit-{{$isoTimestamp}}",
      "invoiceDate": "2024-05-01T06:33:37.361Z",
      "status": "issued",
      "encounter": "",
      "patient": {
          "name": "Venu Ajitesh",
          "patientReference": "ajitesh6x",
          "gender": "male",
          "birthDate": "1940-04-27"
      },
      "practitioners": [
          {
              "name": "Dr.Venu Ajitesh",
              "practitionerId": "Predator"
          }
      ],
      "organisation": {
          "facilityName": "Predator_HIP",
          "facilityId": "Predator_HIP"
      },
      "invoice": {
          "id": "INV-12345", // Required
          "status": "issued", // Required - issued | balanced | cancelled | draft | entered-in-error
          "type": "Pharmacy", // Required - Consultation | Pharmacy | IPD | OPD | Others
          "date": "2025-08-12T10:30:00+05:30", // Required
          "totalNet": 2000.00, // Required
          "totalGross": 2200.00, // Required
          "currency": "INR", // Required
          "paymentTerms": "Due in 15 days", // Optional
          "note": "Surgery + post-op medicines" // Optional
      },
      "chargeItems": [ // Required - at least one
          {
              "id": "CHG-002",
              "productType": "medication", // medication | device | substance
              "chargeType": "Pharmacy",
              "status": "billed", // planned | billable | not-billable | aborted | billed | entered-in-error | unknown
              "description": "Amoxicillin 500mg",
              "quantity": 14,
              "price": [
                  {
                      "priceType": "base", // base | surcharge | deduction | discount | SGST | CGST | informational
                      "amount": "100"
                  }
              ],
              "medication": { // Only for type=medication
                  "medicineName": "AMOX500",
                  "manufacturer": "ABC Pharma",
                  "medicationForm": "tablet",
                  "lotNumber": "ABC123",
                  "expiryDate": "{{$isoTimestamp}}"
              }
          },
          {
              "id": "CHG-003",
              "productType": "device", // medication | device | substance
              "chargeType": "OPD",
              "status": "billed",
              "description": "Surgical Gloves",
              "quantity": 5,
              "price": [
                  {
                      "priceType": "base", // base | surcharge | deduction | discount | SGST | CGST | informational
                      "amount": "200"
                  }
              ],
              "device": { // Only for type=device
                  "udiCarrier": "UDI-GLV-001",
                  "manufacturer": "MedGlove Inc.",
                  "modelNumber": "MG-GLV-SRG",
                  "lotNumber": "194847-0",
                  "serialNumber": "SN-98347",
                  "manufactureDate": "2025-01-10",
                  "expirationDate": "2028-01-10",
                  "status": "active",
                  "safety": [
                      "Latex Free",
                      "Sterile"
                  ],
                  "deviceName": "Surgical Gloves",
                  "note": "Used for major surgery"
              }
          },
          {
              "id": "CHG-004",
              "productType": "substance", // medication | device | substance
              "chargeType": "IPD",
              "status": "billed",
              "description": "Lab Reagent Kit",
              "quantity": 1,
              "price": [
                  {
                      "priceType": "base", // base | surcharge | deduction | discount | SGST | CGST | informational
                      "amount": "300"
                  }
              ],
              "substance": { // Only for type=substance
                  "id": "",
                  "code": "LAB-REAGENT-001",
                  "category": "chemical",
                  "description": "Used to treat infections",
                  "expiry": "{{$isoTimestamp}}",
                  "quantity": "2.0"
              }
          }
      ],
      "payment": {
          "method": "upi", // Optional
          "status":"active", // Optional
          "paymentDate":"", /// Optional
          "paidAmount": 2200.00, // Optional
          "transactionId": "TXN-98765" // Optional
      },
    }
      ```
    </details>

---
### Error response in 400 BadRequest
- There are two kinds of error
  * Validation errors : The errors are thrown when mandatory fields are missing
    ```
    {
      "validationErrors": {
          "code": 1000,
          "error": [
              {
                  "field": "bundleType",
                  "message": "must match \"WellnessRecord\""
              }
          ]
      }
    }
    ```
  * Errors while creating the FHIR bundle
    ```
    {
       "error":{
       "code":1000,
       "message":"Unable to parse date"
       }
    }
    ```
---
### Things To Consider
- If the FHIR bundle is generated the HttpStatus will be `201 created`
- The authoredOn will accept date in the format of string in format : `yyyy-MM-dd` or `yyyy-MM-dd'T'HH:mm:ss.SSSX` - UTC iso time format