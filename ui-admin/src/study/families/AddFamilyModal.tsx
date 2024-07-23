import React, {
  useEffect,
  useState
} from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import {
  Enrollee,
  Family
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api from 'api/api'
import { useLoadingEffect } from 'api/api-utils'
import { getFamilyNameString } from 'util/familyUtils'
import Select from 'react-select'
import { isNil } from 'lodash'
import LoadingSpinner from 'util/LoadingSpinner'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { EnrolleeLink } from 'study/participants/enrolleeView/EnrolleeLink'
import { FamilyLink } from 'study/families/FamilyLink'
import { Store } from 'react-notifications-component'
import { failureNotification } from 'util/notifications'

/**
 * Modal for adding an enrollee to a family or creating a new family with the enrollee as the proband.
 */
export const AddFamilyModal = (
  {
    enrollee,
    studyEnvContext,
    onAddFamily,
    onClose
  }: {
    enrollee: Enrollee,
    studyEnvContext: StudyEnvContextT,
    onAddFamily: (family: Family) => void,
    onClose: () => void
  }
) => {
  const [families, setFamilies] = React.useState<Family[]>([])
  const [selectedFamily, setSelectedFamily] = React.useState<Family>()

  const { isLoading: isLoadingFamilies } = useLoadingEffect(async () => {
    const families = await Api.getAllFamilies(
      studyEnvContext.portal.shortcode,
      studyEnvContext.study.shortcode,
      studyEnvContext.currentEnv.environmentName
    )
    setFamilies(families)
  })

  const familyOptions = families
    .filter(family => family.members?.every(member => member.shortcode !== enrollee.shortcode))
    .map(family => {
      return {
        value: family.shortcode,
        label: `${getFamilyNameString(family)} (${family.shortcode})`
      }
    })

  const addToFamily = async (justification: string) => {
    if (!selectedFamily) {
      return
    }
    try {
      await Api.addMemberToFamily(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        selectedFamily.shortcode,
        enrollee.shortcode,
        justification
      )

      onAddFamily(selectedFamily)
    } catch (e) {
      Store.addNotification(failureNotification('Failed to add to family'))
    }
  }

  const createFamily = async (justification: string) => {
    try {
      const family = await Api.createFamily(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        {
          probandEnrolleeId: enrollee.id,
          studyEnvironmentId: studyEnvContext.currentEnv.id,
          id: '',
          createdAt: Date.now(),
          lastUpdatedAt: Date.now(),
          shortcode: ''
        },
        justification
      )

      onAddFamily(family)
    } catch (e) {
      Store.addNotification(failureNotification('Failed to create family'))
    }
  }

  const [animated, setAnimated] = useState(true)
  const [showAddFamilyJustification, setShowAddFamilyJustification] = useState(false)
  const [showCreateFamilyJustification, setShowCreateFamilyJustification] = useState(false)

  useEffect(() => {
    if (showAddFamilyJustification || showCreateFamilyJustification) {
      setAnimated(false)
    }
  }, [showAddFamilyJustification, showCreateFamilyJustification])

  if (showAddFamilyJustification && selectedFamily) {
    return <JustifyChangesModal
      saveWithJustification={addToFamily}
      onDismiss={() => setShowAddFamilyJustification(false)}
      animated={false}
      bodyText={<p>
        Are you sure you want to add <span className="fst-italic"><EnrolleeLink
          studyEnvContext={studyEnvContext}
          enrollee={enrollee}
        /></span> to the <span className="fst-italic"><FamilyLink
          family={selectedFamily}
          studyEnvContext={studyEnvContext}
        /></span>?
      </p>}
    />
  }

  if (showCreateFamilyJustification) {
    return <JustifyChangesModal
      saveWithJustification={createFamily}
      onDismiss={() => setShowCreateFamilyJustification(false)}
      animated={false}
      bodyText={<p>
        Are you sure you want to create a new family with <span className="fst-italic"><EnrolleeLink
          studyEnvContext={studyEnvContext}
          enrollee={enrollee}
        /></span> as the proband?
      </p>}/>
  }


  return <Modal show onHide={onClose} animation={animated}>
    <ModalHeader>
      <ModalTitle>Add {enrollee.shortcode} to Family</ModalTitle>
    </ModalHeader>
    <ModalBody>
      {isLoadingFamilies ? <LoadingSpinner/> : <>
        <h5>Add to existing family</h5>
        <div className="my-2">
          <Select
            options={familyOptions}
            value={familyOptions.find(option => option.value === selectedFamily?.shortcode)}
            isClearable
            onChange={option => setSelectedFamily(families.find(family => family.shortcode === option?.value))}
          />
        </div>

        <button
          disabled={isNil(selectedFamily)}
          className="btn btn-primary "
          onClick={() => setShowAddFamilyJustification(true)}
        >
          Add
        </button>

        <div
          className="w-100 border-bottom border-1 my-3"
        />

        <h5>Create new family</h5>

        <p
          className="text-muted"
        >
          This will create a new family with {enrollee.shortcode} as the proband.
          You will be able to change the proband later.
        </p>

        <button
          className="btn btn-primary"
          onClick={() => setShowCreateFamilyJustification(true)}
        >
          Create
        </button>
      </>}

    </ModalBody>
    <ModalFooter>
      <button
        className="btn btn-secondary"
        onClick={onClose}
      >
        Cancel
      </button>
    </ModalFooter>
  </Modal>
}
