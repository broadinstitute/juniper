import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { MetricChartType } from './StudyEnvMetricsView'
import React, { useState } from 'react'
import Api, { BasicMetricDatum, FieldMetricDatum, StudyEnvironmentSurvey } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import PieChart from './charts/PieChart'
import Select from 'react-select'
import useReactSingleSelect from '../../util/react-select-utils'
import LineChart from './charts/LineChart'
import BarChart from './charts/BarChart'

/**
 * Shows a graph and summary for a metric.
 */
export default function SurveyInsightsView({ studyEnvContext }: {
  studyEnvContext: StudyEnvContextT
}) {
  const [metricData, setMetricData] = useState<FieldMetricDatum[]>([])
  const [chartType, setChartType] = useState<MetricChartType>()
  const [selectedSurvey, setSelectedSurvey] = useState<StudyEnvironmentSurvey>()
  const [selectedQuestion, setSelectedQuestion] = useState<string>()
  const [fieldOptions, setFieldOptions] = useState<string[]>()

  const { isLoading } = useLoadingEffect(async () => {
    if (selectedSurvey && selectedQuestion) {
      const result = await Api.fetchFieldMetric(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, selectedSurvey.survey.stableId, selectedQuestion)
      setMetricData(result)
    }
  }, [chartType, selectedSurvey, selectedQuestion])

  const fieldMetricsToBasicMetricDatum = (fieldMetrics: FieldMetricDatum[]): BasicMetricDatum[] => {
    return fieldMetrics.map(fieldMetric => {
      return {
        name: 'foo',
        subcategory: fieldMetric.stringValue,
        time: fieldMetric.time
      }
    })
  }

  useLoadingEffect(async () => {
    if (selectedSurvey) {
      const result = await Api.listMetricFields(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, selectedSurvey.survey.stableId)
      setFieldOptions(result)
    } else {
      setSelectedQuestion(undefined)
    }
  }, [selectedSurvey])

  const {
    onChange: chartTypeOnChange, options: chartTypeOptions,
    selectedOption: selectedChartTypeOption, selectInputId: selectChartTypeInputId
  } =
    useReactSingleSelect(
      ['pie', 'bar'],
      (chartType: MetricChartType) => ({ label: chartType, value: chartType }),
      setChartType,
      chartType)

  const {
    onChange: surveyOnChange, options: surveyOptions,
    selectedOption: selectedSurveyOption, selectInputId: selectSurveyInputId
  } =
    useReactSingleSelect(
      studyEnvContext.currentEnv.configuredSurveys,
      (s: StudyEnvironmentSurvey) => ({ label: s.survey.stableId, value: s }),
      setSelectedSurvey,
      selectedSurvey)

  const {
    onChange: questionOnChange, options: questionOptions,
    selectedOption: selectedQuestionOption, selectInputId: selectQuestionInputId
  } =
    useReactSingleSelect(
      fieldOptions || [],
      (s: string) => ({ label: s, value: s }),
      setSelectedQuestion,
      selectedQuestion)

  return <div className="container mb-4">
    <LoadingSpinner isLoading={isLoading}>
      <div className="d-flex align-items-baseline">
        <h2 className="h5">Survey Insights</h2>
      </div>
      <div className="container-fluid border">
        <div className="row">
          <div className="col border">
            {chartType === 'line' &&
              <LineChart metricData={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {chartType === 'pie' &&
              <PieChart metricData={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {chartType === 'bar' &&
                <BarChart metricData={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {!chartType && <div className="d-flex justify-content-center align-items-center h-100">
              <span className="text-muted fst-italic">No chart configured</span>
            </div>}
          </div>
          <div className="col-3 border">
            <div className="container-fluid">
              <h4 className="my-3 align-center">Configure Insight</h4>
              <div className="row my-3">
                <label htmlFor={selectSurveyInputId}>Survey</label>
                <Select
                  options={surveyOptions}
                  inputId={selectSurveyInputId}
                  value={selectedSurveyOption}
                  onChange={surveyOnChange}
                />
                <label htmlFor={selectQuestionInputId} className='mt-3'>Survey Field</label>
                <Select
                  options={questionOptions}
                  inputId={selectQuestionInputId}
                  value={selectedQuestionOption}
                  onChange={questionOnChange}
                />
                <label htmlFor={selectChartTypeInputId} className='mt-3'>Chart Type</label>
                <Select
                  className={'mb-5'}
                  inputId={selectChartTypeInputId}
                  value={selectedChartTypeOption}
                  options={chartTypeOptions}
                  onChange={chartTypeOnChange}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </LoadingSpinner>
  </div>
}
