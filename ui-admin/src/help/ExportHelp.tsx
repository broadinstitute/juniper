import React from 'react'

/** guide to using the participant export/download function */
export default function ExportHelp() {
  return <div>
    <h3>Participant List Export Info</h3>
    <p>Participant export enables download of tabular files (.tsv or .xlsx) containing the data currently visible in
        the Participant List.
            One row will be generated per participant.</p>

    <h4>File format</h4>
    <div className="panel">
      <ul>
        <li>
          <b>.xlsx</b> will create an Excel spreadsheet of the participant data. Empty cells will represent
                    null values.
        </li>
        <li>
          <b>.tsv</b> (tab-delimited values). Will export a tab-delimited file. This may be useful in
                    environments where Excel is unavailable, or if the
                    number of columns to be exported exceeds 16K. In order to have data be compliant, double-quotes will
                    be replaced by single quotes, and any values including
                    tabs or line breaks will be surrounded in double-quotes.
        </li>
      </ul>


    </div>

    <h4>Human readable / Analysis friendly</h4>
    <ul>
      <li>
        <b>Analysis friendly</b> Each picklist answers will be displayed as a stable id, rather than the
                displayed text. For multiselects, each answer option will appear in a separate column. For example, the
                question &quot;Which symptoms have you had?&quot;
                with options &quot;fever&quot;, &quot;nausea&quot;, and &quot;persisent cough&quot;,
                will be exported into 3 columns.
        <table className="table table-striped">
          <thead>
            <tr>
              <td>MEDICAL_HISTORY.SYMTPOMS.FEVER</td>
              <td>MEDICAL_HISTORY.SYMPTOMS.NAUSEA</td>
              <td>MEDICAL_HISTORY.SYMPTOMS.COUGH</td>
            </tr>
            <tr>
              <td>fever</td>
              <td>nausea</td>
              <td>persistent cough</td>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>0</td>
              <td>1</td>
              <td>0</td>
            </tr>
            <tr>
              <td>1</td>
              <td>1</td>
              <td>0</td>
            </tr>
            <tr>
              <td>0</td>
              <td>0</td>
              <td>0</td>
            </tr>
          </tbody>
        </table>

      </li>
      <li>
        <b>Human readable</b> will use display text where possible, and will show multi-select questions as a
                single column, with a comma-delimited string of the answers given. For example, the question &quot;Which
                symptoms have you had?&quot;
                with options &quot;fever&quot;, &quot;nausea&quot;, and &quot;persisent cough&quot;,
                will be exported into 1 column.
        <table className="table table-striped">
          <thead>
            <tr>
              <td>MEDICAL_HISTORY.SYMTPOMS</td>
            </tr>
            <tr>
              <td>symptoms</td>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>nausea</td>
            </tr>
            <tr>
              <td>fever, nausea</td>
            </tr>
            <tr>
              <td></td>
            </tr>
          </tbody>
        </table>
      </li>
    </ul>
    <h4>Include all completions of an activity</h4>
        This option controls how the export will behave if a participant has completed an activity multiple times.
    <ul>
      <li>
        <b>Yes</b> A new set of columns will be added to the export for each time the activity was completed.
                These will be
                denoted by _2, _3, etc... Columns will appear in order of *recency*. So e.g. MEDICAL_HISTORY.SYMPTOMS
                represents the
                most recent completion, while MEDICAL_HISTORY_2.SYMPTOMS represents the next-most recent, and so on.
      </li>
      <li>
        <b>No</b> Only the most recent completion for each activity will be included in the export.
      </li>
    </ul>
  </div>
}
