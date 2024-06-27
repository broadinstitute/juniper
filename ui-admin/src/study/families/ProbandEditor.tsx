import React from 'react'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { EnrolleeSearchbar } from 'study/participants/enrolleeView/EnrolleeSearchbar'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import {
  faCheck,
  faPencil,
  faX
} from '@fortawesome/free-solid-svg-icons'


/**
 *
 */
export const ProbandEditor = (
  {
    family,
    studyEnvContext,
    reloadFamily
  }: {
    family: Family,
    studyEnvContext: StudyEnvContextT,
    reloadFamily: () => void
  }) => {
  const [proband, setProband] = React.useState<Enrollee>(family.proband)
  const [editMode, setEditMode] = React.useState<boolean>(false)
  return <div>
    <h4>Proband</h4>
    <div className="d-flex">
      <div style={{ width: '250px' }}>
        <EnrolleeSearchbar
          disabled={!editMode}
          selectedEnrollee={proband}
          studyEnvContext={studyEnvContext}
          onEnrolleeSelected={newProband => setProband(newProband || family.proband)}
          searchExpFilter={`{family.shortcode} = '${family.shortcode}'`}
        />
      </div>


      {!editMode && <button
        className="btn btn-secondary ms-2"
        onClick={() => {
          setEditMode(!editMode)
          if (!editMode) {
            setProband(family.proband)
          }
        }}>
        <FontAwesomeIcon icon={faPencil} />
      </button>}
      {editMode && <>
        <button
          className="btn btn-success ms-2"
          onClick={async () => {
            console.log(proband)
            setEditMode(false)
            reloadFamily()
          }}>
          <FontAwesomeIcon icon={faCheck}/>
        </button>
        <button
          className="btn btn-secondary"
          onClick={() => {
            setProband(family.proband)
            setEditMode(false)
          }}>
          <FontAwesomeIcon icon={faX}/>
        </button>
      </>}

    </div>
  </div>
}
