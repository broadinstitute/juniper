import {
  Enrollee,
  EnvironmentName,
  getTaskPath,
  getTranslatedStudyName,
  I18nOptions,
  instantToDateString,
  ParticipantFile,
  ParticipantTask,
  QuarantinedFileModal,
  StudyEnvParams,
  UnscannedFileModal,
  useI18n
} from '@juniper/ui-core'
import React, {
  useEffect,
  useState
} from 'react'
import { useActiveUser } from 'providers/ActiveUserProvider'
import Api from 'api/api'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faFile,
  faFileImage,
  faFileLines,
  faFilePdf
} from '@fortawesome/free-solid-svg-icons'
import { usePortalEnv } from 'providers/PortalProvider'
import { Link } from 'react-router-dom'
import Modal from 'react-bootstrap/Modal'
import ThemedModal from 'components/ThemedModal'
import { LoadingSpinner } from 'util/LoadingSpinner'
import { downloadFile } from 'util/downloadUtils'

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
                  enrollee={enrollees.find(enrollee => enrollee.profileId === ppUser?.profileId &&
                    enrollee.studyEnvironmentId === pStudy.study.studyEnvironments[0].id)!}
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
  const [isLoading, setIsLoading] = useState(true)

  const loadDocuments = async () => {
    const enrolleeShortcode = enrollee.shortcode
    const documents = await Api.listParticipantFiles({ studyEnvParams, enrolleeShortcode })
    setParticipantFiles(documents)
  }

  useEffect(() => {
    setIsLoading(true)
    loadDocuments().then(() => {
      setIsLoading(false)
    })
  }, [])

  if (isLoading) {
    return <LoadingSpinner/>
  }

  return <>
    <h5
      className={'mt-3'}>
      {getTranslatedStudyName(
        i18n, studyEnvParams.portalShortcode, studyEnvParams.studyShortcode, studyName
      )} ({participantFiles.length})</h5>
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
      <span>{i18n('documentSharedInResponseTo')}:</span>
      <ul>
        {associatedTasks.map(task => {
          const linkText = i18n(`${task.targetStableId}:${task.targetAssignedVersion}`,
            { defaultValue: task.targetName })
          // don't use a link for study staff forms -- the participant can't see them
          if (task.taskType === 'ADMIN_FORM') {
            return <li key={task.id}>{linkText}</li>
          }
          return <li key={task.id}>
            <Link to={`../${getTaskPath(task, enrollee.shortcode, studyEnvParams.studyShortcode)}`}>
              {linkText}
            </Link>
          </li>
        })}
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
  const [showQuarantinedFileModal, setShowQuarantinedFileModal] = useState(false)
  const [showUnscannedFileModal, setShowUnscannedFileModal] = useState(false)

  const tryDownload = async () => {
    if (participantFile.virusScanResult === 'CLEAN') {
      downloadFile(studyEnvParams, enrollee.shortcode, participantFile.fileName)
    } else if (participantFile.virusScanResult === 'QUARANTINED') {
      setShowQuarantinedFileModal(true)
    } else if (participantFile.virusScanResult === 'UNSCANNED') {
      setShowUnscannedFileModal(true)
    }
  }

  const { i18n } = useI18n()
  return (<>
    <li className="nav-item dropdown d-flex flex-column">
      <button className="btn btn-outline-primary dropdown-toggle" id="fileOptionsDropdown"
        data-bs-toggle="dropdown" aria-expanded="false">
        {i18n('documentOptionsButton')}
      </button>
      <ul className="dropdown-menu" aria-labelledby="fileOptionsDropdown">
        <li>
          <a role={'button'} className="dropdown-item"
            onClick={() => setShowConfirmDelete(true)}
          >
            {i18n('documentDeleteButton')}
          </a>
        </li>
        <li>
          <a className="dropdown-item" role={'button'} onClick={tryDownload}>
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
            {participantFile.associatedAnswers.length === 0 ?
              i18n('documentDeleteAreYouSureTitle') : i18n('documentDeleteDocumentInUseTitle')}
          </h2>
        </Modal.Title>
      </Modal.Header>
      <Modal.Body>
        {participantFile.associatedAnswers.length === 0 ?
          <p className="m-0">
            {i18n('documentDeleteAreYouSureMessage')}
          </p> :
          <p className="m-0">{i18n('documentDeleteDocumentInUseMessage')}</p>
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
            {i18n('documentDeleteButton')}
          </button>
          <button className={'btn btn-outline-secondary m-2'}
            onClick={() => setShowConfirmDelete(false)}>
            {i18n('cancel')}
          </button>
        </div>
      </Modal.Footer>
    </ThemedModal> }

    {showQuarantinedFileModal && <QuarantinedFileModal
      onClose={() => setShowQuarantinedFileModal(false)}
      ModalComponent={ThemedModal}
    />}
    {showUnscannedFileModal && <UnscannedFileModal
      onClose={() => setShowUnscannedFileModal(false)}
      enrolleeShortcode={enrollee.shortcode}
      studyEnvParams={studyEnvParams}
      participantFile={participantFile}
      ModalComponent={ThemedModal}
    />}


  </>
  )
}
