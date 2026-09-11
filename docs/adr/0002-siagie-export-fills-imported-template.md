---
status: accepted
---
# SIAGIE export fills the imported template instead of generating a file

SIAGIE only accepts the exact Excel file it generated for a section: the file
name and the hidden internal ids must match. We therefore keep the imported
template on the device and write levels, conclusions and attendance into it,
rather than building a spreadsheet from scratch.

## Considered Options

- Generate the workbook from the observed structure: rejected, the real
  template is only obtainable with a teacher login and any drift means SIAGIE
  rejects the upload.

## Consequences

- SIAGIE export is unavailable for a section that was created manually; those
  sections can only export PDF/CSV.
- Re-importing a template for the same section merges by Student Code and
  refreshes the stored file.
