import React, {useEffect, useState} from "react";
import Api, {Portal, PortalEnvironment, Study, StudyEnvironment} from "api/api";
import {Survey as SurveyComponent} from "survey-react-ui";
import {Outlet, useNavigate, useOutletContext, useParams} from "react-router-dom";
import {useSurveyJSModel} from "util/surveyJsUtils";

const PREREG_ID_STORAGE_KEY = "preRegResponseId"

function RawPreRegistrationView({portalShortcode, studyShortcode, studyEnv}:
                                  {portalShortcode: string, studyShortcode: string, studyEnv: StudyEnvironment}) {

  const [preRegResponseId, setPreRegResponseId] = useState<string | null>(localStorage.getItem(PREREG_ID_STORAGE_KEY))
  const navigate = useNavigate()
  const survey = studyEnv.preRegSurvey
  // for now, we assume all pre-screeners are a single page
  const pager = { pageNumber: 0, updatePageNumber: () => 0}
  const { surveyModel, refreshSurvey } = useSurveyJSModel(survey, null, handleComplete, pager)

  function updatePreRegResponseId(preRegId: string | null) {
    if (!preRegId) {
      localStorage.removeItem(PREREG_ID_STORAGE_KEY)
    } else {
      localStorage.setItem(PREREG_ID_STORAGE_KEY, preRegId)
    }
    setPreRegResponseId(preRegId)
  }

  function handleComplete() {
    if (!surveyModel) {
      return
    }
    Api.completePreReg({
      portalShortcode,
      envName: studyEnv.environmentName,
      studyShortcode,
      surveyStableId: survey.stableId,
      surveyVersion: survey.version,
      fullData: surveyModel.getPlainData()
    }).then(result => {
      navigate('register')
      updatePreRegResponseId(result.id)
    }).catch(e => {
      alert("an error occurred, please retry")
      updatePreRegResponseId(null)
      // SurveyJS doesn't support "uncompleting" surveys, so we have to reinitialize it
      // (for now we assume prereg is only a single page)
      refreshSurvey(surveyModel.data, 1)
    })
  }

  useEffect(() => {
    if (preRegResponseId) {
      Api.confirmPreReg(portalShortcode, studyEnv.environmentName, preRegResponseId).then(() => {
        //this is a valid pre-reg, redirect to the registration page
        navigate('register')
      }).catch(() => {
        updatePreRegResponseId(null)
        navigate('')
      })
    } else {
      navigate('')
    }

  }, [])

  return <div>
    {(!preRegResponseId && surveyModel) && <SurveyComponent model={surveyModel}/>}
    { preRegResponseId && <Outlet context={{preRegResponseId}}/> }
  </div>
}

type StudyEnvironmentParams = {
  studyShortcode: string,
}

export default function PreregistrationView({portal}: {portal: Portal}) {
  const params = useParams<StudyEnvironmentParams>()
  let studyShortcode: string | undefined = params.studyShortcode
  if (!studyShortcode) {
     return <div>Must specify a study shortcode</div>
  }

  const study = portal.portalStudies.find(pStudy => pStudy.study.shortcode === studyShortcode)?.study
  if (!study) {
    return <div>No matching study for {studyShortcode}</div>
  }
  const studyEnv = study.studyEnvironments[0]
  if (!studyEnv) {
    return <div>No matching environment for {studyShortcode}</div>
  }
  studyEnv.studyShortcode = studyShortcode
  return <RawPreRegistrationView portalShortcode={portal.shortcode}
          studyShortcode={studyShortcode} studyEnv={studyEnv}/>
}

export type RegistrationContext = {
  preRegResponseId: string
}

export function useRegistrationContext(): RegistrationContext {
  return useOutletContext<RegistrationContext>()
}
