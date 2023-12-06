import React, { useState } from 'react'
import Modal from 'react-bootstrap/Modal'
import { Button } from 'components/forms/Button'
import { LabeledDateRangeMode, MetricDateRange } from './metricUtils'

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
export default function MetricDateRangeModal({
  setDateRange, dateRange,
  selectedDateRangeMode, setSelectedDateRangeMode, onDismiss
}: {
  selectedDateRangeMode: LabeledDateRangeMode, setDateRange: (dateRange: MetricDateRange) => void,
  dateRange: MetricDateRange,
  onDismiss: () => void, setSelectedDateRangeMode: (dateRangeMode: LabeledDateRangeMode) => void
}) {
  const [customDateRange, setCustomDateRange] = useState<MetricDateRange>(dateRange)

  return <Modal show={true} className="modal" onHide={onDismiss}>
    <Modal.Header closeButton>
      <Modal.Title>Select date range</Modal.Title>
    </Modal.Header>
    <Modal.Body>
      { dateRangeRadioPicker({
        selectedDateRangeMode,
        onDateSelect: setSelectedDateRangeMode
      }) }
      { selectedDateRangeMode.mode === 'CUSTOM' &&
        <div className="row">
          <div className="col">
            <label className="form-label mt-3" htmlFor="startDate">
              Start date
            </label>
            <input type="date" size={20} id="startDate" className="form-control"
              onChange={e =>
                setCustomDateRange({ ...customDateRange, startDate: new Date(e.target.value).getTime() })
              }/>
          </div>
          <div className="col">
            <label className="form-label mt-3" htmlFor="endDate">
              End date
            </label>
            <input type="date" size={20} id="endDate" className="form-control"
              onChange={e =>
                setCustomDateRange({ ...customDateRange, endDate: new Date(e.target.value).getTime() })
              }/>
          </div>
        </div>
      }
    </Modal.Body>
    <Modal.Footer>
      <Button variant="primary"
        onClick={() => {
          setSelectedDateRangeMode(selectedDateRangeMode)
          if (selectedDateRangeMode.mode === 'CUSTOM') {
            setDateRange(customDateRange)
          }
          onDismiss()
        }}>Done</Button>
    </Modal.Footer>
  </Modal>
}
