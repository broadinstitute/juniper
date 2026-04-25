import React, { useState } from 'react'

import { paramsFromContext, StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import { Enrollee, ParticipantFile } from '@juniper/ui-core'
import { ParticipantDocumentListView } from '../survey/ParticipantDocumentListView'
import { useLoadingEffect } from 'api/api-utils'
import Api from 'api/api'
import LoadingSpinner from 'util/LoadingSpinner'

export default function EnrolleeDocuments({ enrollee, studyEnvContext }: {
    enrollee: Enrollee, studyEnvContext: StudyEnvContextT
}) {
  const [participantFiles, setParticipantFiles] = useState<ParticipantFile[]>([])

  const { isLoading } = useLoadingEffect(async () => {
    const response = await Api.listParticipantFiles({
      studyEnvParams: paramsFromContext(studyEnvContext),
      enrolleeShortcode: enrollee.shortcode
    })
    setParticipantFiles(response)
  }, [enrollee.shortcode])

  return <LoadingSpinner isLoading={isLoading}>
    <ParticipantDocumentListView
      studyEnvContext={studyEnvContext}
      showAssociatedTasks={true}
      enrollee={enrollee}
      documents={participantFiles}/>
  </LoadingSpinner>
}
