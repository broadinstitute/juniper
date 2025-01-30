import {
  Enrollee,
  EnvironmentName,
  I18nOptions,
  instantToDateString,
  ParticipantFile,
  saveBlobAsDownload,
  StudyEnvParams,
  useI18n
} from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faDownload, faFile, faFileImage, faFileLines, faFilePdf } from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from 'providers/PortalProvider'
import { Link } from 'react-router-dom'
import { getTaskPath } from '../task/taskUtils'

export default function DocumentLibrary() {
  const { i18n } = useI18n()
  const { portal, portalEnv } = usePortalEnv()
  const { enrollees, ppUser } = useActiveUser()

  const studiesJoined = portal.portalStudies.filter(pStudy =>
    enrollees.some(enrollee =>
      enrollee.profileId === ppUser?.profileId && enrollee.studyEnvironmentId === pStudy.study.studyEnvironments[0].id)
  )

  return <div
    className="hub-dashboard-background flex-grow-1 pb-2"
    style={{ background: 'var(--dashboard-background-color)' }}>
    <div className="row mx-0 justify-content-center">
      <div className="my-md-4 mx-auto px-0" style={{ maxWidth: 768 }}>
        <div className="card-body">
          <div className="align-items-center">
            <div className="mb-3 rounded round-3 py-4 bg-white px-md-5 shadow-sm px-2">
              <h1 className="pb-3">
                {i18n('documentsPageTitle')}
              </h1>
              <div className="pb-4">
                {i18n('documentsPageMessage')}
              </div>
              <h3>{i18n('documentsPageUploadedDocumentsTitle')}</h3>
              {studiesJoined.map(pStudy =>
                <DocumentsList
                  key={pStudy.study.shortcode}
                  studyName={pStudy.study.name}
                  studyEnvParams={{
                    portalShortcode: portal.shortcode,
                    studyShortcode: pStudy.study.shortcode,
                    envName: portalEnv.environmentName as EnvironmentName
                  }}
                  enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId)!}
                />
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
}

const DocumentsList = ({ studyName, studyEnvParams, enrollee }: {
  studyName: string, studyEnvParams: StudyEnvParams, enrollee: Enrollee
}) => {
  const { i18n } = useI18n()
  const [participantFiles, setParticipantFiles] = useState<ParticipantFile[]>([])

  const loadDocuments = async () => {
    const enrolleeShortcode = enrollee.shortcode
    const documents = await Api.listParticipantFiles({ studyEnvParams, enrolleeShortcode })
    setParticipantFiles(documents)
  }

  useEffect(() => {
    loadDocuments()
  }, [])

  return <>
    <h5 className={'mt-3'}>{studyName} ({participantFiles.length})</h5>
    <div className="d-flex flex-column">
      {participantFiles.length > 0 && <table className="table">
        <thead>
          <tr>
            <th></th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {participantFiles.map((participantFile, index) => (
            <tr key={index}>
              <td>
                <div>
                  {fileTypeToIcon(participantFile.fileType)}
                  {participantFile.fileName}
                  <span className='fst-italic text-muted'> ({instantToDateString(participantFile.createdAt)})</span>
                  {surveyResponseIdsToTaskNames(i18n, studyEnvParams, enrollee, participantFile.surveyResponseIds)}
                </div>
              </td>
              <td className="align-middle">
                <div className={'d-flex justify-content-end'}>
                  <button className="btn btn-outline-primary" onClick={async () => {
                    const response = await Api.downloadParticipantFile({
                      studyEnvParams, enrolleeShortcode: enrollee.shortcode, fileName: participantFile.fileName
                    })
                    saveBlobAsDownload(await response.blob(), participantFile.fileName)
                  }}>
                    <span className="d-flex align-items-center">
                      <FontAwesomeIcon className="pe-1" icon={faDownload}/>{i18n('documentDownloadButton')}
                    </span>
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>}
      {participantFiles.length === 0 &&
          <div className="text-muted fst-italic my-3">{i18n('documentsListNone')}</div>
      }
    </div>
  </>
}

const surveyResponseIdsToTaskNames = (
  i18n: (key: string, options?: I18nOptions) => string, studyEnvParams: StudyEnvParams,
  enrollee: Enrollee, surveyResponseIds: string[]
) => {
  const associatedTasks = surveyResponseIds.map(surveyResponseId => {
    return enrollee.participantTasks.find(task => task.surveyResponseId === surveyResponseId)
  }).filter(task => task !== undefined)

  if (associatedTasks.length === 0) {
    return null
  }

  return (
    <div className={'mt-2'}>
      <span className={'fw-medium'}>Survey Responses</span>
      <ul>
        {associatedTasks.map(task =>
          <li key={task!.id}>
            <Link to={`../${getTaskPath(task!, enrollee.shortcode, studyEnvParams.studyShortcode)}`}>
              {i18n(`${task!.targetStableId}:${task!.targetAssignedVersion}`, { defaultValue: task!.targetName })}
            </Link>
          </li>
        )}
      </ul>
    </div>
  )
}

const fileTypeToIcon = (fileType: string) => {
  if (fileType.startsWith('image/')) {
    return <FontAwesomeIcon className="me-2" icon={faFileImage}/>
  }
  switch (fileType) {
    case 'text/plain':
      return <FontAwesomeIcon className="me-2" icon={faFileLines}/>
    case 'application/pdf':
      return <FontAwesomeIcon className="me-2" icon={faFilePdf}/>
    default:
      return <FontAwesomeIcon className="me-2" icon={faFile}/>
  }
}
