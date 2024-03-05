import { StudyEnvContextT } from '../StudyEnvironmentRouter'
import { MetricChartType } from './StudyEnvMetricsView'
import React, { useEffect, useState } from 'react'
import Api, { BasicMetricDatum, SurveyAnswerDatum, StudyEnvironmentSurvey, VersionedForm } from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import LoadingSpinner from 'util/LoadingSpinner'
import Select from 'react-select'
import useReactSingleSelect from 'util/react-select-utils'
import LineChart from './charts/LineChart'
import PieChart from './charts/PieChart'
import BarChart from './charts/BarChart'
import Histogram from './charts/Histogram'
import { surveyJSModelFromForm } from '@juniper/ui-core'
import { getQuestionsWithComputedValues } from '../participants/survey/SurveyFullDataView'
import { Question } from 'survey-core'

/**
 * Shows a graph and summary for a metric.
 */
export default function SurveyInsightsView({ studyEnvContext }: {
    studyEnvContext: StudyEnvContextT
}) {
  const [metricData, setMetricData] = useState<SurveyAnswerDatum[]>([])
  const [selectedChartType, setSelectedChartType] = useState<MetricChartType>()
  const [selectedSurvey, setSelectedSurvey] = useState<StudyEnvironmentSurvey>()
  const [selectedQuestion, setSelectedQuestion] = useState<string>()
  const [fieldOptions, setFieldOptions] = useState<string[]>()
  const surveyJsModel = selectedSurvey ? surveyJSModelFromForm(selectedSurvey?.survey as VersionedForm) : undefined
  const questions = surveyJsModel ? getQuestionsWithComputedValues(surveyJsModel) : []
  const surveyJsQuestion = questions.find(question => question.name == selectedQuestion) as Question
  const questionText = surveyJsQuestion?.title

  const { isLoading } = useLoadingEffect(async () => {
    if (selectedSurvey && selectedQuestion) {
      const result = await Api.fetchFieldMetric(studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName, selectedSurvey.survey.stableId, selectedQuestion)
      setMetricData(result)
    }
  }, [selectedChartType, selectedSurvey, selectedQuestion])

  //TODO Harmonize this
  const fieldMetricsToBasicMetricDatum = (fieldMetrics: SurveyAnswerDatum[]): BasicMetricDatum[] => {
    if (Object.hasOwn(fieldMetrics[0], 'objectValue')) {
      return parseObjectValues(surveyJsQuestion, fieldMetrics)
    }
    return fieldMetrics.map(fieldMetric => ({
      name: fieldMetric.name,
      subcategory: fieldMetric.stringValue ??
          fieldMetric.numberValue as string ??
          fieldMetric.booleanValue as unknown as string,
      time: fieldMetric.time
    }))
  }

  const parseObjectValues = (question: Question, metricData: SurveyAnswerDatum[]): BasicMetricDatum[] => {
    if (question && question.getType() == 'checkbox') {
      return metricData.flatMap(fieldMetric => {
        const stringValues = JSON.parse(fieldMetric.objectValue || '[]') as string[]
        return stringValues.map(value => ({
          name: fieldMetric.name,
          subcategory: value,
          time: fieldMetric.time
        }))
      })
    } else {
      return []
    }
  }

  useEffect(() => {
    setSelectedQuestion(undefined)
  }, [selectedSurvey])

  useEffect(() => {
    setSelectedChartType(undefined)
  }, [selectedQuestion])

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
          ['bar', 'pie', 'histogram'],
          (chartType: MetricChartType) => ({ label: chartType, value: chartType }),
          setSelectedChartType,
          selectedChartType)

  const {
    onChange: surveyOnChange, options: surveyOptions,
    selectedOption: selectedSurveyOption, selectInputId: selectSurveyInputId
  } =
        useReactSingleSelect(
          studyEnvContext.currentEnv.configuredSurveys,
          (s: StudyEnvironmentSurvey) => ({ label: s.survey.name, value: s }),
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
            {questionText && <h3 className="h5 text-center pt-4">{questionText} (responses={metricData.length})</h3> }
            {selectedChartType === 'line' &&
              <LineChart metricData={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {selectedChartType === 'pie' &&
              <PieChart data={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {selectedChartType === 'bar' &&
              <BarChart data={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {selectedChartType === 'histogram' &&
              <Histogram data={fieldMetricsToBasicMetricDatum(metricData)}/>
            }
            {!selectedChartType && <div className="d-flex justify-content-center align-items-center h-100">
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
                  isDisabled={!selectedSurvey}
                  inputId={selectQuestionInputId}
                  value={selectedQuestionOption}
                  onChange={questionOnChange}
                />
                <label htmlFor={selectChartTypeInputId} className='mt-3'>Chart Type</label>
                <Select
                  className={'mb-5'}
                  isDisabled={!selectedQuestion || !selectedSurvey}
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
