import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { MetricInfo } from './StudyEnvMetricsView'
import MetricGraph from './MetricGraph'
import MetricSummary from './MetricSummary'
import React, { useState } from 'react'
import Api, { BasicMetricDatum } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import { Button } from 'components/forms/Button'
import { faClipboard } from '@fortawesome/free-solid-svg-icons'
import InfoPopup from 'components/forms/InfoPopup'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { instantToDefaultString } from 'util/timeUtils'
import { LabeledDateRangeMode, METRIC_EXPORT_DELIMITER, MetricDateRange } from './metricUtils'

/**
 * Shows a graph and summary for a metric.
 */
export default function MetricView({ studyEnvContext, metricInfo, dateRange, dateRangeMode }: {
  studyEnvContext: StudyEnvContextT, metricInfo: MetricInfo,
  dateRange?: MetricDateRange, dateRangeMode: LabeledDateRangeMode
}) {
  const [metricData, setMetricData] = useState<BasicMetricDatum[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const result = await Api.fetchMetric(studyEnvContext.portal.shortcode, studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName, metricInfo.name)
    setMetricData(result)
  }, [metricInfo.name, studyEnvContext.study.shortcode, studyEnvContext.currentEnv.environmentName])

  const copyRawData = () => {
    if (!metricData) {
      return
    }
    let dataString = `${['name', 'subcategory', 'time'].join(METRIC_EXPORT_DELIMITER)  }\n`
    dataString += metricData.map(metricDatum =>
      [metricInfo.name, metricDatum.subcategory, instantToDefaultString(metricDatum.time)].join(METRIC_EXPORT_DELIMITER)
    ).join('\n')
    navigator.clipboard.writeText(dataString)
  }

  return <div className="container mb-4">
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-baseline">
        <h2 className="h5">{metricInfo.title}</h2>
        { metricInfo.tooltip && <InfoPopup content={metricInfo.tooltip} /> }
        <Button
          variant="secondary"
          tooltip={'Copy raw data to clipboard'}
          onClick={copyRawData}
        >
          <FontAwesomeIcon icon={faClipboard} className={'fa-regular'} />
        </Button>
      </div>
      <div className="container-fluid border">
        <div className="row">
          <div className="col border">
            <MetricGraph metricData={metricData} dateRange={dateRange}/>
          </div>
          <div className="col-3 border">
            <MetricSummary metricData={metricData ?? []} dateRange={dateRange} dateRangeMode={dateRangeMode}/>
          </div>
        </div>
      </div>
    </LoadingSpinner>
  </div>
}
