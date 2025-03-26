# Smarter Reconciliation & Anomaly Detection System


## 🚀 Features
- CSV-based transaction reconciliation
- AI-powered anomaly detection (OpenNLP)
- Configurable matching rules
- REST API for integration
- Multi-format date support supporting different country

## ⚙️ Tech Stack
| Component       | Technology               |
|-----------------|--------------------------|
| Backend         | Spring Boot 3.2 (Java 17)|
| NLP Engine      | Apache OpenNLP 2.3       |
| CSV Processing  | OpenCSV 5.7              |
| Build Tool      | Gradle 8.4               |

## 📦 Setup
1. **Clone & Build**
   ```bash
   git clone https://github.com/ewfx/sradg-risk-scanners.git
   cd smarterReconcilation
2. Add NLP Model
      Download en-ner-amount.bin to src/main/resources
3. Edit application.properties:
      server.port=8080
      spring.servlet.multipart.max-file-size=10MB
4. Troubleshooting
      Error	                        Solution
      CSV parsing failed:	        Verify header columns match
      Model loading error:	        Check en-ner-amount.bin
      Large file processing issues:	Increase JVM heap size


![API Flow](https://i.imgur.com/API_FLOW.png)

## 📮 API Request (Postman Setup)

### 1. Request Configuration
| Setting          | Value                          |
|------------------|--------------------------------|
| Method           | `POST`                         |
| URL              | `http://localhost:8080/financial-reconciliation/api/reconcile'|
| Headers          | `Content-Type: multipart/form-data` |


### 2. Body Parameters (form-data)
| Key              | Type       | Value/Description           | Required |
|------------------|------------|-----------------------------|----------|
| `sourceFile`     | File       | Upload source CSV           | ✅ Yes    |
| `targetFile`     | File       | Upload target CSV           | ✅ Yes    |
| `referenceFile`  | File       | Optional reference CSV      | ❌ No     |
| `amountTolerance`| Text       | e.g., "0.1" (10% tolerance) | ❌ No     |
| `detectAnomalies`| Text       | "true" or "false"           | ❌ No     |

### 3. Postman Collection Snippet
```json
{
  "info": {
    "name": "Reconciliation API"
  },
  "item": [
    {
      "name": "Reconcile Transactions",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "multipart/form-data"
          }
        ],
        "body": {
          "mode": "form-data",
          "formdata": [
            {
              "key": "sourceFile",
              "type": "file",
              "src": "/path/to/source.csv"
            },
            {
              "key": "targetFile",
              "type": "file",
              "src": "/path/to/target.csv"
            },
            {
              "key": "amountTolerance",
              "value": "0.1",
              "type": "text"
            }
          ]
        },
        "url": {
          "raw": "{{base_url}}/api/reconcile",
          "host": ["{{base_url}}"],
          "path": ["api","reconcile"]
        }
      }
    }
  ],
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    }
  ]
}
5. Sample Response
   {
   "matchedRecords": 42,
   "discrepancies": [
   {
   "transactionId": "TXN114",
   "issue": "AMOUNT_MISMATCH",
   "sourceAmount": 1000.50,
   "targetAmount": 1100.00
   }
   ],
   "anomalies": [
   {
   "transactionId": "TXN202",
   "type": "SUSPICIOUS_DESCRIPTION",
   "severity": "HIGH"
   }
   ]
   }