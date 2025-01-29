import React, { useState } from 'react'

import { paramsFromContext, StudyEnvContextT } from '../../StudyEnvironmentRouter'
import { Enrollee, ParticipantFile } from '@juniper/ui-core'
import { ParticipantFileSurveyResponseView } from '../survey/ParticipantFileSurveyResponseView'
import { useLoadingEffect } from '../../../api/api-utils'
import Api from '../../../api/api'

export default function EnrolleeDocuments({ enrollee, studyEnvContext }: {
    enrollee: Enrollee, studyEnvContext: StudyEnvContextT
}) {
  const [participantFiles, setParticipantFiles] = useState<ParticipantFile[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    await loadDocuments()
  }, [enrollee])

  const loadDocuments = async () => {
    if (!enrollee) { return }
    const response = await Api.listParticipantFiles({
      studyEnvParams: paramsFromContext(studyEnvContext),
      enrolleeShortcode: enrollee.shortcode
    })
    setParticipantFiles(response)
  }

  return <ParticipantFileSurveyResponseView
    studyEnvContext={studyEnvContext}
    enrollee={enrollee}
    documents={participantFiles}/>
}
