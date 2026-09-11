---
status: accepted
---
# Setup prefills the school year and computes its periods

The first thing a teacher saw on a cold start was an empty form with ten date
fields: the school year start and end, plus a start and an end for each of the
four bimesters. Every one of them was mandatory, none of them was answerable
without a calendar, and getting one wrong blocked the only button on the screen.

The dates are not free information. A Peruvian primary school year runs from
early March to late December, and its periods are that range cut into equal
parts — which is exactly what `PeriodKind.divide` already computed once the
range was known. The form asked the teacher to type an answer the app could
derive.

Setup now opens already filled in. The label is the year of the device date, the
range is 1 March to 20 December of that year, and the periods are the list
`PeriodKind.divide` returns for it. The eight raw period date fields are gone:
the periods render as one row each, and tapping a row opens a dialog that edits
that period alone.

## Consequences

- A teacher whose school follows the ordinary calendar finishes step 1 by
  pressing *Continuar*. Everything else is a correction, not an entry.
- The prefill is a guess with no authority behind it. 1 March and 20 December
  are a common shape, not a Minedu rule, and a school that starts in April must
  change two dates. The reassurance copy at the top says the dates can be
  corrected, and `Periodos` in settings keeps editing them after setup.
- Editing one period can still leave it overlapping its neighbour or outside the
  year. The validation from the original form is unchanged: the offending row
  carries the message and *Continuar* stays disabled.
- Changing the school year range or the period kind recomputes every period and
  discards manual edits. Adjusting one period after setting the range is the
  supported order, and the opposite order silently loses work.
- The device date is injected as `java.time.Clock`, so the prefill is a value the
  ViewModel tests set rather than a wall clock they race.
