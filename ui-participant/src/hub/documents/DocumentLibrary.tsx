import {
  Enrollee,
  EnvironmentName,
  I18nOptions,
  instantToDateString,
  ParticipantFile, ParticipantTask, saveBlobAsDownload,
  StudyEnvParams,
  useI18n
} from '@juniper/ui-core'
import React, { useEffect, useState } from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFile, faFileImage, faFileLines, faFilePdf } from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from 'providers/PortalProvider'
import { Link } from 'react-router-dom'
import { getTaskPath } from '../task/taskUtils'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from 'components/ThemedModal'

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
                  {surveyResponseIdsToTaskNames(
                    i18n,
                    studyEnvParams,
                    enrollee,
                    participantFile.associatedAnswers.map(answer => answer.surveyResponseId!))
                  }
                </div>
              </td>
              <td className="align-middle">
                <div className={'d-flex justify-content-end'}>
                  <FileOptionsDropdown
                    studyEnvParams={studyEnvParams}
                    loadDocuments={loadDocuments}
                    participantFile={participantFile}
                    enrollee={enrollee}/>
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
  }).filter((task): task is ParticipantTask => task !== undefined)

  if (associatedTasks.length === 0) {
    return null
  }

  return (
    <div className={'mt-2 text-muted'}>
      <span>shared in response to:</span>
      <ul>
        {associatedTasks.map(task =>
          <li key={task.id}>
            <Link to={`../${getTaskPath(task, enrollee.shortcode, studyEnvParams.studyShortcode)}`}>
              {i18n(`${task.targetStableId}:${task.targetAssignedVersion}`, { defaultValue: task.targetName })}
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

const FileOptionsDropdown = ({ studyEnvParams, participantFile, enrollee, loadDocuments }: {
  studyEnvParams: StudyEnvParams, participantFile: ParticipantFile, enrollee: Enrollee, loadDocuments: () => void
}) => {
  const [showConfirmDelete, setShowConfirmDelete] = useState(false)
  const { i18n } = useI18n()
  return (<>
    <li className="nav-item dropdown d-flex flex-column">
      <button className="btn btn-outline-primary dropdown-toggle" id="fileOptionsDropdown"
        data-bs-toggle="dropdown" aria-expanded="false">
            Options
      </button>
      <ul className="dropdown-menu" aria-labelledby="fileOptionsDropdown">
        <li>
          <a role={'button'} className="dropdown-item"
            onClick={() => setShowConfirmDelete(true)}
          >
                Delete
          </a>
        </li>
        <li>
          <a className="dropdown-item" role={'button'} onClick={async () => {
            const response = await Api.downloadParticipantFile({
              studyEnvParams, enrolleeShortcode: enrollee.shortcode, fileName: participantFile.fileName
            })
            saveBlobAsDownload(await response.blob(), participantFile.fileName)
          }}>
            {i18n('documentDownloadButton')}
          </a>
        </li>
      </ul>
    </li>
    {showConfirmDelete && <ThemedModal show={true}
      onHide={() => setShowConfirmDelete(false)} size={'lg'} animation={true}>
      <Modal.Header>
        <Modal.Title>
          <h2 className="fw-bold pb-0 mb-0">
            {participantFile.associatedAnswers.length === 0 ? 'Are you sure?' : 'This document is in use'}
          </h2>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {participantFile.associatedAnswers.length === 0 ?
          <p className="m-0">
            Are you sure you want to delete this document? This cannot be undone. Please note that if a member
            of the study staff has already downloaded this document, it will still be accessible to them. Please
            contact the study staff if you would like to have the document fully removed from all records.
          </p> :
          <p className="m-0">This document is currently shared in response to at least one survey. Please remove it
                  from the survey response(s) before deleting it.</p>
        }
      </Modal.Body>
      <Modal.Footer>
        <div className={'d-flex w-100'}>
          <button className={'btn btn-primary m-2'}
            disabled={participantFile.associatedAnswers.length > 0}
            onClick={async () => {
              await Api.deleteParticipantFile({
                studyEnvParams, enrolleeShortcode: enrollee.shortcode, fileName: participantFile.fileName
              })
              loadDocuments()
              setShowConfirmDelete(false)
            }}>
                Delete
          </button>
          <button className={'btn btn-outline-secondary m-2'}
            onClick={() => setShowConfirmDelete(false)}>
            {i18n('cancel')}
          </button>
        </div>
      </Modal.Footer>
    </ThemedModal> }
  </>
  )
}
