# SMS Sheet Tracker (Android)

## Architecture
- **Ingestion layer:** `SmsReceiver` receives `SMS_RECEIVED` broadcast and keeps only sender IDs `25274`, `16216`, `62423`.
- **Parsing layer:** `SmsParser` extracts amount, TrxID, number, transaction type.
- **Persistence layer:** Room stores every accepted transaction with unique `trxId` index.
- **Sync layer:** WorkManager retries pending rows with network constraint and exponential backoff.
- **Transport layer:** Google Sheets API appends to the wallet-specific tab (`bKash`, `Rocket`, `Nagad`).
- **Runtime layer:** Foreground service + boot receiver keep system active after UI close/reboot.

## Workflow Diagram
1. SMS arrives -> `SmsReceiver`
2. Sender filtered -> `SmsParser`
3. Parsed row persisted in Room (`PENDING`)
4. WorkManager sync request enqueued
5. `SheetsSyncWorker` loads pending rows
6. `GoogleSheetsApiHelper` appends row
7. Success => `SYNCED`, Failure => keep `PENDING` + retry

## Google Sheet Setup
1. Create one Google Spreadsheet.
2. Create tabs: `bKash`, `Rocket`, `Nagad`.
3. In each tab row 1 set headers: `No | Date | Time | TrxID | Recv | Out | Amount | Number`.
4. Create a Google Cloud project and enable Google Sheets API.
5. Create Service Account and download JSON.
6. Share the Spreadsheet with service account email as Editor.
7. Import the JSON from app UI (`Import Service Account JSON`) and set spreadsheet id in `BuildConfigHolder`.

## Regex
- Amount: `(?:Tk\.?|BDT\s?)(\d{1,3}(?:,\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)`
- TrxID: `(?:TrxID|TxnID|Transaction\sID)\s*[:#-]?\s*([A-Z0-9]{8,20})`
- Number: `(?:from|to)\s+((?:\+?88)?01[3-9]\d{8})`

## Security
- Service account JSON is imported via SAF file picker.
- JSON is encrypted-at-rest with `EncryptedSharedPreferences` + `MasterKey`.
- No credential hardcoded in source.

## Testing Checklist
- Permission flow: SMS + Notification granted.
- Battery optimization exemption granted.
- Foreground notification remains visible.
- Reboot device and verify service restarts.
- Send sample SMS from allowed sender IDs and check insert.
- Send other sender IDs and verify ignore.
- Disable internet and verify rows stay pending.
- Restore internet and verify retry sync.
- Verify duplicate TrxID skipped.
