---
status: accepted
---
# Student name is stored as one SIAGIE-format string, never split

`Student` carries one uppercase name string exactly as it comes from the
SIAGIE Excel cell — surnames and given names together, in whatever order
SIAGIE printed them — with no first-name/last-name split. `GetStudentsUseCase`
orders students by this string as a plain lexicographic sort, not by a parsed
surname.

## Considered Options

- **Split into surname and given-name fields.** Rejected: SIAGIE's own export
  does not guarantee a parseable split (compound surnames, single names,
  inconsistent comma placement), and reconstructing the original cell for
  re-export risks producing a string SIAGIE did not write, breaking the
  lossless import/export round trip.

## Consequences

- Sorting is a plain string sort, not a "sort by surname" sort; two students
  whose SIAGIE strings start with a shared prefix sort exactly as that prefix
  compares, with no special-casing.
- Export never has to reassemble a name from parts, since the field it writes
  back is the same string it read.
- Any future UI that wants to show "given name only" (for example a casual
  greeting) has no data to do so without a separate, explicit decision — see
  `CONTEXT.md`'s `Student` entry, which already avoids "pupil" / "kid" /
  "learner" but does not currently note the single-string shape.
