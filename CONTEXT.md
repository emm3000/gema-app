# Gema

Offline gradebook for Peruvian primary school teachers (EBR, grades 1-6). It
records attendance and competency achievement under the CNEB and fills the
SIAGIE Excel templates the Ministry requires.

## Language

### Time

**School Year**:
The root of all teacher data for one calendar year. Owns Periods and Sections.
_Avoid_: year, cycle

**Period**:
One evaluation window of a School Year, either a bimester or a trimester,
with a start and end date. Achievement Levels are reported per Period.
_Avoid_: bimester, trimester (use only when the configured kind matters), term, semester

### People and groups

**Teacher**:
The single user of the app. Owns one or more Sections. Not a login.
_Avoid_: user, account, tutor

**Section**:
A group of Students of one Grade within a School Year, taught by the Teacher.
A multigrade classroom is several Sections.
_Avoid_: class, classroom, group, course

**Grade**:
The primary level a Section belongs to, first through sixth.
_Avoid_: level, year, course

**Student**:
A child enrolled in a Section, identified by their SIAGIE student code.
_Avoid_: pupil, kid, learner

**Student Code**:
The 14-digit code SIAGIE assigns to a Student. Stable across years and schools.
_Avoid_: DNI, id, code

**SIAGIE Id**:
The internal numeric id SIAGIE uses for a Student inside a template file.
Only known after importing a template.
_Avoid_: id, student id

**Withdrawn**:
A Student who left the Section during the School Year. Keeps all recorded data,
no longer appears in attendance.
_Avoid_: deleted, removed, transferred, inactive

### Curriculum

**Area**:
A curricular area of primary EBR (Comunicación, Matemática, Personal Social,
Ciencia y Tecnología, Arte y Cultura, Educación Física, Educación Religiosa,
Inglés, Castellano como segunda lengua). A Section has all Areas active by
default; the Teacher may hide the ones they do not teach.
_Avoid_: subject, course, matter

**Competency**:
One of the CNEB competencies belonging to an Area, identified by its SIAGIE
number within that Area. Preloaded and versioned by the app, never typed by
the Teacher.
_Avoid_: skill, objective, standard

**Worked Competency**:
A Competency the Teacher marks as developed in a given Period for a Section.
Only Worked Competencies are exported.
_Avoid_: active competency, selected competency

### Evaluation

**Achievement Level**:
The literal scale of the CNEB: AD, A, B, C. Never averaged or computed.
_Avoid_: grade, score, mark, nota

**Activity**:
Something the Teacher did with the Section that produces evidence, linked to
one or more Competencies.
_Avoid_: evidence, task, assignment, evaluation, exam

**Evidence Level**:
The Achievement Level a Student showed on one Activity for one Competency.
Optional per Student.
_Avoid_: activity grade, partial grade

**Period Level**:
The Achievement Level the Teacher assigns to a Student for a Worked Competency
at the close of a Period. Decided by the Teacher, informed by Evidence Levels,
never derived automatically.
_Avoid_: final grade, average, computed level

**Descriptive Conclusion**:
Free text explaining a Period Level. Required by SIAGIE when the Period Level
is C, optional otherwise.
_Avoid_: comment, feedback, note

**Unworked Comment**:
The SIAGIE alternative to an Achievement Level for a competency that could not
be evaluated: no actions carried out, insufficient evidence, or other.
_Avoid_: comentario 1/2/3, N/A, empty grade

### Attendance

**Attendance**:
One Attendance Status per Student per school day for a Section.
_Avoid_: roll call, register

**Attendance Status**:
Present, late, absent, or justified absence.
_Avoid_: mark, flag

### SIAGIE exchange

**SIAGIE Template**:
The Excel file a Teacher downloads from SIAGIE for one Section: a grades
template (one sheet per Area) or a monthly attendance template.
_Avoid_: excel, sheet, spreadsheet, plantilla

**Import**:
Reading a SIAGIE Template to create or merge Students by Student Code.
_Avoid_: sync, load, upload

**Export**:
Filling an imported SIAGIE Template with Period Levels, Descriptive
Conclusions or Attendance, preserving its file name and structure.
_Avoid_: sync, generate, upload

**Backup**:
A single file containing all teacher data, shared to another device to restore.
_Avoid_: sync, cloud, export
