import React from 'react'
import { Enrollee, Survey, SurveyResponse } from 'api/api'
import DocumentTitle from '../../../util/DocumentTitle'
import { instantToDefaultString } from '@juniper/ui-core'
import SurveyFullDataView from './SurveyFullDataView'
import SurveyView from '../../surveys/SurveyView'

/** allows editing of a survey response */
export default function SurveyEditView({ response, survey, enrollee }:
{response?: SurveyResponse, survey: Survey, enrollee: Enrollee}) {
  return <div>
    <DocumentTitle title={`${enrollee.shortcode} - ${survey.name}`}/>
  <h6>{survey.name}</h6>
  <div>
      <SurveyView studyEnvContext={}
  </div>
</div>
}


const makeEmptyResponse = (enrollee: Enrollee, survey: Survey, adminUserId: string): SurveyResponse => {
  return {
    enrolleeId: enrollee.id,
    creatingAdminUserId: adminUserId,
    surveyId: survey.id,
    resumeData: '{}',
    answers: [],
    complete: false
  }
}
