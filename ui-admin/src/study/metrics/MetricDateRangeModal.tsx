import React from 'react'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { LabeledDateRangeMode } from './metricUtils'

const dateRangeRadioPicker = ({ selectedDateRangeMode, onDateSelect } : {
  selectedDateRangeMode: LabeledDateRangeMode
  onDateSelect: (dateRangeMode: LabeledDateRangeMode) => void
}) => {
  const dateRangeOptions: LabeledDateRangeMode[] = [
    { label: 'All Time', mode: 'ALL_TIME' },
    { label: 'Last Month', mode: 'LAST_MONTH' },
    { label: 'Last Week', mode: 'LAST_WEEK' },
    { label: 'Last 24 Hours', mode: 'LAST_24_HOURS' },
    { label: 'Custom', mode: 'CUSTOM' }
  ]

  return dateRangeOptions.map((dateRangeOption, index) => {
    return <div className="form-check" key={index}>
      <input className="form-check-input" type="radio" name="plotTimeRange" id={`dateRange-${index}`}
        checked={selectedDateRangeMode.mode === dateRangeOption.mode}
        onChange={() => {
          onDateSelect(dateRangeOption)
        }}/>
      <label className="form-check-label" htmlFor={`dateRange-${index}`}>
        {dateRangeOption.label}
      </label>
    </div>
  })
}

/**
 * Modal for selecting a date range for a MetricView
 */
export default function MetricDateRangeModal({ selectedDateRangeMode, setSelectedDateRangeMode, onDismiss }: {
  selectedDateRangeMode: LabeledDateRangeMode,
  onDismiss: () => void, setSelectedDateRangeMode: (dateRangeMode: LabeledDateRangeMode) => void
}) {
  return <Modal show={true} className="modal" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Select date range</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      { dateRangeRadioPicker({
        selectedDateRangeMode,
        onDateSelect: setSelectedDateRangeMode
      }) }
    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        onClick={() => {
          setSelectedDateRangeMode(selectedDateRangeMode)
          onDismiss()
        }}>Done</Button>
    </Modal.Footer>
  </Modal>
}
