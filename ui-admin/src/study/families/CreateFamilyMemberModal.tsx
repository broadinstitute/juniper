import React, { useState } from 'react'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
  ModalTitle
} from 'react-bootstrap'
import {
  Enrollee,
  Family,
  Profile
} from '@juniper/ui-core'
import { StudyEnvContextT } from 'study/StudyEnvironmentRouter'
import Api from 'api/api'
import Select from 'react-select'
import JustifyChangesModal from 'study/participants/JustifyChangesModal'
import { FamilyRelationshipSelect } from 'study/families/FamilyRelationshipSelect'
import { EditableProfile } from 'study/participants/enrolleeView/EnrolleeProfile'
import {
  InfoCard,
  InfoCardBody,
  InfoCardHeader,
  InfoCardTitle
} from 'components/InfoCard'
import { Store } from 'react-notifications-component'
import {
  failureNotification,
  successNotification
} from 'util/notifications'

// sentinel value for the "new member" option in the enrollee/target selects
const NEW_MEMBER = 'NEW_MEMBER'

const enrolleeName = (enrollee: Enrollee): string => {
  const name = [enrollee.profile?.givenName, enrollee.profile?.familyName].filter(n => !!n).join(' ')
  return name || enrollee.shortcode
}

/**
 * Modal for study staff to create a brand new enrollee as a member of the family. The new enrollee is related
 * to an existing family member (the proband by default), and gets a placeholder (non-deliverable) account email
 * derived from that member's, so they can't log in unless their email is later changed and they are invited.
 */
export const CreateFamilyMemberModal = ({
  family, studyEnvContext, onCreated, onClose
}: {
  family: Family, studyEnvContext: StudyEnvContextT, onCreated: () => void, onClose: () => void
}) => {
  const members = family.members || []
  const proband = members.find(m => m.id === family.probandEnrolleeId) || members[0]
  // The new member is one side of the relation ("{enrollee} is the {relationship} of {target}"), and an existing
  // family member is the other. By default the proband is the enrollee and the new member is the target, but
  // either side can be changed.
  const [enrolleeSide, setEnrolleeSide] = useState<string>(proband?.shortcode ?? NEW_MEMBER)
  const [targetSide, setTargetSide] = useState<string>(NEW_MEMBER)
  const [relationship, setRelationship] = useState('')
  const [profile, setProfile] = useState<Profile>({
    doNotEmail: true,
    doNotEmailSolicit: false,
    mailingAddress: { street1: '', street2: '', city: '', state: '', country: '', postalCode: '' }
  })
  const [showJustify, setShowJustify] = useState(false)

  const sideOptions = [{ value: NEW_MEMBER, label: 'New member' }].concat(members.map(m => ({
    value: m.shortcode,
    label: `${enrolleeName(m)} (${m.shortcode})`
  })))

  // exactly one side is the new member: picking an existing member on a side moves "new member" to the other side
  // if it isn't already there, and picking "new member" on a side moves the other side back to an existing member
  const changeSide = (side: 'enrollee' | 'target', value: string) => {
    const [setThis, setOther, otherValue] = side === 'enrollee'
      ? [setEnrolleeSide, setTargetSide, targetSide]
      : [setTargetSide, setEnrolleeSide, enrolleeSide]
    setThis(value)
    if (value === NEW_MEMBER && otherValue === NEW_MEMBER) {
      setOther(proband?.shortcode ?? NEW_MEMBER)
    } else if (value !== NEW_MEMBER && otherValue !== NEW_MEMBER) {
      setOther(NEW_MEMBER)
    }
  }

  const newMemberIsTarget = targetSide === NEW_MEMBER
  const existingShortcode = newMemberIsTarget ? enrolleeSide : targetSide
  const existing = members.find(m => m.shortcode === existingShortcode)
  const canSubmit = !!existing && relationship.trim().length > 0

  const sideLabel = (value: string) => value === NEW_MEMBER
    ? 'the new member'
    : sideOptions.find(o => o.value === value)?.label ?? value

  const create = async (justification: string) => {
    try {
      const created = await Api.createFamilyMember(
        studyEnvContext.portal.shortcode,
        studyEnvContext.study.shortcode,
        studyEnvContext.currentEnv.environmentName,
        family.shortcode,
        {
          existingEnrolleeShortcode: existingShortcode,
          newMemberIsTarget,
          familyRelationship: relationship.trim(),
          profile
        },
        justification)
      Store.addNotification(successNotification(`Created ${created.shortcode}.`))
      onCreated()
    } catch (e) {
      Store.addNotification(failureNotification('Could not create family member.'))
      setShowJustify(false)
    }
  }

  if (showJustify) {
    return <JustifyChangesModal
      saveWithJustification={create}
      onDismiss={() => setShowJustify(false)}
      animated={false}
      bodyText={<p>
        Create a new enrollee and add them to this family, where {sideLabel(enrolleeSide)} is
        the {relationship.trim()} of {sideLabel(targetSide)}?
      </p>}
    />
  }

  return <Modal show onHide={onClose} size="xl">
    <ModalHeader>
      <ModalTitle>Create new family member</ModalTitle>
    </ModalHeader>
    <ModalBody>
      <p className="text-muted">
        This creates a new enrollee. Their account email will be based on the existing member&apos;s email. They
        will not be able to log in unless their email is changed and they are invited.
      </p>
      <InfoCard>
        <InfoCardHeader>
          <InfoCardTitle title="Relationship"/>
        </InfoCardHeader>
        <InfoCardBody>
          <div className="row w-100 align-items-center">
            <div className="col fw-bold mb-2">Enrollee</div>
            <div className="col fw-bold mb-2">Relation</div>
            <div className="col fw-bold mb-2">Target</div>
          </div>
          <div className="row w-100 align-items-center">
            <div className="col">
              <SideSelect label="Enrollee" options={sideOptions} value={enrolleeSide}
                onChange={value => changeSide('enrollee', value)}/>
            </div>
            <div className="col">
              <span className="fst-italic">
                is the <FamilyRelationshipSelect className="d-inline-block" value={relationship}
                  onChange={setRelationship}/> of
              </span>
            </div>
            <div className="col">
              <SideSelect label="Target" options={sideOptions} value={targetSide}
                onChange={value => changeSide('target', value)}/>
            </div>
          </div>
        </InfoCardBody>
      </InfoCard>
      <InfoCard>
        <InfoCardHeader>
          <InfoCardTitle title="Profile"/>
        </InfoCardHeader>
        <InfoCardBody>
          <EditableProfile profile={profile} setProfile={setProfile} hideAccountFields/>
        </InfoCardBody>
      </InfoCard>
    </ModalBody>
    <ModalFooter>
      <button className="btn btn-primary" disabled={!canSubmit} onClick={() => setShowJustify(true)}>
        Create
      </button>
      <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
    </ModalFooter>
  </Modal>
}

/** one side (enrollee or target) of the relation: either the new member or an existing family member */
const SideSelect = ({ label, options, value, onChange }: {
  label: string, options: { value: string, label: string }[], value: string, onChange: (value: string) => void
}) => <Select
  aria-label={label}
  options={options}
  styles={{ control: baseStyles => ({ ...baseStyles, borderColor: 'var(--bs-border-color)' }) }}
  value={options.find(o => o.value === value) || null}
  onChange={option => option && onChange(option.value)}
/>
